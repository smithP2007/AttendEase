package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AttendanceRecordEntity
import com.example.data.db.SubjectEntity
import com.example.ui.theme.MotionTokens
import com.example.ui.theme.StatusCritical
import com.example.ui.theme.StatusSafe
import com.example.ui.theme.interactiveSpring
import com.example.util.TouchFeedback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun SubjectAttendanceCalendar(
    subject: SubjectEntity,
    records: List<AttendanceRecordEntity>,
    onToggleDate: (dateString: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Calendar month state
    var displayCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
        })
    }

    val todayCal = remember { Calendar.getInstance() }
    val todayDateKey = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(todayCal.time)
    }

    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthYearTitle = remember(displayCalendar.timeInMillis) {
        monthYearFormat.format(displayCalendar.time)
    }

    val isCurrentMonth = remember(displayCalendar.timeInMillis) {
        displayCalendar.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                displayCalendar.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)
    }

    // Days in current display month
    val year = displayCalendar.get(Calendar.YEAR)
    val month = displayCalendar.get(Calendar.MONTH)
    val daysInMonth = displayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    // Calculate start day of week (Monday = 0, Sunday = 6)
    val firstDayOfWeekIndex = remember(year, month) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // Calendar.SUNDAY = 1, MONDAY = 2
        // Convert to Monday=0, Tuesday=1 ... Sunday=6
        (dayOfWeek - Calendar.MONDAY + 7) % 7
    }

    // Records mapped by date string for O(1) lookup
    val recordMap by remember(records, subject.id) {
        derivedStateOf {
            records.associateBy { it.dateString }
        }
    }

    // Count present and absent for the active displayed month
    val monthPrefix = String.format(Locale.US, "%04d-%02d-", year, month + 1)
    val monthRecords = remember(recordMap, monthPrefix) {
        recordMap.filterKeys { it.startsWith(monthPrefix) }
    }
    val monthPresentCount = remember(monthRecords) {
        monthRecords.values.count { it.status == "PRESENT" }
    }
    val monthAbsentCount = remember(monthRecords) {
        monthRecords.values.count { it.status == "ABSENT" }
    }

    var selectedDateKey by remember { mutableStateOf(todayDateKey) }

    val glassBorderBrush = Brush.linearGradient(
        colors = listOf(
            StatusSafe.copy(alpha = 0.42f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("subject_calendar_card")
            .graphicsLayer {
                clip = true
                shape = RoundedCornerShape(22.dp)
            },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, glassBorderBrush),
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            StatusSafe.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = Offset(300f, 80f),
                        radius = 380f
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header: Month Navigation & Today Jump (Muzli top-right calendar style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = StatusSafe.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, StatusSafe.copy(alpha = 0.4f)),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = StatusSafe,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = monthYearTitle,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Attendance Calendar",
                                color = StatusSafe,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!isCurrentMonth) {
                            OutlinedButton(
                                onClick = {
                                    TouchFeedback.tick(context)
                                    displayCalendar = Calendar.getInstance().apply {
                                        set(Calendar.DAY_OF_MONTH, 1)
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Text("Current", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        IconButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                val c = Calendar.getInstance().apply {
                                    time = displayCalendar.time
                                    add(Calendar.MONTH, -1)
                                }
                                displayCalendar = c
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                TouchFeedback.tick(context)
                                val c = Calendar.getInstance().apply {
                                    time = displayCalendar.time
                                    add(Calendar.MONTH, 1)
                                }
                                displayCalendar = c
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Month Stats Badge Row
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Present count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(StatusSafe, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$monthPresentCount Present",
                                    color = StatusSafe,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Absent count
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(StatusCritical, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "$monthAbsentCount Absent",
                                    color = StatusCritical,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        val totalMonthLogged = monthPresentCount + monthAbsentCount
                        val monthRate = if (totalMonthLogged > 0) {
                            (monthPresentCount.toDouble() / totalMonthLogged.toDouble()) * 100.0
                        } else 100.0

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (monthRate >= subject.targetPercentage) StatusSafe.copy(alpha = 0.14f) else StatusCritical.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = if (totalMonthLogged > 0) "${String.format(Locale.US, "%.0f", monthRate)}% month" else "No classes logged",
                                color = if (monthRate >= subject.targetPercentage) StatusSafe else StatusCritical,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Days of the week header (Mon - Sun)
                val weekDayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekDayLabels.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Days Grid
                val totalCells = firstDayOfWeekIndex + daysInMonth
                val rows = (totalCells + 6) / 7

                for (rowIndex in 0 until rows) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - firstDayOfWeekIndex + 1

                            if (dayNumber in 1..daysInMonth) {
                                val dateKey = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayNumber)
                                val record = recordMap[dateKey]
                                val isToday = dateKey == todayDateKey
                                val isSelected = dateKey == selectedDateKey

                                CalendarDateCircle(
                                    dayNumber = dayNumber,
                                    dateKey = dateKey,
                                    status = record?.status,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    onCircleClick = {
                                        selectedDateKey = dateKey
                                        TouchFeedback.click(context)
                                        onToggleDate(dateKey)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Blank filler box
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Color Rule Legend
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(StatusSafe, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Teal = Present", color = StatusSafe, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(StatusCritical, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coral = Absent", color = StatusCritical, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Neutral = Unmarked", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Tap any date circle: 1st tap marks Present, 2nd tap marks Absent, 3rd tap clears.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Quick Status Info for Selected Date
                val selectedRecord = recordMap[selectedDateKey]
                AnimatedVisibility(
                    visible = selectedDateKey.isNotBlank(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (selectedRecord?.status) {
                                "PRESENT" -> StatusSafe.copy(alpha = 0.1f)
                                "ABSENT" -> StatusCritical.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            border = BorderStroke(
                                1.dp,
                                when (selectedRecord?.status) {
                                    "PRESENT" -> StatusSafe.copy(alpha = 0.4f)
                                    "ABSENT" -> StatusCritical.copy(alpha = 0.4f)
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Date: $selectedDateKey",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = when (selectedRecord?.status) {
                                            "PRESENT" -> "Status: Attended (Present)"
                                            "ABSENT" -> "Status: Missed (Absent)"
                                            else -> "Status: Not logged yet (tap circle to log)"
                                        },
                                        color = when (selectedRecord?.status) {
                                            "PRESENT" -> StatusSafe
                                            "ABSENT" -> StatusCritical
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Quick Action Button to cycle or reset
                                OutlinedButton(
                                    onClick = {
                                        TouchFeedback.click(context)
                                        onToggleDate(selectedDateKey)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = when (selectedRecord?.status) {
                                            null -> StatusSafe.copy(alpha = 0.1f)
                                            "PRESENT" -> StatusCritical.copy(alpha = 0.1f)
                                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                        },
                                        contentColor = when (selectedRecord?.status) {
                                            null -> StatusSafe
                                            "PRESENT" -> StatusCritical
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        when (selectedRecord?.status) {
                                            null -> StatusSafe
                                            "PRESENT" -> StatusCritical
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    )
                                ) {
                                    Text(
                                        text = when (selectedRecord?.status) {
                                            null -> "+ Mark Present"
                                            "PRESENT" -> "➔ Make Absent"
                                            else -> "↺ Clear Mark"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDateCircle(
    dayNumber: Int,
    dateKey: String,
    status: String?,
    isToday: Boolean,
    isSelected: Boolean,
    onCircleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    val circleColor by animateColorAsState(
        targetValue = when (status) {
            "PRESENT" -> StatusSafe
            "ABSENT" -> StatusCritical
            else -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
        },
        animationSpec = MotionTokens.ColorSpring,
        label = "DateCircleColor"
    )

    val targetBorderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        status == "PRESENT" -> StatusSafe
        status == "ABSENT" -> StatusCritical
        else -> Color.Transparent
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = MotionTokens.ColorSpring,
        label = "DateCircleBorderColor"
    )

    val contentColor = when (status) {
        "PRESENT" -> Color(0xFF041311)
        "ABSENT" -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .height(44.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing halo for marked/today dates (matching date '17' in Muzli screenshot)
        if (status != null || isToday) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        when (status) {
                            "PRESENT" -> StatusSafe.copy(alpha = 0.22f)
                            "ABSENT" -> StatusCritical.copy(alpha = 0.22f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        },
                        CircleShape
                    )
            )
        }
        Surface(
            modifier = Modifier
                .size(34.dp)
                .testTag("date_circle_$dateKey")
                .interactiveSpring(interactionSource, pressedScale = 0.88f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 20.dp)
                ) {
                    onCircleClick()
                },
            shape = CircleShape,
            color = circleColor,
            border = BorderStroke(
                width = when {
                    isSelected -> 2.dp
                    isToday -> 1.5.dp
                    status != null -> 1.dp
                    else -> 0.dp
                },
                color = animatedBorderColor
            ),
            shadowElevation = if (status != null) 4.dp else 0.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "$dayNumber",
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = if (status != null || isToday) FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }
    }
}
