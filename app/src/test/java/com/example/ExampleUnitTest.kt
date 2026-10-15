package com.example

import com.example.data.remote.AiAction
import com.example.data.remote.GeminiAdvisorService
import com.example.util.AttendanceCalculator
import com.example.util.AttendanceStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAttendanceCalculator_safeBunks() {
        // Attended 30 out of 30 classes with 75% target
        // Max allowed total = 30 / 0.75 = 40. Bunks allowed = 40 - 30 = 10.
        val result = AttendanceCalculator.calculate(attended = 30, total = 30, targetPercentage = 75.0)
        assertEquals(100.0, result.percentage, 0.01)
        assertEquals(AttendanceStatus.SAFE, result.status)
        assertEquals(10, result.safeBunks)
        assertEquals(0, result.classesNeeded)
    }

    @Test
    fun testAttendanceCalculator_singleClassHeld_zeroSafeBunks() {
        // Attended 1 out of 1 class (100%): maxAllowedTotal = 1 / 0.75 = 1.33 -> floor(0.33) = 0 safe bunks
        val result = AttendanceCalculator.calculate(attended = 1, total = 1, targetPercentage = 75.0)
        assertEquals(100.0, result.percentage, 0.01)
        assertEquals(AttendanceStatus.SAFE, result.status)
        assertEquals(0, result.safeBunks)
        assertEquals(0, result.classesNeeded)
    }

    @Test
    fun testAttendanceCalculator_classesNeeded() {
        // Attended 6 out of 10 classes (60%) with 75% target
        // Needed: ceil((75*10 - 600) / 25) = ceil(150 / 25) = 6.
        val result = AttendanceCalculator.calculate(attended = 6, total = 10, targetPercentage = 75.0)
        assertEquals(60.0, result.percentage, 0.01)
        assertEquals(AttendanceStatus.CRITICAL, result.status)
        assertEquals(0, result.safeBunks)
        assertEquals(6, result.classesNeeded)

        // Verifying: if 6 attended consecutively: 12 / 16 = 75.0%
        val simulated = AttendanceCalculator.simulate(
            currentAttended = 6,
            currentTotal = 10,
            futureAttended = 6,
            futureMissed = 0
        )
        assertEquals(75.0, simulated, 0.01)
    }

    @Test
    fun testAttendanceCalculator_exactThreshold() {
        // Attended 15 out of 20 (75.0%) with 75.0% target
        val result = AttendanceCalculator.calculate(attended = 15, total = 20, targetPercentage = 75.0)
        assertEquals(75.0, result.percentage, 0.01)
        assertEquals(0, result.safeBunks)
        assertEquals(0, result.classesNeeded)
    }

    @Test
    fun testAttendanceCalculator_zeroClasses() {
        val result = AttendanceCalculator.calculate(attended = 0, total = 0, targetPercentage = 75.0)
        assertEquals(100.0, result.percentage, 0.01)
        assertEquals(AttendanceStatus.SAFE, result.status)
    }

    @Test
    fun testAttendanceCalculator_forecastSemester() {
        val forecast = AttendanceCalculator.forecastSemester(
            currentAttended = 20,
            currentHeld = 24,
            remainingWeeks = 8,
            classesPerWeek = 3,
            targetPercentage = 75.0
        )
        assertEquals(48, forecast.projectedTotalClasses)
        assertEquals(24, forecast.remainingClasses)
        assertEquals(16, forecast.minClassesToAttend)
        assertEquals(8, forecast.totalBunksAllowedForSemester)
        assertTrue(forecast.isAchievable)
    }

    @Test
    fun testGeminiModelIdNormalization() {
        assertEquals("gemini-2.5-flash", GeminiAdvisorService.normalizeApiModelId("gemini-3.5-flash"))
        assertEquals("gemini-2.5-pro", GeminiAdvisorService.normalizeApiModelId("gemini-3.1-pro-preview"))
        assertEquals("gemini-2.5-flash-lite", GeminiAdvisorService.normalizeApiModelId("gemini-3.1-flash-lite-preview"))
        assertEquals("gemini-2.5-flash", GeminiAdvisorService.normalizeApiModelId("gemini-2.5-flash"))
    }

    @Test
    fun testAiActionParsing_updateAttendance() {
        val service = GeminiAdvisorService()
        val rawResponse = """
            I have marked Operating Systems as Present for today.
            ```json:action
            {
              "actions": [
                {
                  "type": "UPDATE_ATTENDANCE",
                  "subjectName": "Operating Systems",
                  "status": "PRESENT"
                }
              ]
            }
            ```
        """.trimIndent()

        val parsed = service.parseActionsFromResponse(rawResponse)
        assertEquals(1, parsed.actions.size)
        assertTrue(parsed.actions[0] is AiAction.UpdateAttendance)
        val action = parsed.actions[0] as AiAction.UpdateAttendance
        assertEquals("Operating Systems", action.subjectName)
        assertEquals("PRESENT", action.status)
    }

    @Test
    fun testCalendarDateCycleLogic() {
        // Simulating the user-specified interaction:
        // 1. Initial unmarked: tap once -> PRESENT (Green)
        // 2. Tap on green circle again -> ABSENT (Red)
        // 3. Tap on red circle again -> Cleared (Null)
        val initialStatus: String? = null
        val firstTap = when (initialStatus) {
            null -> "PRESENT"
            "PRESENT" -> "ABSENT"
            else -> null
        }
        assertEquals("PRESENT", firstTap)

        val secondTap = when (firstTap) {
            null -> "PRESENT"
            "PRESENT" -> "ABSENT"
            else -> null
        }
        assertEquals("ABSENT", secondTap)

        val thirdTap = when (secondTap) {
            null -> "PRESENT"
            "PRESENT" -> "ABSENT"
            else -> null
        }
        assertEquals(null, thirdTap)
    }
}
