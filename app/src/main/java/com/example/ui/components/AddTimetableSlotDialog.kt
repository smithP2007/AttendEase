package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.db.SubjectEntity
import com.example.data.db.TimetableSlotEntity
import com.example.util.TouchFeedback

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTimetableSlotDialog(
    initialSlot: TimetableSlotEntity? = null,
    initialDayOfWeek: Int = 1,
    subjects: List<SubjectEntity>,
    onDismiss: () -> Unit,
    onSave: (
        slotId: Long,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subjectName: String,
        room: String,
        explicitSubjectId: Long?
    ) -> Unit
) {
    val context = LocalContext.current
    val isEditing = initialSlot != null
    var selectedDay by remember { mutableIntStateOf(initialSlot?.dayOfWeek ?: initialDayOfWeek) }
    var useExistingSubject by remember { mutableStateOf(subjects.isNotEmpty() && initialSlot?.subjectId != null) }
    var selectedSubject by remember {
        mutableStateOf(
            if (initialSlot != null) {
                subjects.find { it.id == initialSlot.subjectId } ?: subjects.firstOrNull()
            } else {
                subjects.firstOrNull()
            }
        )
    }
    var customSubjectName by remember {
        mutableStateOf(initialSlot?.subjectName ?: "")
    }
    var startTime by remember { mutableStateOf(initialSlot?.startTime ?: "09:00 AM") }
    var endTime by remember { mutableStateOf(initialSlot?.endTime ?: "10:00 AM") }
    var room by remember { mutableStateOf(initialSlot?.room ?: selectedSubject?.roomLocation ?: "Room 101") }

    var expandedDay by remember { mutableStateOf(false) }
    var expandedSubject by remember { mutableStateOf(false) }

    val days = listOf(
        Pair(1, "Monday"),
        Pair(2, "Tuesday"),
        Pair(3, "Wednesday"),
        Pair(4, "Thursday"),
        Pair(5, "Friday"),
        Pair(6, "Saturday"),
        Pair(7, "Sunday")
    )

    val timePresets = listOf(
        Pair("08:00 AM", "09:00 AM"),
        Pair("09:00 AM", "10:00 AM"),
        Pair("10:15 AM", "11:15 AM"),
        Pair("11:30 AM", "12:30 PM"),
        Pair("01:00 PM", "02:00 PM"),
        Pair("02:15 PM", "03:15 PM"),
        Pair("03:30 PM", "04:30 PM")
    )

    val effectiveSubjectName = if (useExistingSubject && selectedSubject != null) {
        selectedSubject!!.name
    } else {
        customSubjectName.trim()
    }

    val canSave = effectiveSubjectName.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))
                Column {
                    Text(
                        text = if (isEditing) "Edit Scheduled Class" else "Schedule Weekly Class",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Saved in weekly timetable",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Day of Week Selector
                Text(
                    text = "Day of the Week",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expandedDay,
                    onExpandedChange = { expandedDay = it }
                ) {
                    OutlinedTextField(
                        value = days.find { it.first == selectedDay }?.second ?: "Monday",
                        onValueChange = {},
                        readOnly = true,
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDay) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                            .fillMaxWidth()
                            .testTag("slot_day_dropdown"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDay,
                        onDismissRequest = { expandedDay = false }
                    ) {
                        days.forEach { (dInt, dName) ->
                            DropdownMenuItem(
                                text = { Text(dName) },
                                onClick = {
                                    TouchFeedback.tick(context)
                                    selectedDay = dInt
                                    expandedDay = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Name Selection / Input
                Text(
                    text = "Course / Subject",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                if (subjects.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = useExistingSubject,
                            onClick = {
                                TouchFeedback.tick(context)
                                useExistingSubject = true
                            },
                            label = { Text("Select Course", fontSize = 11.sp) },
                            colors = filterChipColors()
                        )
                        FilterChip(
                            selected = !useExistingSubject,
                            onClick = {
                                TouchFeedback.tick(context)
                                useExistingSubject = false
                            },
                            label = { Text("Custom Name", fontSize = 11.sp) },
                            colors = filterChipColors()
                        )
                    }
                }

                if (useExistingSubject && subjects.isNotEmpty()) {
                    ExposedDropdownMenuBox(
                        expanded = expandedSubject,
                        onExpandedChange = { expandedSubject = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject?.let { "${it.name} (${it.code})" } ?: "Select Course",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSubject) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                                .testTag("slot_subject_dropdown"),
                            colors = fieldColors(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedSubject,
                            onDismissRequest = { expandedSubject = false }
                        ) {
                            subjects.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text("${s.name} (${s.code})") },
                                    onClick = {
                                        TouchFeedback.tick(context)
                                        selectedSubject = s
                                        if (s.roomLocation.isNotBlank() && room.isBlank()) {
                                            room = s.roomLocation
                                        }
                                        expandedSubject = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = customSubjectName,
                        onValueChange = { customSubjectName = it },
                        placeholder = { Text("e.g. Distributed Systems", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("slot_custom_subject_input"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time Slots (Start Time & End Time)
                Text(
                    text = "Class Timings",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // Quick preset time chips
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    timePresets.forEach { (pStart, pEnd) ->
                        val isSelected = startTime == pStart && endTime == pEnd
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable {
                                TouchFeedback.tick(context)
                                startTime = pStart
                                endTime = pEnd
                            }
                        ) {
                            Text(
                                text = "$pStart - $pEnd",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slot_start_time_input"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        leadingIcon = {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("slot_end_time_input"),
                        colors = fieldColors(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Room / Location
                Text(
                    text = "Room / Location (Optional)",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    placeholder = { Text("e.g. Room 302, Lab 1", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("slot_room_input"),
                    colors = fieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (canSave) {
                        TouchFeedback.click(context)
                        onSave(
                            initialSlot?.id ?: 0L,
                            selectedDay,
                            startTime,
                            endTime,
                            effectiveSubjectName,
                            room,
                            if (useExistingSubject) selectedSubject?.id else null
                        )
                    }
                },
                enabled = canSave,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_slot_button")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.size(4.dp))
                Text(if (isEditing) "Update Slot" else "Save Class", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
private fun filterChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
)
