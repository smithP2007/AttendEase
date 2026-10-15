package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import com.example.ui.theme.MotionTokens
import com.example.ui.theme.interactiveSpring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AttendanceRecordEntity
import com.example.data.db.SubjectEntity
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.util.AttendanceCalculator
import com.example.util.AttendanceStatus
import com.example.util.TouchFeedback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SubjectsScreen(
    subjects: List<SubjectEntity>,
    targetPercentage: Double,
    warningMargin: Double = 5.0,
    recentRecords: List<AttendanceRecordEntity>,
    onToggleAttendance: (SubjectEntity, String, String) -> Unit,
    onMarkAllPresent: (String) -> Unit,
    onClearAll: (String) -> Unit,
    onSubjectClick: (SubjectEntity) -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var calendarDate by remember { mutableStateOf(Calendar.getInstance()) }

    val dateKeyFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val displayDateFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }

    val selectedDateStr = dateKeyFormat.format(calendarDate.time)
    val todayDateStr = dateKeyFormat.format(Date())
    val isToday = selectedDateStr == todayDateStr

    val filteredSubjects by remember(subjects, searchQuery, selectedFilter, targetPercentage, warningMargin) {
        derivedStateOf {
            subjects.filter { subject ->
                val matchesSearch = subject.name.contains(searchQuery, ignoreCase = true) ||
                        subject.code.contains(searchQuery, ignoreCase = true)
                val res = AttendanceCalculator.calculate(subject.attendedClasses, subject.totalClasses, targetPercentage, warningMargin)
                val matchesFilter = when (selectedFilter) {
                    "DANGER" -> res.status != AttendanceStatus.SAFE
                    "SAFE" -> res.status == AttendanceStatus.SAFE
                    else -> true
                }
                matchesSearch && matchesFilter
            }
        }
    }

    val dateRecords = remember(recentRecords, selectedDateStr) {
        recentRecords.filter { it.dateString == selectedDateStr }
    }
    val markedCount = remember(subjects, dateRecords) {
        subjects.count { sub -> dateRecords.any { it.subjectId == sub.id } }
    }
    val presentCount = remember(dateRecords) { dateRecords.count { it.status == "PRESENT" } }
    val absentCount = remember(dateRecords) { dateRecords.count { it.status == "ABSENT" } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("subjects_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Date Selector & Batch Controls
        item(contentType = "DateSelector") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                        )
                    )
                ),
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Date Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                val c = Calendar.getInstance().apply {
                                    time = calendarDate.time
                                    add(Calendar.DAY_OF_YEAR, -1)
                                }
                                calendarDate = c
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isToday) StatusSafe.copy(alpha = 0.14f) else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                Text(
                                    text = if (isToday) "TODAY" else "LOGGED DATE",
                                    color = if (isToday) StatusSafe else MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = displayDateFormat.format(calendarDate.time),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                val c = Calendar.getInstance().apply {
                                    time = calendarDate.time
                                    add(Calendar.DAY_OF_YEAR, 1)
                                }
                                calendarDate = c
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Overview
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$markedCount of ${subjects.size} subjects logged",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$presentCount Present • $absentCount Absent • ${subjects.size - markedCount} Pending",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                            if (!isToday) {
                                OutlinedButton(
                                    onClick = {
                                        TouchFeedback.tick(context)
                                        calendarDate = Calendar.getInstance()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Today", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Batch Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                TouchFeedback.click(context)
                                onMarkAllPresent(selectedDateStr)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .testTag("mark_all_present_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = StatusSafe.copy(alpha = 0.1f),
                                contentColor = StatusSafe
                            ),
                            border = BorderStroke(1.dp, StatusSafe.copy(alpha = 0.4f))
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark All Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                onClearAll(selectedDateStr)
                            },
                            modifier = Modifier
                                .weight(0.9f)
                                .height(38.dp)
                                .testTag("clear_today_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Day", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Search & Filter Header
        item(contentType = "FilterHeader") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Courses (${subjects.size})",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            TouchFeedback.tick(context)
                            onAddSubjectClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("subjects_tab_add_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or code...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = {
                            TouchFeedback.tick(context)
                            selectedFilter = "ALL"
                        },
                        label = { Text("All (${subjects.size})") },
                        colors = filterChipColors()
                    )
                    FilterChip(
                        selected = selectedFilter == "DANGER",
                        onClick = {
                            TouchFeedback.tick(context)
                            selectedFilter = "DANGER"
                        },
                        label = {
                            val count = subjects.count {
                                AttendanceCalculator.calculate(it.attendedClasses, it.totalClasses, targetPercentage, warningMargin).status != AttendanceStatus.SAFE
                            }
                            Text("Danger ($count)")
                        },
                        colors = filterChipColors()
                    )
                    FilterChip(
                        selected = selectedFilter == "SAFE",
                        onClick = {
                            TouchFeedback.tick(context)
                            selectedFilter = "SAFE"
                        },
                        label = {
                            val count = subjects.count {
                                AttendanceCalculator.calculate(it.attendedClasses, it.totalClasses, targetPercentage, warningMargin).status == AttendanceStatus.SAFE
                            }
                            Text("Safe ($count)")
                        },
                        colors = filterChipColors()
                    )
                }
            }
        }

        // Subjects List with Toggle Buttons
        if (filteredSubjects.isEmpty()) {
            item(contentType = "EmptyState") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (subjects.isEmpty()) "No Courses Enrolled" else "No Matching Courses",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (subjects.isEmpty()) "Tap '+ Add Course' above to start tracking." else "Try clearing your search query.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(
                items = filteredSubjects,
                key = { it.id },
                contentType = { "SubjectToggleItem" }
            ) { subject ->
                val currentRecord = dateRecords.firstOrNull { it.subjectId == subject.id }
                SubjectToggleItem(
                    subject = subject,
                    targetPercentage = targetPercentage,
                    warningMargin = warningMargin,
                    currentStatus = currentRecord?.status,
                    onToggle = { newStatus ->
                        TouchFeedback.click(context)
                        onToggleAttendance(subject, newStatus, selectedDateStr)
                    },
                    onClick = {
                        TouchFeedback.tick(context)
                        onSubjectClick(subject)
                    },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun SubjectToggleItem(
    subject: SubjectEntity,
    targetPercentage: Double,
    warningMargin: Double = 5.0,
    currentStatus: String?,
    onToggle: (String) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cardInteractionSource = remember { MutableInteractionSource() }
    val presentInteractionSource = remember { MutableInteractionSource() }
    val absentInteractionSource = remember { MutableInteractionSource() }

    val calc = AttendanceCalculator.calculate(
        attended = subject.attendedClasses,
        total = subject.totalClasses,
        targetPercentage = targetPercentage,
        warningMargin = warningMargin
    )
    val progress = (calc.percentage / 100.0).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = MotionTokens.ProgressSpring,
        label = "SubjectToggleProgress"
    )

    val isPresent = currentStatus == "PRESENT"
    val isAbsent = currentStatus == "ABSENT"

    val statusColor = when (calc.status) {
        AttendanceStatus.SAFE -> StatusSafe
        AttendanceStatus.WARNING -> StatusWarning
        AttendanceStatus.CRITICAL -> StatusCritical
    }

    val topGlowColor by animateColorAsState(
        targetValue = when {
            isPresent -> StatusSafe
            isAbsent -> StatusCritical
            calc.status == AttendanceStatus.CRITICAL -> StatusCritical
            else -> statusColor
        },
        animationSpec = MotionTokens.ColorSpring,
        label = "SubjectToggleBorder"
    )

    val borderBrush = Brush.linearGradient(
        colors = listOf(
            topGlowColor.copy(alpha = 0.48f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            topGlowColor.copy(alpha = 0.14f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_toggle_card_${subject.id}")
            .interactiveSpring(cardInteractionSource, pressedScale = 0.98f)
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(22.dp)
            }
            .clickable(
                interactionSource = cardInteractionSource,
                indication = ripple(bounded = true)
            ) {
                TouchFeedback.tick(context)
                onClick()
            },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, borderBrush),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            topGlowColor.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        center = Offset(80f, 60f),
                        radius = 320f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (subject.code.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(7.dp),
                                    color = statusColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.35f)),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = subject.code,
                                        color = statusColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            if (subject.roomLocation.isNotBlank()) {
                                Text(
                                    text = "• ${subject.roomLocation}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subject.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%.1f", calc.percentage)}%",
                                color = statusColor,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "${subject.attendedClasses}/${subject.totalClasses} held",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(34.dp)
                        ) {
                            val ringTrack = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                            val diamondColors = remember(statusColor, calc.percentage, targetPercentage) {
                                if (calc.percentage >= targetPercentage) {
                                    listOf(
                                        Color(0xFF00E676), // Bright Emerald
                                        Color(0xFF00E5FF), // Diamond Aqua highlight
                                        Color(0xFF00B0FF)  // Electric Blue
                                    )
                                } else {
                                    listOf(
                                        statusColor.copy(alpha = 0.7f),
                                        statusColor,
                                        statusColor.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            val ringLinearBrush = remember(diamondColors) {
                                Brush.linearGradient(
                                    colors = diamondColors,
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                )
                            }

                            Canvas(
                                modifier = Modifier.size(34.dp)
                            ) {
                                val sw = 3.5.dp.toPx()
                                val d = size.minDimension - sw
                                val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                                drawArc(
                                    color = ringTrack,
                                    startAngle = 270f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    topLeft = tl,
                                    size = Size(d, d),
                                    style = Stroke(width = sw)
                                )
                                if (animatedProgress > 0.005f) {
                                    val sweep = animatedProgress * 360f
                                    drawArc(
                                        brush = ringLinearBrush,
                                        startAngle = 270f,
                                        sweepAngle = sweep,
                                        useCenter = false,
                                        topLeft = tl,
                                        size = Size(d, d),
                                        style = Stroke(width = sw, cap = StrokeCap.Round)
                                    )
                                }
                            }
                            Text(
                                text = "${calc.percentage.toInt()}%",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(12.dp))

                val presentBtnBg by animateColorAsState(
                    targetValue = if (isPresent) StatusSafe else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = MotionTokens.ColorSpring,
                    label = "TogglePresentBg"
                )
                val absentBtnBg by animateColorAsState(
                    targetValue = if (isAbsent) StatusCritical else MaterialTheme.colorScheme.surfaceContainerHigh,
                    animationSpec = MotionTokens.ColorSpring,
                    label = "ToggleAbsentBg"
                )

                // Dual Toggle Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            TouchFeedback.click(context)
                            onToggle("PRESENT")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .interactiveSpring(presentInteractionSource, pressedScale = 0.94f)
                            .testTag("toggle_present_${subject.id}"),
                        interactionSource = presentInteractionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = presentBtnBg,
                            contentColor = if (isPresent) Color(0xFF041311) else StatusSafe
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPresent) StatusSafe else StatusSafe.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Present",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {
                            TouchFeedback.click(context)
                            onToggle("ABSENT")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .interactiveSpring(absentInteractionSource, pressedScale = 0.94f)
                            .testTag("toggle_absent_${subject.id}"),
                        interactionSource = absentInteractionSource,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = absentBtnBg,
                            contentColor = if (isAbsent) Color.White else StatusCritical
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isAbsent) StatusCritical else StatusCritical.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Absent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
