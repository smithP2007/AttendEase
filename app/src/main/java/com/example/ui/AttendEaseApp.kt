package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.example.ui.theme.MotionTokens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.SubjectEntity
import com.example.data.db.TimetableSlotEntity
import com.example.ui.components.AddEditSubjectDialog
import com.example.ui.components.AddTimetableSlotDialog
import com.example.ui.components.SubjectDetailSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GeminiApiKeySheet
import com.example.ui.screens.GeminiHubScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SimulatorScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.util.TouchFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendEaseApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val globalTarget by viewModel.globalTarget.collectAsStateWithLifecycle()
    val warningMargin by viewModel.warningMargin.collectAsStateWithLifecycle()
    val compactCardView by viewModel.compactCardView.collectAsStateWithLifecycle()
    val maxRefreshRateEnabled by viewModel.maxRefreshRateEnabled.collectAsStateWithLifecycle()
    val lowLatencyHaptics by viewModel.lowLatencyHaptics.collectAsStateWithLifecycle()
    val gpuHardwareLayers by viewModel.gpuHardwareLayers.collectAsStateWithLifecycle()
    val strictSingleRegistration by viewModel.strictSingleRegistration.collectAsStateWithLifecycle()
    val includeWeekendDays by viewModel.includeWeekendDays.collectAsStateWithLifecycle()
    val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val autoApplyAiActions by viewModel.autoApplyAiActions.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsStateWithLifecycle()
    val accentTheme by viewModel.accentTheme.collectAsStateWithLifecycle()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val timetableSlots by viewModel.timetableSlots.collectAsStateWithLifecycle()
    val holidays by viewModel.holidays.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val futureAttended by viewModel.simFutureAttended.collectAsStateWithLifecycle()
    val futureMissed by viewModel.simFutureMissed.collectAsStateWithLifecycle()
    val recentRecords by viewModel.recentRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val lastActionNotice by viewModel.lastActionNotice.collectAsStateWithLifecycle()

    val availableModels = viewModel.availableGeminiModels
    val selectedModelId by viewModel.selectedGeminiModel.collectAsStateWithLifecycle()
    val chatMessages by viewModel.geminiChatMessages.collectAsStateWithLifecycle()
    val isQueryingModel by viewModel.isQueryingModel.collectAsStateWithLifecycle()

    // Dialog & Sheet States
    var showApiKeySheet by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<SubjectEntity?>(null) }
    var selectedSubjectDetail by remember { mutableStateOf<SubjectEntity?>(null) }
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var editingSlot by remember { mutableStateOf<TimetableSlotEntity?>(null) }

    if (isSettingsOpen) {
        BackHandler {
            viewModel.closeSettings()
        }
        SettingsScreen(
            themeMode = themeMode,
            onThemeModeChange = { viewModel.setThemeMode(it) },
            dynamicColorEnabled = dynamicColorEnabled,
            onDynamicColorChange = { viewModel.setDynamicColorEnabled(it) },
            accentTheme = accentTheme,
            onAccentThemeChange = { viewModel.setAccentTheme(it) },
            compactCardView = compactCardView,
            onCompactCardViewChange = { viewModel.setCompactCardView(it) },
            maxRefreshRateEnabled = maxRefreshRateEnabled,
            onMaxRefreshRateChange = { viewModel.setMaxRefreshRateEnabled(it) },
            lowLatencyHaptics = lowLatencyHaptics,
            onLowLatencyHapticsChange = { viewModel.setLowLatencyHaptics(it) },
            gpuHardwareLayers = gpuHardwareLayers,
            onGpuHardwareLayersChange = { viewModel.setGpuHardwareLayers(it) },
            targetPercentage = globalTarget,
            onTargetPercentageChange = { viewModel.setGlobalTarget(it) },
            warningMargin = warningMargin,
            onWarningMarginChange = { viewModel.setWarningMargin(it) },
            strictSingleRegistration = strictSingleRegistration,
            onStrictSingleRegistrationChange = { viewModel.setStrictSingleRegistration(it) },
            includeWeekendDays = includeWeekendDays,
            onIncludeWeekendDaysChange = { viewModel.setIncludeWeekendDays(it) },
            studentProfile = studentProfile,
            onUpdateProfile = { name, roll, uni, sem ->
                viewModel.updateStudentProfile(name, roll, uni, sem)
            },
            availableModels = availableModels,
            selectedModelId = selectedModelId,
            onSelectModel = { viewModel.selectGeminiModel(it) },
            customApiKey = customApiKey,
            onSaveApiKey = { viewModel.setCustomApiKey(it) },
            onTestApiKey = { key, callback ->
                viewModel.testGeminiApiKey(key, callback)
            },
            autoApplyAiActions = autoApplyAiActions,
            onAutoApplyAiActionsChange = { viewModel.setAutoApplyAiActions(it) },
            subjects = subjects,
            overallStats = overallStats,
            onResetTodayRecords = { viewModel.resetTodayRecords() },
            onClearAllRecords = { viewModel.clearAllRecords() },
            onRestoreDemoData = { viewModel.restoreDemoData() },
            onNavigateBack = { viewModel.closeSettings() }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AttendEase",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${studentProfile.name} • ${studentProfile.semester}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                },
                actions = {
                    // Average Percentage Pill
                    val isOverallSafe = overallStats.overallPercentage >= globalTarget
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isOverallSafe) StatusSafe.copy(alpha = 0.12f) else StatusCritical.copy(alpha = 0.12f),
                        border = BorderStroke(
                            1.dp,
                            if (isOverallSafe) StatusSafe.copy(alpha = 0.35f) else StatusCritical.copy(alpha = 0.35f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(
                                        if (isOverallSafe) StatusSafe else StatusCritical,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", overallStats.overallPercentage)}% avg",
                                color = if (isOverallSafe) StatusSafe else StatusCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Gemini API Key Pill
                    val isKeyActive = customApiKey.isNotBlank()
                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = if (isKeyActive) StatusSafe.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(
                            1.dp,
                            if (isKeyActive) StatusSafe.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .height(30.dp)
                            .clickable {
                                TouchFeedback.tick(context)
                                showApiKeySheet = true
                            }
                            .testTag("topbar_gemini_api_key")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 0.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "Gemini API Key",
                                tint = if (isKeyActive) StatusSafe else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isKeyActive) "API Key" else "Add Key",
                                color = if (isKeyActive) StatusSafe else MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Prominent Settings Button
                    IconButton(
                        onClick = {
                            TouchFeedback.tick(context)
                            viewModel.openSettings()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("topbar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                tonalElevation = 3.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        TouchFeedback.tick(context)
                        viewModel.selectTab(0)
                    },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors(),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        TouchFeedback.tick(context)
                        viewModel.selectTab(1)
                    },
                    icon = { Icon(Icons.Default.School, contentDescription = "Courses") },
                    label = { Text("Courses", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors(),
                    modifier = Modifier.testTag("nav_tab_subjects")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        TouchFeedback.tick(context)
                        viewModel.selectTab(2)
                    },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Schedule") },
                    label = { Text("Schedule", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors(),
                    modifier = Modifier.testTag("nav_tab_schedule")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = {
                        TouchFeedback.tick(context)
                        viewModel.selectTab(3)
                    },
                    icon = { Icon(Icons.Default.Calculate, contentDescription = "Calculator") },
                    label = { Text("Calculator", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors(),
                    modifier = Modifier.testTag("nav_tab_calculator")
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = {
                        TouchFeedback.tick(context)
                        viewModel.selectTab(4)
                    },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Gemini AI") },
                    label = { Text("Gemini AI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = navBarItemColors(),
                    modifier = Modifier.testTag("nav_tab_gemini")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally(
                        animationSpec = MotionTokens.SlideTransitionSpring,
                        initialOffsetX = { fullWidth -> (fullWidth * 0.15f * direction).toInt() }
                    ) + fadeIn(animationSpec = MotionTokens.ScreenTransitionSpring)) togetherWith
                            (slideOutHorizontally(
                                animationSpec = MotionTokens.SlideTransitionSpring,
                                targetOffsetX = { fullWidth -> (-fullWidth * 0.15f * direction).toInt() }
                            ) + fadeOut(animationSpec = MotionTokens.ScreenTransitionSpring))
                },
                label = "ScreenTransition"
            ) { tab ->
                when (tab) {
                    0 -> DashboardScreen(
                        stats = overallStats,
                        subjects = subjects,
                        targetPercentage = globalTarget,
                        warningMargin = warningMargin,
                        isCompact = compactCardView,
                        recentRecords = recentRecords,
                        onMarkPresent = { subject ->
                            viewModel.markAttendance(subject, "PRESENT")
                        },
                        onMarkAbsent = { subject ->
                            viewModel.markAttendance(subject, "ABSENT")
                        },
                        onSubjectClick = { subject ->
                            selectedSubjectDetail = subject
                        },
                        onAddSubjectClick = {
                            showAddSubjectDialog = true
                        }
                    )
                    1 -> SubjectsScreen(
                        subjects = subjects,
                        targetPercentage = globalTarget,
                        warningMargin = warningMargin,
                        recentRecords = recentRecords,
                        onToggleAttendance = { subject, status, dateStr ->
                            viewModel.toggleAttendanceForDate(subject, status, dateStr)
                        },
                        onMarkAllPresent = { dateStr ->
                            viewModel.markAllPresentForDate(subjects, dateStr)
                        },
                        onClearAll = { dateStr ->
                            viewModel.clearAllForDate(subjects, dateStr)
                        },
                        onSubjectClick = { subject ->
                            selectedSubjectDetail = subject
                        },
                        onAddSubjectClick = {
                            showAddSubjectDialog = true
                        }
                    )
                    2 -> TimetableScreen(
                        slots = timetableSlots,
                        subjects = subjects,
                        recentRecords = recentRecords,
                        selectedDay = selectedDay,
                        includeWeekends = includeWeekendDays,
                        onSelectDay = { viewModel.selectDay(it) },
                        onMarkPresentForSlot = { s ->
                            viewModel.markAttendance(s, "PRESENT")
                        },
                        onMarkAbsentForSlot = { s ->
                            viewModel.markAttendance(s, "ABSENT")
                        },
                        onDeleteSlot = { slot ->
                            viewModel.deleteTimetableSlot(slot)
                        },
                        onEditSlot = { slot ->
                            editingSlot = slot
                        },
                        onClearDay = { day ->
                            viewModel.clearTimetableSlotsForDay(day)
                        },
                        onAddSlotClick = {
                            showAddSlotDialog = true
                        }
                    )
                    3 -> SimulatorScreen(
                        stats = overallStats,
                        subjects = subjects,
                        targetPercentage = globalTarget,
                        onTargetChange = { viewModel.setGlobalTarget(it) },
                        futureAttended = futureAttended,
                        onFutureAttendedChange = { viewModel.setSimFutureAttended(it) },
                        futureMissed = futureMissed,
                        onFutureMissedChange = { viewModel.setSimFutureMissed(it) },
                        holidays = holidays,
                        onAddHoliday = { name, date, isExam ->
                            viewModel.addHoliday(name, date, isExam)
                        },
                        onDeleteHoliday = { holiday ->
                            viewModel.deleteHoliday(holiday)
                        },
                        onImportOfficialHolidays = {
                            viewModel.importOfficialHolidays()
                        }
                    )
                    4 -> GeminiHubScreen(
                        customApiKey = customApiKey,
                        availableModels = availableModels,
                        selectedModelId = selectedModelId,
                        chatMessages = chatMessages,
                        isQuerying = isQueryingModel,
                        onSelectModel = { modelId ->
                            viewModel.selectGeminiModel(modelId)
                        },
                        onSendQuery = { prompt, imageUri, imageBitmap ->
                            viewModel.sendGeminiChatQuery(prompt, imageUri, imageBitmap)
                        },
                        onNavigateToApiKey = {
                            showApiKeySheet = true
                        },
                        onNavigateToTab = { tabIndex ->
                            viewModel.selectTab(tabIndex)
                        }
                    )
                }
            }

            // Floating Action Execution Notification Banner
            androidx.compose.animation.AnimatedVisibility(
                visible = lastActionNotice != null,
                enter = slideInVertically(
                    animationSpec = MotionTokens.SlideTransitionSpring,
                    initialOffsetY = { -it }
                ) + fadeIn(animationSpec = MotionTokens.ScreenTransitionSpring),
                exit = slideOutVertically(
                    animationSpec = MotionTokens.SlideTransitionSpring,
                    targetOffsetY = { -it }
                ) + fadeOut(animationSpec = MotionTokens.ScreenTransitionSpring),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, StatusSafe),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = null,
                            tint = StatusSafe,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lastActionNotice ?: "",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                viewModel.clearActionNotice()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Subject Dialog
    if (showAddSubjectDialog) {
        AddEditSubjectDialog(
            defaultTarget = globalTarget,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { name, code, attended, total, target, color, credits, room ->
                viewModel.addSubject(name, code, attended, total, target, color, credits, room)
                showAddSubjectDialog = false
            }
        )
    }

    // Edit Subject Dialog
    editingSubject?.let { subject ->
        AddEditSubjectDialog(
            initialSubject = subject,
            defaultTarget = globalTarget,
            onDismiss = { editingSubject = null },
            onSave = { name, code, attended, total, target, color, credits, room ->
                viewModel.updateSubject(
                    subject.copy(
                        name = name,
                        code = code,
                        attendedClasses = attended,
                        totalClasses = total,
                        targetPercentage = target,
                        colorHex = color,
                        creditHours = credits,
                        roomLocation = room
                    )
                )
                editingSubject = null
                selectedSubjectDetail = null
            }
        )
    }

    // Subject Detail & Logs Sheet
    selectedSubjectDetail?.let { subject ->
        val currentSubjectState = subjects.find { it.id == subject.id } ?: subject
        val subjectRecords = allRecords.filter { it.subjectId == subject.id }
        SubjectDetailSheet(
            subject = currentSubjectState,
            targetPercentage = globalTarget,
            warningMargin = warningMargin,
            records = subjectRecords,
            onDismiss = { selectedSubjectDetail = null },
            onEdit = {
                editingSubject = currentSubjectState
            },
            onDelete = {
                viewModel.deleteSubject(currentSubjectState)
                selectedSubjectDetail = null
            },
            onUndoRecord = { record ->
                viewModel.undoRecord(record, currentSubjectState)
            },
            onToggleDate = { dateStr ->
                viewModel.cycleAttendanceForDate(currentSubjectState, dateStr)
            }
        )
    }

    // Add Timetable Slot Dialog
    if (showAddSlotDialog) {
        AddTimetableSlotDialog(
            initialDayOfWeek = selectedDay,
            subjects = subjects,
            onDismiss = { showAddSlotDialog = false },
            onSave = { slotId, day, start, end, sName, room, sId ->
                viewModel.saveTimetableSlot(
                    slotId = 0L,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    subjectName = sName,
                    room = room,
                    explicitSubjectId = sId
                )
                showAddSlotDialog = false
            }
        )
    }

    // Edit Timetable Slot Dialog
    editingSlot?.let { slot ->
        AddTimetableSlotDialog(
            initialSlot = slot,
            initialDayOfWeek = slot.dayOfWeek,
            subjects = subjects,
            onDismiss = { editingSlot = null },
            onSave = { slotId, day, start, end, sName, room, sId ->
                viewModel.saveTimetableSlot(
                    slotId = slot.id,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    subjectName = sName,
                    room = room,
                    explicitSubjectId = sId
                )
                editingSlot = null
            }
        )
    }

    // Gemini API Key Modal Sheet
    if (showApiKeySheet) {
        ModalBottomSheet(
            onDismissRequest = { showApiKeySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            GeminiApiKeySheet(
                customApiKey = customApiKey,
                selectedModelId = selectedModelId,
                availableModels = availableModels,
                onSelectModel = { viewModel.selectGeminiModel(it) },
                autoApplyAiActions = autoApplyAiActions,
                onAutoApplyAiActionsChange = { viewModel.setAutoApplyAiActions(it) },
                onSaveApiKey = { key ->
                    viewModel.setCustomApiKey(key)
                },
                onTestApiKey = { key, callback ->
                    viewModel.testGeminiApiKey(key, callback)
                },
                onClearApiKey = {
                    viewModel.clearCustomApiKey()
                },
                onDismiss = {
                    showApiKeySheet = false
                }
            )
        }
    }
}

@Composable
private fun navBarItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
