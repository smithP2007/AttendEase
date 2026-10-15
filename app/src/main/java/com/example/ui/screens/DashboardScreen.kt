package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.example.data.db.AttendanceRecordEntity
import com.example.data.db.SubjectEntity
import com.example.ui.OverallAttendanceStats
import com.example.ui.components.AttendanceGauge
import com.example.ui.components.AttendanceSummaryCards
import com.example.ui.components.SubjectCard
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.util.AttendanceCalculator
import com.example.util.AttendanceStatus
import com.example.util.TouchFeedback
import java.text.SimpleDateFormat
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    stats: OverallAttendanceStats,
    subjects: List<SubjectEntity>,
    targetPercentage: Double,
    warningMargin: Double = 5.0,
    isCompact: Boolean = false,
    recentRecords: List<AttendanceRecordEntity>,
    onMarkPresent: (SubjectEntity) -> Unit,
    onMarkAbsent: (SubjectEntity) -> Unit,
    onSubjectClick: (SubjectEntity) -> Unit,
    onAddSubjectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DANGER", "SAFE"
    var searchQuery by remember { mutableStateOf("") }

    val todayDateKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val todayDisplayFormatted = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    val todayRecords = remember(recentRecords, todayDateKey) {
        recentRecords.filter { it.dateString == todayDateKey }
    }
    val todayMarkedCount = remember(subjects, todayRecords) {
        subjects.count { sub -> todayRecords.any { it.subjectId == sub.id } }
    }

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

    // Identify most critical course in danger
    val mostCriticalCourse by remember(subjects, targetPercentage, warningMargin) {
        derivedStateOf {
            subjects
                .map { s -> Pair(s, AttendanceCalculator.calculate(s.attendedClasses, s.totalClasses, targetPercentage, warningMargin)) }
                .filter { it.second.status != AttendanceStatus.SAFE }
                .minByOrNull { it.second.percentage }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Prominent Today's Date & Registration Banner
        item(contentType = "TodayBanner") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TODAY",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = todayDisplayFormatted,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (todayMarkedCount == subjects.size && subjects.isNotEmpty()) {
                                StatusSafe.copy(alpha = 0.15f)
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(
                                1.dp,
                                if (todayMarkedCount == subjects.size && subjects.isNotEmpty()) {
                                    StatusSafe.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                        ) {
                            Text(
                                text = "$todayMarkedCount/${subjects.size} Logged",
                                color = if (todayMarkedCount == subjects.size && subjects.isNotEmpty()) StatusSafe else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Hero Attendance Gauge Card
        item(contentType = "AttendanceGauge") {
            AttendanceGauge(
                percentage = stats.overallPercentage,
                targetPercentage = targetPercentage,
                warningMargin = warningMargin,
                safeBunks = stats.totalSafeBunks,
                classesNeeded = stats.totalClassesNeeded
            )
        }

        // Section Title: Attendance Summary
        item(contentType = "SummaryHeader") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attendance Summary",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(100.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Threshold: ${targetPercentage.toInt()}%",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Attendance Summary Cards
        item(contentType = "SummaryCards") {
            AttendanceSummaryCards(
                stats = stats,
                targetPercentage = targetPercentage,
                onCardClick = { category ->
                    when (category) {
                        "SAFE" -> selectedFilter = "SAFE"
                        "DANGER" -> selectedFilter = "DANGER"
                        else -> selectedFilter = "ALL"
                    }
                }
            )
        }

        // Critical Risk Alert Banner (when courses fall below target)
        if (mostCriticalCourse != null) {
            val (criticalSub, calc) = mostCriticalCourse!!
            item(contentType = "CriticalRiskBanner") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = StatusCritical.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, StatusCritical.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = StatusCritical.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusCritical,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Action Required: ${criticalSub.name}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Currently at ${String.format(java.util.Locale.US, "%.1f", calc.percentage)}%. Attend next ${calc.classesNeeded} consecutive class${if (calc.classesNeeded > 1) "es" else ""} to avoid exam debarment.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Search Bar and Filter Header
        item(contentType = "FilterHeader") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enrolled Courses (${subjects.size})",
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
                        modifier = Modifier.testTag("add_subject_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Course", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search courses...", fontSize = 13.sp) },
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

                // Filter Chips
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
                        label = { Text("Danger (${stats.subjectsInDanger})") },
                        colors = filterChipColors()
                    )
                    FilterChip(
                        selected = selectedFilter == "SAFE",
                        onClick = {
                            TouchFeedback.tick(context)
                            selectedFilter = "SAFE"
                        },
                        label = { Text("Safe (${subjects.size - stats.subjectsInDanger})") },
                        colors = filterChipColors()
                    )
                }
            }
        }

        // Subject Cards
        if (filteredSubjects.isEmpty()) {
            item(contentType = "EmptyNotice") {
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
                        Text(
                            text = if (subjects.isEmpty()) "No Courses Added Yet" else "No Matching Courses",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (subjects.isEmpty()) "Tap '+ Add Course' above to track your first class!" else "Try clearing your search query or switching filters.",
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
                contentType = { "SubjectCard" }
            ) { subject ->
                val currentTodayRecord = todayRecords.firstOrNull { it.subjectId == subject.id }
                SubjectCard(
                    subject = subject,
                    targetPercentage = targetPercentage,
                    warningMargin = warningMargin,
                    todayStatus = currentTodayRecord?.status,
                    isCompact = isCompact,
                    onMarkPresent = { onMarkPresent(subject) },
                    onMarkAbsent = { onMarkAbsent(subject) },
                    onClick = { onSubjectClick(subject) },
                    modifier = Modifier.animateItem()
                )
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
