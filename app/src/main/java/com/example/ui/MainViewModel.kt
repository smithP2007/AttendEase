package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.db.AppDatabase
import com.example.data.db.AttendanceRecordEntity
import com.example.data.db.HolidayEntity
import com.example.data.db.SubjectEntity
import com.example.data.db.TimetableSlotEntity
import com.example.data.remote.AiAction
import com.example.data.remote.GeminiAdvisorService
import com.example.data.remote.GeminiModelInfo
import com.example.data.repository.AttendanceRepository
import com.example.data.settings.AccentTheme
import com.example.data.settings.SettingsManager
import com.example.data.settings.StudentProfile
import com.example.data.settings.ThemeMode
import com.example.util.AgentIntentEngine
import com.example.util.AttendanceCalculator
import com.example.util.AttendanceStatus
import com.example.util.ImageUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class OverallAttendanceStats(
    val totalAttended: Int,
    val totalConducted: Int,
    val overallPercentage: Double,
    val totalSafeBunks: Int,
    val totalClassesNeeded: Int,
    val subjectsInDanger: Int,
    val totalSubjects: Int
)

data class GeminiChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "GEMINI"
    val text: String,
    val modelId: String? = null,
    val imageUri: String? = null,
    val executedActions: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = AttendanceRepository(database)
    private val settingsManager = SettingsManager(application)
    private val authManager = AuthManager(application)
    private val geminiService = GeminiAdvisorService()

    // Settings StateFlows
    val themeMode: StateFlow<ThemeMode> = settingsManager.themeMode
    val dynamicColorEnabled: StateFlow<Boolean> = settingsManager.dynamicColorEnabled
    val accentTheme: StateFlow<AccentTheme> = settingsManager.accentTheme
    val compactCardView: StateFlow<Boolean> = settingsManager.compactCardView
    val maxRefreshRateEnabled: StateFlow<Boolean> = settingsManager.maxRefreshRateEnabled
    val lowLatencyHaptics: StateFlow<Boolean> = settingsManager.lowLatencyHaptics
    val gpuHardwareLayers: StateFlow<Boolean> = settingsManager.gpuHardwareLayers
    val globalTarget: StateFlow<Double> = settingsManager.targetPercentage
    val warningMargin: StateFlow<Double> = settingsManager.warningMargin
    val strictSingleRegistration: StateFlow<Boolean> = settingsManager.strictSingleRegistration
    val includeWeekendDays: StateFlow<Boolean> = settingsManager.includeWeekendDays
    val studentProfile: StateFlow<StudentProfile> = settingsManager.studentProfile
    val autoApplyAiActions: StateFlow<Boolean> = settingsManager.autoApplyAiActions

    // Gemini API Key
    val customApiKey: StateFlow<String> = authManager.customApiKey

    // Gemini Models
    val availableGeminiModels: List<GeminiModelInfo> = GeminiAdvisorService.AVAILABLE_MODELS
    val selectedGeminiModel: StateFlow<String> = settingsManager.selectedModel

    // Chat Messages
    private val _geminiChatMessages = MutableStateFlow<List<GeminiChatMessage>>(emptyList())
    val geminiChatMessages: StateFlow<List<GeminiChatMessage>> = _geminiChatMessages.asStateFlow()

    private val _isQueryingModel = MutableStateFlow(false)
    val isQueryingModel: StateFlow<Boolean> = _isQueryingModel.asStateFlow()

    private val _lastActionNotice = MutableStateFlow<String?>(null)
    val lastActionNotice: StateFlow<String?> = _lastActionNotice.asStateFlow()

    // Active Navigation Tab: 0=Dashboard, 1=Subjects, 2=Schedule, 3=Calculator, 4=Gemini AI
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Settings Screen visibility toggle
    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    // Timetable selected day (1=Mon ... 7=Sun)
    private val _selectedDay = MutableStateFlow(getInitialDayOfWeek())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    val subjects: StateFlow<List<SubjectEntity>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timetableSlots: StateFlow<List<TimetableSlotEntity>> = repository.timetableSlots
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<HolidayEntity>> = repository.holidays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentRecords: StateFlow<List<AttendanceRecordEntity>> = repository.recentRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecords: StateFlow<List<AttendanceRecordEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall aggregate statistics
    val overallStats: StateFlow<OverallAttendanceStats> = combine(
        subjects,
        globalTarget,
        warningMargin
    ) { subjectList, target, margin ->
        var totalAttended = 0
        var totalConducted = 0
        var totalBunks = 0
        var totalNeeded = 0
        var dangerCount = 0

        for (s in subjectList) {
            totalAttended += s.attendedClasses
            totalConducted += s.totalClasses
            val res = AttendanceCalculator.calculate(s.attendedClasses, s.totalClasses, target, margin)
            totalBunks += res.safeBunks
            totalNeeded += res.classesNeeded
            if (res.status != AttendanceStatus.SAFE) {
                dangerCount++
            }
        }

        val percentage = if (totalConducted > 0) {
            (totalAttended.toDouble() / totalConducted.toDouble()) * 100.0
        } else 100.0

        OverallAttendanceStats(
            totalAttended = totalAttended,
            totalConducted = totalConducted,
            overallPercentage = percentage,
            totalSafeBunks = totalBunks,
            totalClassesNeeded = totalNeeded,
            subjectsInDanger = dangerCount,
            totalSubjects = subjectList.size
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallAttendanceStats(0, 0, 100.0, 0, 0, 0, 0)
    )

    // Simulation sliders state
    private val _simFutureAttended = MutableStateFlow(10)
    val simFutureAttended: StateFlow<Int> = _simFutureAttended.asStateFlow()

    private val _simFutureMissed = MutableStateFlow(2)
    val simFutureMissed: StateFlow<Int> = _simFutureMissed.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    // Navigation & Settings
    fun selectTab(tab: Int) {
        _isSettingsOpen.value = false
        _selectedTab.value = tab
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    // Setting modifiers
    fun setThemeMode(mode: ThemeMode) = settingsManager.setThemeMode(mode)
    fun setDynamicColorEnabled(enabled: Boolean) = settingsManager.setDynamicColorEnabled(enabled)
    fun setAccentTheme(accent: AccentTheme) = settingsManager.setAccentTheme(accent)
    fun setCompactCardView(enabled: Boolean) = settingsManager.setCompactCardView(enabled)
    fun setMaxRefreshRateEnabled(enabled: Boolean) = settingsManager.setMaxRefreshRateEnabled(enabled)
    fun setLowLatencyHaptics(enabled: Boolean) = settingsManager.setLowLatencyHaptics(enabled)
    fun setGpuHardwareLayers(enabled: Boolean) = settingsManager.setGpuHardwareLayers(enabled)
    fun setGlobalTarget(target: Double) = settingsManager.setTargetPercentage(target)
    fun setWarningMargin(margin: Double) = settingsManager.setWarningMargin(margin)
    fun setStrictSingleRegistration(strict: Boolean) = settingsManager.setStrictSingleRegistration(strict)
    fun setIncludeWeekendDays(include: Boolean) = settingsManager.setIncludeWeekendDays(include)
    fun updateStudentProfile(name: String, roll: String, uni: String, sem: String) =
        settingsManager.updateStudentProfile(name, roll, uni, sem)
    fun setAutoApplyAiActions(auto: Boolean) = settingsManager.setAutoApplyAiActions(auto)

    fun selectDay(day: Int) {
        _selectedDay.value = day
    }

    fun setSimFutureAttended(count: Int) {
        _simFutureAttended.value = count
    }

    fun setSimFutureMissed(count: Int) {
        _simFutureMissed.value = count
    }

    fun clearActionNotice() {
        _lastActionNotice.value = null
    }

    fun setCustomApiKey(key: String) {
        authManager.setCustomGeminiApiKey(key)
    }

    fun testGeminiApiKey(apiKey: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = geminiService.testConnection(apiKey, selectedGeminiModel.value)
            result.onSuccess { msg -> onResult(true, msg) }
                .onFailure { err -> onResult(false, err.localizedMessage ?: "Connection error") }
        }
    }

    fun markAttendance(subject: SubjectEntity, status: String, note: String = "") {
        viewModelScope.launch {
            repository.markAttendance(
                subject = subject,
                status = status,
                note = note,
                strictSingleRegistration = strictSingleRegistration.value
            )
        }
    }

    fun cycleAttendanceForDate(subject: SubjectEntity, dateString: String) {
        viewModelScope.launch {
            val newStatus = repository.cycleAttendanceForDate(subject, dateString)
            val statusText = when (newStatus) {
                "PRESENT" -> "Marked '${subject.name}' as Present (Green) for $dateString"
                "ABSENT" -> "Marked '${subject.name}' as Absent (Red) for $dateString"
                else -> "Cleared attendance for $dateString"
            }
            _lastActionNotice.value = statusText
        }
    }

    fun undoRecord(record: AttendanceRecordEntity, subject: SubjectEntity) {
        viewModelScope.launch {
            repository.undoLastRecord(record, subject)
        }
    }

    fun toggleAttendanceForDate(subject: SubjectEntity, status: String, dateString: String) {
        viewModelScope.launch {
            repository.markAttendance(
                subject = subject,
                status = status,
                dateString = dateString,
                strictSingleRegistration = strictSingleRegistration.value
            )
        }
    }

    fun markAllPresentForDate(subjects: List<SubjectEntity>, dateString: String) {
        viewModelScope.launch {
            repository.markAllPresentForDate(subjects, dateString)
        }
    }

    fun clearAllForDate(subjects: List<SubjectEntity>, dateString: String) {
        viewModelScope.launch {
            repository.clearAllForDate(subjects, dateString)
        }
    }

    fun resetTodayRecords() {
        viewModelScope.launch {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.clearAllForDate(subjects.value, todayStr)
            _lastActionNotice.value = "Reset all attendance marks for today"
        }
    }

    fun clearAllRecords() {
        viewModelScope.launch {
            repository.clearAllAttendanceRecords()
            _lastActionNotice.value = "Cleared all attendance marks for this semester"
        }
    }

    fun restoreDemoData() {
        viewModelScope.launch {
            repository.resetAllDataToDemo()
            _lastActionNotice.value = "Restored sample university courses and timetable"
        }
    }

    fun addSubject(
        name: String,
        code: String,
        attended: Int,
        total: Int,
        target: Double,
        colorHex: String,
        credits: Int,
        room: String
    ) {
        viewModelScope.launch {
            repository.insertSubject(
                SubjectEntity(
                    name = name.trim(),
                    code = code.trim().uppercase(),
                    attendedClasses = attended,
                    totalClasses = total,
                    targetPercentage = target,
                    colorHex = colorHex,
                    creditHours = credits,
                    roomLocation = room.trim()
                )
            )
        }
    }

    fun updateSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: SubjectEntity) {
        viewModelScope.launch {
            repository.deleteSubject(subject)
        }
    }

    fun saveTimetableSlot(
        slotId: Long = 0L,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subjectName: String,
        room: String,
        explicitSubjectId: Long? = null
    ) {
        viewModelScope.launch {
            val matched = if (explicitSubjectId != null && explicitSubjectId > 0) {
                subjects.value.find { it.id == explicitSubjectId }
            } else {
                subjects.value.find { it.name.equals(subjectName, ignoreCase = true) }
            }
            repository.insertTimetableSlot(
                TimetableSlotEntity(
                    id = slotId,
                    dayOfWeek = dayOfWeek,
                    startTime = startTime,
                    endTime = endTime,
                    subjectName = subjectName,
                    subjectId = matched?.id ?: 0,
                    room = room
                )
            )
        }
    }

    fun deleteTimetableSlot(slot: TimetableSlotEntity) {
        viewModelScope.launch {
            repository.deleteTimetableSlot(slot)
        }
    }

    fun clearTimetableSlotsForDay(dayOfWeek: Int) {
        viewModelScope.launch {
            repository.clearTimetableSlotsForDay(dayOfWeek)
        }
    }

    fun addHoliday(name: String, dateString: String, isExam: Boolean) {
        viewModelScope.launch {
            repository.insertHoliday(
                HolidayEntity(
                    name = name.trim(),
                    dateString = dateString.trim(),
                    isExam = isExam
                )
            )
        }
    }

    fun deleteHoliday(holiday: HolidayEntity) {
        viewModelScope.launch {
            repository.deleteHoliday(holiday)
        }
    }

    fun importOfficialHolidays() {
        viewModelScope.launch {
            for ((hName, hDate, isExam) in GeminiAdvisorService.OFFICIAL_SEMESTER_HOLIDAYS) {
                repository.insertHoliday(
                    HolidayEntity(
                        name = hName,
                        dateString = hDate,
                        isExam = isExam
                    )
                )
            }
            _lastActionNotice.value = "Imported 12 official semester holidays into calendar"
        }
    }

    fun selectGeminiModel(modelId: String) {
        settingsManager.setSelectedModel(modelId)
    }

    fun clearCustomApiKey() {
        authManager.clearCustomGeminiApiKey()
    }

    private suspend fun executeAiAction(action: AiAction): String? {
        val currentSubs = repository.subjects.first()
        return when (action) {
            is AiAction.UpdateAttendance -> {
                var matched = currentSubs.find {
                    it.name.contains(action.subjectName, ignoreCase = true) ||
                    action.subjectName.contains(it.name, ignoreCase = true) ||
                    (it.code.isNotBlank() && it.code.contains(action.subjectName, ignoreCase = true))
                }
                if (matched == null) {
                    val words = action.subjectName.lowercase().split(" ", "-", "_").filter { it.length >= 3 }
                    matched = currentSubs.find { s -> words.any { s.name.lowercase().contains(it) } }
                }

                if (matched != null) {
                    if (action.setAttended != null && action.setTotal != null) {
                        repository.updateSubject(
                            matched.copy(
                                attendedClasses = action.setAttended.coerceAtLeast(0),
                                totalClasses = action.setTotal.coerceAtLeast(action.setAttended)
                            )
                        )
                        val newPct = if (action.setTotal > 0) (action.setAttended.toDouble() / action.setTotal) * 100.0 else 100.0
                        "Updated '${matched.name}' to ${action.setAttended}/${action.setTotal} (${String.format(Locale.US, "%.1f", newPct)}%)"
                    } else {
                        val status = action.status ?: "PRESENT"
                        repository.markAttendance(matched, status, strictSingleRegistration = strictSingleRegistration.value)
                        val newTotal = matched.totalClasses + 1
                        val newAtt = if (status == "PRESENT") matched.attendedClasses + 1 else matched.attendedClasses
                        val newPct = (newAtt.toDouble() / newTotal) * 100.0
                        "Marked '${matched.name}' as $status (${newAtt}/$newTotal • ${String.format(Locale.US, "%.1f", newPct)}%)"
                    }
                } else {
                    val newAttended = action.setAttended ?: 14
                    val newTotal = action.setTotal ?: (if (action.status == "ABSENT") 15 else 16)
                    val newCode = "CS-${(200..499).random()}"
                    val newSub = SubjectEntity(
                        name = action.subjectName,
                        code = newCode,
                        attendedClasses = newAttended,
                        totalClasses = newTotal,
                        targetPercentage = globalTarget.value,
                        colorHex = "#6366F1",
                        creditHours = 3,
                        roomLocation = "Room 302"
                    )
                    repository.insertSubject(newSub)
                    val newPct = if (newTotal > 0) (newAttended.toDouble() / newTotal) * 100.0 else 100.0
                    "Enrolled '${action.subjectName}' with $newAttended/$newTotal classes (${String.format(Locale.US, "%.1f", newPct)}%)"
                }
            }
            is AiAction.AddSubject -> {
                repository.insertSubject(
                    SubjectEntity(
                        name = action.name.trim(),
                        code = action.code.trim().uppercase(),
                        attendedClasses = action.attended,
                        totalClasses = action.total,
                        targetPercentage = action.target,
                        colorHex = action.colorHex,
                        creditHours = action.credits,
                        roomLocation = action.room.trim()
                    )
                )
                "Enrolled new course: '${action.name}' (${action.attended}/${action.total} classes)"
            }
            is AiAction.DeleteSubject -> {
                val matched = currentSubs.find {
                    it.name.contains(action.subjectName, ignoreCase = true)
                }
                if (matched != null) {
                    repository.deleteSubject(matched)
                    "Removed course '${matched.name}' and its records"
                } else null
            }
            is AiAction.UpdateTarget -> {
                setGlobalTarget(action.targetPercentage)
                "Updated university attendance criteria to ${action.targetPercentage.toInt()}%"
            }
            is AiAction.UpdateForecast -> {
                if (action.futureAttended != null) _simFutureAttended.value = action.futureAttended
                if (action.futureMissed != null) _simFutureMissed.value = action.futureMissed
                val parts = mutableListOf<String>()
                if (action.weeksRemaining != null) parts.add("${action.weeksRemaining} wks remaining")
                if (action.classesPerWeek != null) parts.add("${action.classesPerWeek} classes/wk")
                if (action.futureAttended != null) parts.add("+${action.futureAttended} attended")
                if (action.futureMissed != null) parts.add("+${action.futureMissed} missed")
                "Updated semester forecast simulator (${parts.joinToString(", ")})"
            }
            is AiAction.AddTimetableSlot -> {
                saveTimetableSlot(
                    dayOfWeek = action.dayOfWeek,
                    startTime = action.startTime,
                    endTime = action.endTime,
                    subjectName = action.subjectName,
                    room = action.room
                )
                val dayName = when (action.dayOfWeek) {
                    1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"; 6 -> "Sat"; else -> "Sun"
                }
                "Scheduled ${action.subjectName} on $dayName (${action.startTime} - ${action.endTime})"
            }
            is AiAction.ClearTimetableDay -> {
                repository.clearTimetableSlotsForDay(action.dayOfWeek)
                "Cleared all timetable classes for day ${action.dayOfWeek}"
            }
            is AiAction.AddHoliday -> {
                repository.insertHoliday(
                    HolidayEntity(
                        name = action.name,
                        dateString = action.dateString,
                        isExam = action.isExam
                    )
                )
                "Added calendar event: '${action.name}' (${action.dateString})"
            }
            is AiAction.ImportOfficialHolidays -> {
                importOfficialHolidays()
                "Imported 12 official semester holidays & exam dates into calendar"
            }
            is AiAction.DeleteHoliday -> {
                val currentHolidays = repository.holidays.first()
                val matched = currentHolidays.find {
                    it.name.contains(action.nameOrDate, ignoreCase = true) || it.dateString == action.nameOrDate
                }
                if (matched != null) {
                    repository.deleteHoliday(matched)
                    "Removed holiday '${matched.name}'"
                } else null
            }
        }
    }

    fun sendGeminiChatQuery(
        prompt: String,
        imageUri: String? = null,
        imageBitmap: Bitmap? = null
    ) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isBlank() && imageBitmap == null) return
        if (_isQueryingModel.value) return

        val profile = studentProfile.value

        val effectivePrompt = if (cleanPrompt.isNotBlank()) {
            cleanPrompt
        } else {
            "Please analyze this attached screenshot/image, extract all courses, attendance statistics, or timetable slots, and update my attendance tracker!"
        }

        val userMsg = GeminiChatMessage(
            sender = "USER",
            text = effectivePrompt,
            imageUri = imageUri
        )
        _geminiChatMessages.value = _geminiChatMessages.value + userMsg
        _isQueryingModel.value = true

        viewModelScope.launch {
            val modelId = selectedGeminiModel.value
            val currentSubs = subjects.value
            val target = globalTarget.value
            val stats = overallStats.value
            val currentHolidays = holidays.value
            val currentSlots = timetableSlots.value

            val sbSummary = StringBuilder()
            sbSummary.append("Overall Attendance: ${String.format(Locale.US, "%.1f", stats.overallPercentage)}% (Target: ${target.toInt()}%)\n")
            for (s in currentSubs) {
                val calc = AttendanceCalculator.calculate(s.attendedClasses, s.totalClasses, target, warningMargin.value)
                sbSummary.append("• ${s.name} (${s.code}): ${s.attendedClasses}/${s.totalClasses} (${String.format(Locale.US, "%.1f", calc.percentage)}%) - Bunks: ${calc.safeBunks}, Need: ${calc.classesNeeded}\n")
            }
            val sbHolidays = StringBuilder()
            for (h in currentHolidays) {
                sbHolidays.append("• ${h.name} on ${h.dateString} (${if (h.isExam) "Exam" else "Holiday"})\n")
            }
            val sbSlots = StringBuilder()
            for (slot in currentSlots) {
                sbSlots.append("• Day ${slot.dayOfWeek}: ${slot.subjectName} (${slot.startTime}-${slot.endTime})\n")
            }

            val imageBase64 = if (imageBitmap != null) {
                ImageUtil.bitmapToBase64(imageBitmap)
            } else null

            val result = geminiService.generatePrompt(
                modelId = modelId,
                prompt = effectivePrompt,
                imageBase64 = imageBase64,
                userEmail = null,
                userDisplayName = profile.name,
                attendanceContext = sbSummary.toString(),
                holidaysContext = sbHolidays.toString(),
                timetableContext = sbSlots.toString(),
                forecastContext = "Simulating: ${simFutureAttended.value} attended, ${simFutureMissed.value} missed",
                customApiKey = authManager.getCustomGeminiApiKey()
            )

            val aiResponse = result.getOrNull()
            val executedNotes = mutableListOf<String>()

            if (autoApplyAiActions.value) {
                if (aiResponse != null) {
                    for (action in aiResponse.actions) {
                        val note = executeAiAction(action)
                        if (note != null) executedNotes.add(note)
                    }
                }
                if (executedNotes.isEmpty()) {
                    val intentActions = AgentIntentEngine.extractActions(effectivePrompt, currentSubs, imageBitmap != null)
                    for (action in intentActions) {
                        val note = executeAiAction(action)
                        if (note != null) executedNotes.add(note)
                    }
                }
            }

            if (executedNotes.isNotEmpty()) {
                _lastActionNotice.value = executedNotes.joinToString(" • ")
            }

            val replyText = aiResponse?.displayText ?: "Autonomous actions processed for Gemini $modelId."

            _geminiChatMessages.value = _geminiChatMessages.value + GeminiChatMessage(
                sender = "GEMINI",
                text = replyText,
                modelId = modelId,
                executedActions = executedNotes
            )
            _isQueryingModel.value = false
        }
    }

    private fun getInitialDayOfWeek(): Int {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }
}
