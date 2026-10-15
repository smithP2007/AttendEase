package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.SubjectEntity
import com.example.data.remote.GeminiModelInfo
import com.example.data.settings.AccentTheme
import com.example.data.settings.StudentProfile
import com.example.data.settings.ThemeMode
import com.example.ui.OverallAttendanceStats
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.util.DisplayRateHelper
import com.example.util.ExportManager
import com.example.util.TouchFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    dynamicColorEnabled: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
    accentTheme: AccentTheme,
    onAccentThemeChange: (AccentTheme) -> Unit,
    compactCardView: Boolean,
    onCompactCardViewChange: (Boolean) -> Unit,
    maxRefreshRateEnabled: Boolean,
    onMaxRefreshRateChange: (Boolean) -> Unit,
    lowLatencyHaptics: Boolean,
    onLowLatencyHapticsChange: (Boolean) -> Unit,
    gpuHardwareLayers: Boolean,
    onGpuHardwareLayersChange: (Boolean) -> Unit,
    targetPercentage: Double,
    onTargetPercentageChange: (Double) -> Unit,
    warningMargin: Double,
    onWarningMarginChange: (Double) -> Unit,
    strictSingleRegistration: Boolean,
    onStrictSingleRegistrationChange: (Boolean) -> Unit,
    includeWeekendDays: Boolean,
    onIncludeWeekendDaysChange: (Boolean) -> Unit,
    studentProfile: StudentProfile,
    onUpdateProfile: (name: String, roll: String, uni: String, sem: String) -> Unit,
    availableModels: List<GeminiModelInfo>,
    selectedModelId: String,
    onSelectModel: (String) -> Unit,
    customApiKey: String,
    onSaveApiKey: (String) -> Unit,
    onTestApiKey: (String, (Boolean, String) -> Unit) -> Unit,
    autoApplyAiActions: Boolean,
    onAutoApplyAiActionsChange: (Boolean) -> Unit,
    subjects: List<SubjectEntity>,
    overallStats: OverallAttendanceStats,
    onResetTodayRecords: () -> Unit,
    onClearAllRecords: () -> Unit,
    onRestoreDemoData: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayInfo = remember(context) { DisplayRateHelper.getDisplayInfo(context) }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showClearRecordsConfirm by remember { mutableStateOf(false) }
    var showRestoreDemoConfirm by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }

    val isPeakPipelineActive = maxRefreshRateEnabled || gpuHardwareLayers
    val statusBannerColor = if (isPeakPipelineActive) StatusSafe else MaterialTheme.colorScheme.primary
    val activeRateLabel = if (maxRefreshRateEnabled) {
        "${displayInfo.maxRefreshRate.toInt()} Hz Peak"
    } else {
        "60 Hz Standard"
    }
    val statusSummaryText = buildString {
        append(
            if (maxRefreshRateEnabled) {
                "Display: ${displayInfo.currentRefreshRate.toInt()} Hz Peak Locked"
            } else {
                "Display: Standard Refresh Rate"
            }
        )
        append(" • ")
        append(
            if (gpuHardwareLayers) {
                "GPU Layers Active (Vulkan/SkiaGL)"
            } else {
                "GPU Texture Layers Off"
            }
        )
        append(" • ")
        append(
            if (lowLatencyHaptics) {
                "Low-Latency Haptics On"
            } else {
                "Touch Haptics Muted"
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Settings",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Performance, Display & Material You",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            TouchFeedback.tick(context)
                            onNavigateBack()
                        },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        TouchFeedback.tick(context)
                        showHelpDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Regulations Guide",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("settings_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 0: High Refresh Rate & Low-Latency Hardware Rendering
            item {
                SettingsSectionHeader(
                    title = "Display & Hardware Acceleration",
                    icon = Icons.Default.Speed
                )
            }
            item {
                SettingsCard {
                    // Live Hardware & Display Status Banner reflecting actual switch states
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = statusBannerColor.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, statusBannerColor.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("display_hardware_status_banner")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = statusBannerColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (maxRefreshRateEnabled) "Max Display Refresh Rate" else "Display Rendering Pipeline",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBannerColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = activeRateLabel,
                                        color = statusBannerColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = statusSummaryText,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle: Max Refresh Rate Support
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Maximum Display Refresh Rate",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Lock display to peak 90Hz/120Hz/144Hz for butter-smooth scrolling",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = maxRefreshRateEnabled,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onMaxRefreshRateChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle: Low-Latency Touch Feedback & Haptics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Low-Latency Touch Haptics",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Instant tactile acknowledgement for button taps and attendance marks",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = lowLatencyHaptics,
                            onCheckedChange = {
                                onLowLatencyHapticsChange(it)
                                if (it) TouchFeedback.click(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle: GPU Hardware Layer Acceleration
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPU Hardware Acceleration",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Offload gauge animations and list cards to hardware texture buffers",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = gpuHardwareLayers,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onGpuHardwareLayersChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section 1: Appearance & Material You Design
            item {
                SettingsSectionHeader(
                    title = "Appearance & Material You",
                    icon = Icons.Default.Palette
                )
            }
            item {
                SettingsCard {
                    // Theme Mode Segmented Selector
                    Text(
                        text = "Theme Display Mode",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Pair(ThemeMode.SYSTEM, "System"),
                            Pair(ThemeMode.LIGHT, "Light"),
                            Pair(ThemeMode.DARK, "Dark"),
                            Pair(ThemeMode.AMOLED, "AMOLED")
                        ).forEach { (mode, label) ->
                            val isSelected = themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        TouchFeedback.tick(context)
                                        onThemeModeChange(mode)
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Material You Dynamic Color Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dynamic Color (Android 12+)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Extract accent tones from your wallpaper",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = dynamicColorEnabled,
                            onCheckedChange = { enabled ->
                                TouchFeedback.click(context)
                                onDynamicColorChange(enabled)
                                if (enabled) {
                                    onAccentThemeChange(AccentTheme.DYNAMIC)
                                } else if (accentTheme == AccentTheme.DYNAMIC) {
                                    onAccentThemeChange(AccentTheme.INDIGO)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Accent Palette Picker
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Accent Color Palette",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (dynamicColorEnabled && accentTheme == AccentTheme.DYNAMIC) {
                                "Wallpaper Dynamic Active"
                            } else {
                                accentTheme.label
                            },
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccentTheme.entries.forEach { accent ->
                            val isSelected = if (dynamicColorEnabled) {
                                accent == AccentTheme.DYNAMIC
                            } else {
                                accentTheme == accent
                            }
                            val previewColor = if (accent == AccentTheme.DYNAMIC && dynamicColorEnabled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color(android.graphics.Color.parseColor(accent.hexPreview))
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(previewColor, CircleShape)
                                    .clickable {
                                        TouchFeedback.tick(context)
                                        onAccentThemeChange(accent)
                                        onDynamicColorChange(accent == AccentTheme.DYNAMIC)
                                    }
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Compact Card View Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Compact Card Mode",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Denser layout to view more courses on screen",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = compactCardView,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onCompactCardViewChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section 2: Academic & Attendance Criteria
            item {
                SettingsSectionHeader(
                    title = "Academic Rules & Criteria",
                    icon = Icons.Default.Tune
                )
            }
            item {
                SettingsCard {
                    // Target Percentage Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Minimum Attendance Target",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Mandated exam eligibility threshold",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "${targetPercentage.toInt()}%",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Slider(
                        value = targetPercentage.toFloat(),
                        onValueChange = {
                            TouchFeedback.tick(context)
                            onTargetPercentageChange(it.toDouble())
                        },
                        valueRange = 60f..90f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(60.0, 70.0, 75.0, 80.0, 85.0).forEach { preset ->
                            val isSelected = targetPercentage.toInt() == preset.toInt()
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        TouchFeedback.tick(context)
                                        onTargetPercentageChange(preset)
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text(
                                        text = "${preset.toInt()}%",
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Early Warning Margin Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Early Warning Buffer Margin",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Alert before dropping below target (+${warningMargin.toInt()}%)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "±${warningMargin.toInt()}%",
                                color = StatusWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = warningMargin.toFloat(),
                        onValueChange = {
                            TouchFeedback.tick(context)
                            onWarningMarginChange(it.toDouble())
                        },
                        valueRange = 2f..10f,
                        steps = 7,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusWarning,
                            activeTrackColor = StatusWarning,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Strict Single Registration Rule Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Strict Single Registration",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Prevent duplicate logging on the same calendar day",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = strictSingleRegistration,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onStrictSingleRegistrationChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Weekend Schedule Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Include Weekends in Schedule",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Show Saturday and Sunday in weekly timetable",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = includeWeekendDays,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onIncludeWeekendDaysChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section 3: Student Profile
            item {
                SettingsSectionHeader(
                    title = "Student Profile",
                    icon = Icons.Default.Person
                )
            }
            item {
                SettingsCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = studentProfile.name.take(2).uppercase(),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = studentProfile.name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${studentProfile.rollNo}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = studentProfile.university,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        IconButton(onClick = {
                            TouchFeedback.tick(context)
                            showProfileDialog = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Section 4: AI & Gemini Engine
            item {
                SettingsSectionHeader(
                    title = "AI & Autonomous Engine",
                    icon = Icons.Default.AutoAwesome
                )
            }
            item {
                SettingsCard {
                    Text(
                        text = "Active Gemini Model",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    availableModels.forEach { model ->
                        val isSelected = model.id == selectedModelId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    TouchFeedback.tick(context)
                                    onSelectModel(model.id)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = model.displayName,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.surfaceContainer
                                        ) {
                                            Text(
                                                text = model.tag,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = model.description,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // API Key Settings
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Personal Gemini API Key",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (customApiKey.isNotBlank()) "Key configured • Quota active" else "Using default agent configuration",
                                color = if (customApiKey.isNotBlank()) StatusSafe else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                showApiKeyDialog = true
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (customApiKey.isNotBlank()) "Change" else "Configure")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-apply AI actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Apply AI Actions",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Directly execute timetable & attendance changes",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = autoApplyAiActions,
                            onCheckedChange = {
                                TouchFeedback.click(context)
                                onAutoApplyAiActionsChange(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Section 5: Data Management & Export
            item {
                SettingsSectionHeader(
                    title = "Data Management & Export",
                    icon = Icons.Default.Download
                )
            }
            item {
                SettingsCard {
                    // Export CSV
                    SettingsActionRow(
                        title = "Export CSV Spreadsheet",
                        subtitle = "Download all course metrics in .csv format",
                        icon = Icons.Default.Download,
                        onClick = {
                            TouchFeedback.click(context)
                            val csv = ExportManager.generateCsv(subjects, targetPercentage)
                            ExportManager.shareCsv(context, csv)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Share Statement
                    SettingsActionRow(
                        title = "Share Attendance Statement",
                        subtitle = "Send formatted report to advisor or faculty",
                        icon = Icons.Default.Share,
                        onClick = {
                            TouchFeedback.click(context)
                            val text = ExportManager.generateSummaryText(
                                subjects,
                                targetPercentage,
                                overallStats.overallPercentage,
                                studentProfile.name
                            )
                            ExportManager.shareText(context, text)
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Copy Summary
                    SettingsActionRow(
                        title = "Copy Summary to Clipboard",
                        subtitle = "Quick clipboard copy of current status",
                        icon = Icons.Default.ContentCopy,
                        onClick = {
                            TouchFeedback.tick(context)
                            val text = ExportManager.generateSummaryText(
                                subjects,
                                targetPercentage,
                                overallStats.overallPercentage,
                                studentProfile.name
                            )
                            ExportManager.copyToClipboard(context, text)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset Today's Marks
                    OutlinedButton(
                        onClick = {
                            TouchFeedback.tick(context)
                            onResetTodayRecords()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset Today's Attendance Marks")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Restore Demo Data
                    OutlinedButton(
                        onClick = {
                            TouchFeedback.tick(context)
                            showRestoreDemoConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore Sample University Courses")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Clear All Records
                    OutlinedButton(
                        onClick = {
                            TouchFeedback.heavy(context)
                            showClearRecordsConfirm = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusCritical),
                        border = BorderStroke(1.dp, StatusCritical.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Attendance Records")
                    }
                }
            }

            // App Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isPeakPipelineActive) {
                            "AttendEase • High Refresh Rate & GPU Accelerated"
                        } else {
                            "AttendEase • Standard Display & Power-Saving Mode"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = statusSummaryText,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showProfileDialog) {
        var pName by remember { mutableStateOf(studentProfile.name) }
        var pRoll by remember { mutableStateOf(studentProfile.rollNo) }
        var pUni by remember { mutableStateOf(studentProfile.university) }
        var pSem by remember { mutableStateOf(studentProfile.semester) }

        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("Edit Student Profile", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = pName,
                        onValueChange = { pName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pRoll,
                        onValueChange = { pRoll = it },
                        label = { Text("Roll / Student ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pUni,
                        onValueChange = { pUni = it },
                        label = { Text("University / College") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = pSem,
                        onValueChange = { pSem = it },
                        label = { Text("Semester & Department") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        TouchFeedback.click(context)
                        onUpdateProfile(pName, pRoll, pUni, pSem)
                        showProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Profile")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Configure Gemini API Key Dialog
    if (showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(customApiKey) }
        var isTesting by remember { mutableStateOf(false) }
        var testResultMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini API Key", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your Google AI Studio API key to power vision analysis and multimodal reasoning with your personal quota.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (keyInput.isNotBlank()) {
                                    TouchFeedback.tick(context)
                                    isTesting = true
                                    testResultMsg = "Testing..."
                                    onTestApiKey(keyInput) { success, msg ->
                                        isTesting = false
                                        testResultMsg = if (success) "✓ $msg" else "✗ $msg"
                                    }
                                }
                            },
                            enabled = !isTesting && keyInput.isNotBlank()
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            } else {
                                Text("Test Connection")
                            }
                        }
                        if (keyInput.isNotBlank()) {
                            TextButton(onClick = {
                                TouchFeedback.tick(context)
                                keyInput = ""
                                onSaveApiKey("")
                            }) {
                                Text("Clear", color = StatusCritical)
                            }
                        }
                    }
                    if (testResultMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = testResultMsg ?: "",
                            color = if (testResultMsg?.startsWith("✓") == true) StatusSafe else StatusCritical,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        TouchFeedback.click(context)
                        onSaveApiKey(keyInput)
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Clear Records Confirmation Dialog
    if (showClearRecordsConfirm) {
        AlertDialog(
            onDismissRequest = { showClearRecordsConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Clear All Records?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "This will reset all course counts (attended/total) to 0 and remove all attendance history. Enrolled courses will be preserved.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        TouchFeedback.heavy(context)
                        onClearAllRecords()
                        showClearRecordsConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusCritical),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecordsConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Restore Demo Confirmation Dialog
    if (showRestoreDemoConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreDemoConfirm = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Restore Sample Courses?", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "This will replace current data with the default university semester courses, timetable slots, and holidays.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        TouchFeedback.click(context)
                        onRestoreDemoData()
                        showRestoreDemoConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDemoConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Regulations Guide Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Academic Policy Guide", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "• 75% Rule: Most universities mandate ≥ 75% attendance in theory & laboratory courses to be eligible to sit for semester examinations.\n\n" +
                                "• Safe Bunk Math: Safe Bunks = floor(Attended / 0.75 - Total). Represents the exact number of consecutive lectures you can miss while keeping attendance above 75%.\n\n" +
                                "• Classes Needed: Needed = ceil((0.75 * Total - Attended) / 0.25). Shows exactly how many consecutive lectures you must attend without absence to recover.\n\n" +
                                "• Medical Condonation: Attendance between 65% and 75% often qualifies for exemption with official medical documentation.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
