package com.example.util

import kotlin.math.ceil
import kotlin.math.floor

enum class AttendanceStatus {
    SAFE,       // >= target% (e.g. >= 75%)
    WARNING,    // target - margin <= % < target% (e.g. 70% to 74.9%)
    CRITICAL    // < target - margin (e.g. < 70%)
}

data class CalculationResult(
    val percentage: Double,
    val status: AttendanceStatus,
    val safeBunks: Int,
    val classesNeeded: Int,
    val statusMessage: String
)

data class SemesterForecast(
    val projectedTotalClasses: Int,
    val remainingClasses: Int,
    val minClassesToAttend: Int,
    val totalBunksAllowedForSemester: Int,
    val isAchievable: Boolean,
    val explanation: String
)

object AttendanceCalculator {

    fun calculate(
        attended: Int,
        total: Int, // Classes held so far
        targetPercentage: Double = 75.0,
        warningMargin: Double = 5.0
    ): CalculationResult {
        if (total <= 0) {
            return CalculationResult(
                percentage = 100.0,
                status = AttendanceStatus.SAFE,
                safeBunks = 0,
                classesNeeded = 0,
                statusMessage = "Semester starting. 0 classes held yet. Log Present or Absent as lectures occur."
            )
        }

        val percentage = (attended.toDouble() / total.toDouble()) * 100.0

        return if (percentage >= targetPercentage) {
            // Can safely bunk classes
            val targetDecimal = targetPercentage / 100.0
            val maxAllowedTotal = attended.toDouble() / targetDecimal
            val bunks = floor(maxAllowedTotal - total).toInt().coerceAtLeast(0)
            val status = if (percentage >= targetPercentage + warningMargin) {
                AttendanceStatus.SAFE
            } else {
                AttendanceStatus.WARNING
            }
            val msg = if (bunks > 0) {
                "On track! Based on $attended/$total classes held, you can safely miss $bunks class${if (bunks > 1) "es" else ""} without dropping below ${targetPercentage.toInt()}%."
            } else {
                "Right on the line (${String.format(java.util.Locale.US, "%.1f", percentage)}%)! Missing the next class will drop you below ${targetPercentage.toInt()}%."
            }

            CalculationResult(
                percentage = percentage,
                status = status,
                safeBunks = bunks,
                classesNeeded = 0,
                statusMessage = msg
            )
        } else {
            // Need to attend classes
            val needed = if (targetPercentage >= 100.0) {
                999
            } else {
                val numerator = (targetPercentage * total) - (100.0 * attended)
                val denominator = 100.0 - targetPercentage
                ceil(numerator / denominator).toInt().coerceAtLeast(1)
            }
            val status = if (percentage < targetPercentage - warningMargin) {
                AttendanceStatus.CRITICAL
            } else {
                AttendanceStatus.WARNING
            }
            val msg = "Danger (${String.format(java.util.Locale.US, "%.1f", percentage)}%)! You must attend the next $needed consecutive class${if (needed > 1) "es" else ""} to reach ${targetPercentage.toInt()}%."

            CalculationResult(
                percentage = percentage,
                status = status,
                safeBunks = 0,
                classesNeeded = needed,
                statusMessage = msg
            )
        }
    }

    /**
     * Simulates future percentage if the student attends [futureAttended] and misses [futureMissed] classes.
     */
    fun simulate(
        currentAttended: Int,
        currentTotal: Int,
        futureAttended: Int,
        futureMissed: Int
    ): Double {
        val newAttended = currentAttended + futureAttended
        val newTotal = currentTotal + futureAttended + futureMissed
        if (newTotal <= 0) return 100.0
        return (newAttended.toDouble() / newTotal.toDouble()) * 100.0
    }

    /**
     * Projects semester totals based on estimated remaining weeks and weekly lecture frequency.
     */
    fun forecastSemester(
        currentAttended: Int,
        currentHeld: Int,
        remainingWeeks: Int,
        classesPerWeek: Int,
        targetPercentage: Double = 75.0
    ): SemesterForecast {
        val remainingClasses = (remainingWeeks * classesPerWeek).coerceAtLeast(0)
        val projectedTotalClasses = currentHeld + remainingClasses

        if (projectedTotalClasses <= 0) {
            return SemesterForecast(
                projectedTotalClasses = 0,
                remainingClasses = 0,
                minClassesToAttend = 0,
                totalBunksAllowedForSemester = 0,
                isAchievable = true,
                explanation = "Start marking lectures as they are conducted."
            )
        }

        val targetDecimal = targetPercentage / 100.0
        val totalAttendedNeededAtEnd = ceil(projectedTotalClasses * targetDecimal).toInt()
        val futureMustAttend = (totalAttendedNeededAtEnd - currentAttended).coerceAtLeast(0)

        return if (futureMustAttend > remainingClasses) {
            SemesterForecast(
                projectedTotalClasses = projectedTotalClasses,
                remainingClasses = remainingClasses,
                minClassesToAttend = remainingClasses,
                totalBunksAllowedForSemester = 0,
                isAchievable = false,
                explanation = "Mathematical Alert: Even attending all remaining $remainingClasses classes, you will reach only ${String.format(java.util.Locale.US, "%.1f", ((currentAttended + remainingClasses).toDouble() / projectedTotalClasses) * 100.0)}%. Contact your academic advisor for condonation options."
            )
        } else {
            val bunksRemaining = (remainingClasses - futureMustAttend).coerceAtLeast(0)
            SemesterForecast(
                projectedTotalClasses = projectedTotalClasses,
                remainingClasses = remainingClasses,
                minClassesToAttend = futureMustAttend,
                totalBunksAllowedForSemester = bunksRemaining,
                isAchievable = true,
                explanation = "Across the remaining $remainingClasses classes (~$remainingWeeks weeks), you must attend at least $futureMustAttend classes. You can safely bunk up to $bunksRemaining classes across the rest of the semester!"
            )
        }
    }
}
