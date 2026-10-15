package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.data.db.HolidayEntity
import com.example.data.db.SubjectEntity
import com.example.ui.OverallAttendanceStats
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.StatusWarning
import com.example.util.AttendanceCalculator
import com.example.util.TouchFeedback

@Composable
fun SimulatorScreen(
    stats: OverallAttendanceStats,
    subjects: List<SubjectEntity> = emptyList(),
    targetPercentage: Double,
    onTargetChange: (Double) -> Unit,
    futureAttended: Int,
    onFutureAttendedChange: (Int) -> Unit,
    futureMissed: Int,
    onFutureMissedChange: (Int) -> Unit,
    holidays: List<HolidayEntity>,
    onAddHoliday: (name: String, date: String, isExam: Boolean) -> Unit,
    onDeleteHoliday: (HolidayEntity) -> Unit,
    onImportOfficialHolidays: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }

    val selectedSubject = remember(subjects, selectedSubjectId) {
        subjects.find { it.id == selectedSubjectId }
    }

    val baseAttended = selectedSubject?.attendedClasses ?: stats.totalAttended
    val baseConducted = selectedSubject?.totalClasses ?: stats.totalConducted
    val basePercentage = if (selectedSubject != null) {
        if (baseConducted > 0) (baseAttended.toDouble() / baseConducted.toDouble()) * 100.0 else 100.0
    } else {
        stats.overallPercentage
    }

    val projectedPercentage = AttendanceCalculator.simulate(
        currentAttended = baseAttended,
        currentTotal = baseConducted,
        futureAttended = futureAttended,
        futureMissed = futureMissed
    )
    val projectedCalc = AttendanceCalculator.calculate(
        attended = baseAttended + futureAttended,
        total = baseConducted + futureAttended + futureMissed,
        targetPercentage = targetPercentage
    )
    val delta = projectedPercentage - basePercentage

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("simulator_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: University Criteria Slider
        item(contentType = "CriteriaCard") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "University Criteria",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${targetPercentage.toInt()}% Required",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Most universities mandate 75% minimum attendance to appear in semester exams.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = targetPercentage.toFloat(),
                        onValueChange = {
                            TouchFeedback.tick(context)
                            onTargetChange(it.toDouble())
                        },
                        valueRange = 60f..90f,
                        steps = 5,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(60.0, 70.0, 75.0, 80.0, 90.0).forEach { p ->
                            Text(
                                text = "${p.toInt()}%",
                                color = if (targetPercentage.toInt() == p.toInt()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = if (targetPercentage.toInt() == p.toInt()) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Future Scenario Simulator (with Course Selector)
        item(contentType = "FutureScenarioCard") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = StatusSafe,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Future Scenario Simulator",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulate how attending or missing upcoming classes impacts your overall or per-course percentage.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )

                    if (subjects.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("simulator_course_selector")
                        ) {
                            item {
                                val isAllSelected = selectedSubject == null
                                FilterChip(
                                    selected = isAllSelected,
                                    onClick = {
                                        TouchFeedback.tick(context)
                                        selectedSubjectId = null
                                    },
                                    label = {
                                        Text(
                                            text = "All Courses (${stats.totalAttended}/${stats.totalConducted})",
                                            fontSize = 11.sp,
                                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    )
                                )
                            }
                            items(subjects, key = { it.id }) { sub ->
                                val isSelected = selectedSubject?.id == sub.id
                                val shortLabel = if (sub.code.isNotBlank()) {
                                    "${sub.code} (${sub.attendedClasses}/${sub.totalClasses})"
                                } else {
                                    "${sub.name} (${sub.attendedClasses}/${sub.totalClasses})"
                                }
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        TouchFeedback.tick(context)
                                        selectedSubjectId = sub.id
                                    },
                                    label = {
                                        Text(
                                            text = shortLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier.testTag("sim_course_chip_${sub.id}")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Attending Slider
                    Text(
                        text = "Classes you will attend: +$futureAttended",
                        color = StatusSafe,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = futureAttended.toFloat(),
                        onValueChange = {
                            TouchFeedback.tick(context)
                            onFutureAttendedChange(it.toInt())
                        },
                        valueRange = 0f..30f,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusSafe,
                            activeTrackColor = StatusSafe,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Missing Slider
                    Text(
                        text = "Classes you will miss: -$futureMissed",
                        color = StatusCritical,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = futureMissed.toFloat(),
                        onValueChange = {
                            TouchFeedback.tick(context)
                            onFutureMissedChange(it.toInt())
                        },
                        valueRange = 0f..20f,
                        colors = SliderDefaults.colors(
                            thumbColor = StatusCritical,
                            activeTrackColor = StatusCritical,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Projection Result Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${selectedSubject?.name ?: "All Courses"} • Current: ${String.format(java.util.Locale.US, "%.1f", basePercentage)}%",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Projected: ${String.format(java.util.Locale.US, "%.1f", projectedPercentage)}%",
                                        color = if (projectedPercentage >= targetPercentage) StatusSafe else StatusCritical,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (delta >= 0) StatusSafe.copy(alpha = 0.15f) else StatusCritical.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (delta >= 0) "+${String.format(java.util.Locale.US, "%.1f", delta)}%" else "${String.format(java.util.Locale.US, "%.1f", delta)}%",
                                        color = if (delta >= 0) StatusSafe else StatusCritical,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "After scenario (${baseAttended + futureAttended}/${baseConducted + futureAttended + futureMissed} classes): " +
                                        if (projectedCalc.safeBunks > 0) {
                                            "${projectedCalc.safeBunks} additional safe bunk${if (projectedCalc.safeBunks > 1) "s" else ""} available."
                                        } else if (projectedCalc.classesNeeded > 0) {
                                            "Need ${projectedCalc.classesNeeded} consecutive class${if (projectedCalc.classesNeeded > 1) "es" else ""} to reach ${targetPercentage.toInt()}%."
                                        } else {
                                            "Right at the ${targetPercentage.toInt()}% threshold (0 bunks remaining)."
                                        },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Semester End Forecaster
        item(contentType = "ForecasterCard") {
            var forecastWeeks by remember { mutableFloatStateOf(8f) }
            var forecastWeeklyClasses by remember { mutableFloatStateOf(3f) }
            val forecast = AttendanceCalculator.forecastSemester(
                currentAttended = baseAttended,
                currentHeld = baseConducted,
                remainingWeeks = forecastWeeks.toInt(),
                classesPerWeek = forecastWeeklyClasses.toInt(),
                targetPercentage = targetPercentage
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Semester End Forecaster",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (selectedSubject != null) "Forecasting: ${selectedSubject.name}" else "When total classes are unknown (All Courses)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Weekly Estimator",
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Weeks left in semester", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        Text("${forecastWeeks.toInt()} weeks", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = forecastWeeks,
                        onValueChange = {
                            TouchFeedback.tick(context)
                            forecastWeeks = it
                        },
                        valueRange = 1f..16f,
                        steps = 14,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Average classes per week", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp)
                        Text("${forecastWeeklyClasses.toInt()} classes/wk", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = forecastWeeklyClasses,
                        onValueChange = {
                            TouchFeedback.tick(context)
                            forecastWeeklyClasses = it
                        },
                        valueRange = 1f..6f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Estimated Total", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(
                                        text = "~${forecast.projectedTotalClasses} classes",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "($baseConducted held + ${forecast.remainingClasses} left)",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Max Bunks Allowed", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(
                                        text = "${forecast.totalBunksAllowedForSemester} Bunks",
                                        color = if (forecast.totalBunksAllowedForSemester > 0) StatusSafe else StatusCritical,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(
                                        text = "Must attend ${forecast.minClassesToAttend} classes",
                                        color = if (forecast.isAchievable) StatusSafe else StatusCritical,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Holidays & Academic Calendar
        item(contentType = "HolidayHeader") {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = StatusWarning,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Holidays & Calendar",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = {
                                TouchFeedback.click(context)
                                onImportOfficialHolidays()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("Import Online", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                TouchFeedback.tick(context)
                                showAddHolidayDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        if (holidays.isEmpty()) {
            item(contentType = "EmptyHolidays") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "No holidays or exam events planned yet. Tap 'Import Online' to load official calendar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(
                items = holidays,
                key = { it.id },
                contentType = { "HolidayItem" }
            ) { h ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (h.isExam) StatusCritical.copy(alpha = 0.15f) else StatusWarning.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (h.isExam) "EXAM" else "HOLIDAY",
                                        color = if (h.isExam) StatusCritical else StatusWarning,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = h.dateString, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = h.name, color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = {
                                TouchFeedback.heavy(context)
                                onDeleteHoliday(h)
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAddHolidayDialog) {
        var hName by remember { mutableStateOf("") }
        var hDate by remember { mutableStateOf("2026-11-15") }
        var isExam by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddHolidayDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Add Semester Event", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = hName,
                        onValueChange = { hName = it },
                        label = { Text("Event Name (e.g. Diwali Break / Mid-Terms)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = hDate,
                        onValueChange = { hDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isExam,
                            onCheckedChange = { isExam = it },
                            colors = CheckboxDefaults.colors(checkedColor = StatusCritical)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("This is an examination event", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (hName.isNotBlank()) {
                            TouchFeedback.click(context)
                            onAddHoliday(hName, hDate, isExam)
                            showAddHolidayDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save Event")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHolidayDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
