package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.db.AttendanceRecordEntity
import com.example.data.db.HolidayEntity
import com.example.data.db.SubjectEntity
import com.example.data.db.TimetableSlotEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(private val database: AppDatabase) {
    private val dao = database.attendanceDao()

    val subjects: Flow<List<SubjectEntity>> = dao.getAllSubjects()
    val timetableSlots: Flow<List<TimetableSlotEntity>> = dao.getAllTimetableSlots()
    val holidays: Flow<List<HolidayEntity>> = dao.getAllHolidays()
    val recentRecords: Flow<List<AttendanceRecordEntity>> = dao.getRecentRecords()
    val allRecords: Flow<List<AttendanceRecordEntity>> = dao.getAllRecords()

    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecordEntity>> {
        return dao.getRecordsForSubject(subjectId)
    }

    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val currentSubjects = dao.getAllSubjects().first()
        if (currentSubjects.isEmpty()) {
            seedDemoData()
        } else {
            deduplicateExistingSubjects(currentSubjects)
            val refreshedSubjects = dao.getAllSubjects().first()
            val currentRecords = dao.getAllRecords().first()
            if (currentRecords.isEmpty()) {
                seedDemoRecords(refreshedSubjects)
            }
        }
    }

    /**
     * Cleans up any duplicate courses (matching non-empty course code or normalized course name)
     * that were previously inserted multiple times.
     */
    suspend fun deduplicateExistingSubjects(existing: List<SubjectEntity>) = withContext(Dispatchers.IO) {
        val seenKeys = mutableMapOf<String, SubjectEntity>()
        for (subject in existing.sortedBy { it.id }) {
            val normalizedCode = subject.code.trim().uppercase(Locale.ROOT)
            val normalizedName = subject.name.trim().lowercase(Locale.ROOT)
            val key = if (normalizedCode.isNotBlank()) "CODE:$normalizedCode" else "NAME:$normalizedName"
            val nameKey = "NAME:$normalizedName"

            val primary = seenKeys[key] ?: seenKeys[nameKey]
            if (primary == null) {
                seenKeys[key] = subject
                seenKeys[nameKey] = subject
            } else if (primary.id != subject.id) {
                // Keep the higher class count if one of the duplicates has more progress logged
                if (subject.totalClasses > primary.totalClasses || subject.attendedClasses > primary.attendedClasses) {
                    val merged = primary.copy(
                        attendedClasses = maxOf(primary.attendedClasses, subject.attendedClasses),
                        totalClasses = maxOf(primary.totalClasses, subject.totalClasses),
                        roomLocation = primary.roomLocation.ifBlank { subject.roomLocation }
                    )
                    dao.updateSubject(merged)
                    seenKeys[key] = merged
                    seenKeys[nameKey] = merged
                }
                dao.deleteRecordsForSubject(subject.id)
                dao.deleteSubject(subject)
            }
        }
    }

    suspend fun seedDemoData() = withContext(Dispatchers.IO) {
        val s1 = insertSubject(
            SubjectEntity(
                name = "Computer Networks",
                code = "CS-401",
                attendedClasses = 28,
                totalClasses = 32,
                targetPercentage = 75.0,
                colorHex = "#6366F1", // Indigo
                creditHours = 4,
                roomLocation = "Room 302"
            )
        )
        val s2 = insertSubject(
            SubjectEntity(
                name = "Database Systems",
                code = "CS-402",
                attendedClasses = 24,
                totalClasses = 30,
                targetPercentage = 75.0,
                colorHex = "#06B6D4", // Cyan
                creditHours = 4,
                roomLocation = "Room 204"
            )
        )
        val s3 = insertSubject(
            SubjectEntity(
                name = "Operating Systems Lab",
                code = "CS-403L",
                attendedClasses = 11,
                totalClasses = 15,
                targetPercentage = 75.0,
                colorHex = "#8B5CF6", // Purple
                creditHours = 2,
                roomLocation = "OS Lab 3"
            )
        )
        val s4 = insertSubject(
            SubjectEntity(
                name = "Software Engineering",
                code = "CS-404",
                attendedClasses = 18,
                totalClasses = 26,
                targetPercentage = 75.0,
                colorHex = "#F43F5E", // Rose
                creditHours = 3,
                roomLocation = "Seminar Hall B"
            )
        )
        val s5 = insertSubject(
            SubjectEntity(
                name = "Probability & Statistics",
                code = "MA-401",
                attendedClasses = 22,
                totalClasses = 25,
                targetPercentage = 75.0,
                colorHex = "#10B981", // Emerald
                creditHours = 3,
                roomLocation = "Math Block 101"
            )
        )

        // Seed Timetable slots (1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri)
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 1, // Mon
                startTime = "09:00 AM",
                endTime = "10:00 AM",
                subjectId = s1,
                subjectName = "Computer Networks",
                room = "Room 302"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 1,
                startTime = "10:15 AM",
                endTime = "11:15 AM",
                subjectId = s2,
                subjectName = "Database Systems",
                room = "Room 204"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 1,
                startTime = "02:00 PM",
                endTime = "04:00 PM",
                subjectId = s3,
                subjectName = "Operating Systems Lab",
                room = "OS Lab 3"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 2, // Tue
                startTime = "09:00 AM",
                endTime = "10:00 AM",
                subjectId = s4,
                subjectName = "Software Engineering",
                room = "Seminar Hall B"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 2,
                startTime = "11:30 AM",
                endTime = "12:30 PM",
                subjectId = s5,
                subjectName = "Probability & Statistics",
                room = "Math Block 101"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 3, // Wed
                startTime = "09:00 AM",
                endTime = "10:00 AM",
                subjectId = s1,
                subjectName = "Computer Networks",
                room = "Room 302"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 3,
                startTime = "10:15 AM",
                endTime = "11:15 AM",
                subjectId = s2,
                subjectName = "Database Systems",
                room = "Room 204"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 4, // Thu
                startTime = "01:00 PM",
                endTime = "02:00 PM",
                subjectId = s4,
                subjectName = "Software Engineering",
                room = "Seminar Hall B"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 4,
                startTime = "02:15 PM",
                endTime = "03:15 PM",
                subjectId = s5,
                subjectName = "Probability & Statistics",
                room = "Math Block 101"
            )
        )
        insertTimetableSlot(
            TimetableSlotEntity(
                dayOfWeek = 5, // Fri
                startTime = "10:00 AM",
                endTime = "11:00 AM",
                subjectId = s1,
                subjectName = "Computer Networks",
                room = "Room 302"
            )
        )

        // Seed sample holidays
        insertHoliday(
            HolidayEntity(
                name = "University Tech Fest (Invento)",
                dateString = "2026-10-24",
                isExam = false
            )
        )
        insertHoliday(
            HolidayEntity(
                name = "Semester Mid-Term Examinations",
                dateString = "2026-11-05",
                isExam = true
            )
        )

        val allSubs = listOf(s1, s2, s3, s4, s5)
        seedDemoRecords(allSubs.mapNotNull { id -> dao.getSubjectById(id).first() })
    }

    suspend fun seedDemoRecords(subjects: List<SubjectEntity>) = withContext(Dispatchers.IO) {
        // Seed realistic records for October 2026 (Oct 1 - Oct 13)
        val octoberDates = listOf(
            "2026-10-01", "2026-10-02", "2026-10-05", "2026-10-06",
            "2026-10-07", "2026-10-08", "2026-10-09", "2026-10-12", "2026-10-13"
        )
        // Also seed late September dates
        val septemberDates = listOf(
            "2026-09-21", "2026-09-22", "2026-09-23", "2026-09-24", "2026-09-25",
            "2026-09-28", "2026-09-29", "2026-09-30"
        )
        val allPastDates = septemberDates + octoberDates

        for (subject in subjects) {
            val isCritical = subject.name.contains("Software", ignoreCase = true) || subject.name.contains("Database", ignoreCase = true)
            for ((index, dateStr) in allPastDates.withIndex()) {
                val status = if (isCritical && (index == 2 || index == 6 || index == 11)) {
                    "ABSENT"
                } else if (!isCritical && index == 4) {
                    "ABSENT"
                } else {
                    "PRESENT"
                }
                val existing = dao.getRecordForSubjectAndDate(subject.id, dateStr)
                if (existing == null) {
                    dao.insertRecord(
                        AttendanceRecordEntity(
                            subjectId = subject.id,
                            timestamp = System.currentTimeMillis() - ((allPastDates.size - index) * 86400000L),
                            dateString = dateStr,
                            status = status,
                            notes = if (status == "ABSENT") "Missed lecture" else "Attended"
                        )
                    )
                }
            }
        }
    }

    suspend fun resetAllDataToDemo() = withContext(Dispatchers.IO) {
        dao.deleteAllRecords()
        dao.deleteAllTimetableSlots()
        dao.deleteAllHolidays()
        dao.deleteAllSubjects()
        seedDemoData()
    }

    suspend fun clearAllAttendanceRecords() = withContext(Dispatchers.IO) {
        dao.deleteAllRecords()
        val subs = dao.getAllSubjects().first()
        for (s in subs) {
            dao.updateSubject(s.copy(attendedClasses = 0, totalClasses = 0))
        }
    }

    /**
     * Inserts a subject or updates an existing subject if one with the same code or name already exists,
     * preventing duplicate course cards (such as duplicate CS-305 Artificial Intelligence entries).
     */
    suspend fun insertSubject(subject: SubjectEntity): Long = withContext(Dispatchers.IO) {
        val cleanName = subject.name.trim()
        val cleanCode = subject.code.trim().uppercase(Locale.ROOT)
        if (subject.id == 0L) {
            val existing = (if (cleanCode.isNotBlank()) dao.getSubjectByCode(cleanCode) else null)
                ?: dao.getSubjectByName(cleanName)
            if (existing != null) {
                val updated = existing.copy(
                    name = cleanName.ifBlank { existing.name },
                    code = cleanCode.ifBlank { existing.code },
                    attendedClasses = subject.attendedClasses,
                    totalClasses = subject.totalClasses,
                    targetPercentage = subject.targetPercentage,
                    colorHex = subject.colorHex.ifBlank { existing.colorHex },
                    creditHours = subject.creditHours,
                    roomLocation = subject.roomLocation.trim().ifBlank { existing.roomLocation }
                )
                dao.updateSubject(updated)
                return@withContext existing.id
            }
        }
        dao.insertSubject(
            subject.copy(
                name = cleanName,
                code = cleanCode,
                roomLocation = subject.roomLocation.trim()
            )
        )
    }

    suspend fun updateSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        dao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: SubjectEntity) = withContext(Dispatchers.IO) {
        dao.deleteRecordsForSubject(subject.id)
        dao.deleteSubject(subject)
    }

    suspend fun markAttendance(
        subject: SubjectEntity,
        status: String, // "PRESENT", "ABSENT", "CANCELLED"
        dateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        note: String = "",
        strictSingleRegistration: Boolean = true
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getRecordForSubjectAndDate(subject.id, dateString)

        // SINGLE-REGISTRATION RULE:
        if (strictSingleRegistration && existing != null && existing.status == status) {
            return@withContext
        }

        val freshSubject = dao.getSubjectById(subject.id).first() ?: subject

        if (existing != null) {
            // Updating existing record for this date
            dao.insertRecord(
                existing.copy(
                    status = status,
                    timestamp = System.currentTimeMillis(),
                    notes = note
                )
            )
            when {
                existing.status == "PRESENT" && status == "ABSENT" -> {
                    dao.updateSubject(
                        freshSubject.copy(
                            attendedClasses = (freshSubject.attendedClasses - 1).coerceAtLeast(0)
                        )
                    )
                }
                existing.status == "ABSENT" && status == "PRESENT" -> {
                    dao.updateSubject(
                        freshSubject.copy(
                            attendedClasses = freshSubject.attendedClasses + 1
                        )
                    )
                }
                existing.status == "CANCELLED" && status == "PRESENT" -> {
                    dao.updateSubject(
                        freshSubject.copy(
                            attendedClasses = freshSubject.attendedClasses + 1,
                            totalClasses = freshSubject.totalClasses + 1
                        )
                    )
                }
                existing.status == "CANCELLED" && status == "ABSENT" -> {
                    dao.updateSubject(
                        freshSubject.copy(
                            totalClasses = freshSubject.totalClasses + 1
                        )
                    )
                }
                status == "CANCELLED" -> {
                    val minusAttended = if (existing.status == "PRESENT") 1 else 0
                    dao.updateSubject(
                        freshSubject.copy(
                            attendedClasses = (freshSubject.attendedClasses - minusAttended).coerceAtLeast(0),
                            totalClasses = (freshSubject.totalClasses - 1).coerceAtLeast(0)
                        )
                    )
                }
            }
            return@withContext
        }

        // First entry for this date
        dao.insertRecord(
            AttendanceRecordEntity(
                subjectId = freshSubject.id,
                timestamp = System.currentTimeMillis(),
                dateString = dateString,
                status = status,
                notes = note
            )
        )
        when (status) {
            "PRESENT" -> {
                dao.updateSubject(
                    freshSubject.copy(
                        attendedClasses = freshSubject.attendedClasses + 1,
                        totalClasses = freshSubject.totalClasses + 1
                    )
                )
            }
            "ABSENT" -> {
                dao.updateSubject(
                    freshSubject.copy(
                        totalClasses = freshSubject.totalClasses + 1
                    )
                )
            }
        }
    }

    suspend fun clearAttendanceForDate(
        subject: SubjectEntity,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        val freshSubject = dao.getSubjectById(subject.id).first() ?: subject
        val existing = dao.getRecordForSubjectAndDate(freshSubject.id, dateString) ?: return@withContext
        dao.deleteRecord(existing)
        if (existing.status == "PRESENT") {
            dao.updateSubject(
                freshSubject.copy(
                    attendedClasses = (freshSubject.attendedClasses - 1).coerceAtLeast(0),
                    totalClasses = (freshSubject.totalClasses - 1).coerceAtLeast(0)
                )
            )
        } else if (existing.status == "ABSENT") {
            dao.updateSubject(
                freshSubject.copy(
                    totalClasses = (freshSubject.totalClasses - 1).coerceAtLeast(0)
                )
            )
        }
    }

    suspend fun cycleAttendanceForDate(
        subject: SubjectEntity,
        dateString: String
    ): String? = withContext(Dispatchers.IO) {
        val freshSubject = dao.getSubjectById(subject.id).first() ?: subject
        val existing = dao.getRecordForSubjectAndDate(freshSubject.id, dateString)
        when (existing?.status) {
            null -> {
                // 1st click: Mark as PRESENT (Green)
                markAttendance(
                    subject = freshSubject,
                    status = "PRESENT",
                    dateString = dateString,
                    strictSingleRegistration = false
                )
                "PRESENT"
            }
            "PRESENT" -> {
                // 2nd click on Green circle: Mark as ABSENT (Red)
                markAttendance(
                    subject = freshSubject,
                    status = "ABSENT",
                    dateString = dateString,
                    strictSingleRegistration = false
                )
                "ABSENT"
            }
            "ABSENT" -> {
                // 3rd click on Red circle: Clear/Reset
                clearAttendanceForDate(freshSubject, dateString)
                null
            }
            else -> {
                markAttendance(
                    subject = freshSubject,
                    status = "PRESENT",
                    dateString = dateString,
                    strictSingleRegistration = false
                )
                "PRESENT"
            }
        }
    }

    suspend fun markAllPresentForDate(
        subjects: List<SubjectEntity>,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        for (s in subjects) {
            val fresh = dao.getSubjectById(s.id).first() ?: s
            val existing = dao.getRecordForSubjectAndDate(fresh.id, dateString)
            if (existing == null) {
                dao.insertRecord(
                    AttendanceRecordEntity(
                        subjectId = fresh.id,
                        timestamp = System.currentTimeMillis(),
                        dateString = dateString,
                        status = "PRESENT"
                    )
                )
                dao.updateSubject(
                    fresh.copy(
                        attendedClasses = fresh.attendedClasses + 1,
                        totalClasses = fresh.totalClasses + 1
                    )
                )
            }
        }
    }

    suspend fun clearAllForDate(
        subjects: List<SubjectEntity>,
        dateString: String
    ) = withContext(Dispatchers.IO) {
        for (s in subjects) {
            val fresh = dao.getSubjectById(s.id).first() ?: s
            val existing = dao.getRecordForSubjectAndDate(fresh.id, dateString)
            if (existing != null) {
                dao.deleteRecord(existing)
                if (existing.status == "PRESENT") {
                    dao.updateSubject(
                        fresh.copy(
                            attendedClasses = (fresh.attendedClasses - 1).coerceAtLeast(0),
                            totalClasses = (fresh.totalClasses - 1).coerceAtLeast(0)
                        )
                    )
                } else if (existing.status == "ABSENT") {
                    dao.updateSubject(
                        fresh.copy(
                            totalClasses = (fresh.totalClasses - 1).coerceAtLeast(0)
                        )
                    )
                }
            }
        }
    }

    suspend fun undoLastRecord(record: AttendanceRecordEntity, subject: SubjectEntity) = withContext(Dispatchers.IO) {
        val freshSubject = dao.getSubjectById(subject.id).first() ?: subject
        dao.deleteRecord(record)
        when (record.status) {
            "PRESENT" -> {
                dao.updateSubject(
                    freshSubject.copy(
                        attendedClasses = (freshSubject.attendedClasses - 1).coerceAtLeast(0),
                        totalClasses = (freshSubject.totalClasses - 1).coerceAtLeast(0)
                    )
                )
            }
            "ABSENT" -> {
                dao.updateSubject(
                    freshSubject.copy(
                        totalClasses = (freshSubject.totalClasses - 1).coerceAtLeast(0)
                    )
                )
            }
        }
    }

    suspend fun insertTimetableSlot(slot: TimetableSlotEntity): Long = withContext(Dispatchers.IO) {
        if (slot.id == 0L) {
            val existing = dao.findMatchingTimetableSlot(slot.dayOfWeek, slot.startTime, slot.subjectName)
            if (existing != null) {
                val updated = existing.copy(
                    endTime = slot.endTime,
                    subjectId = if (slot.subjectId > 0L) slot.subjectId else existing.subjectId,
                    room = slot.room.ifBlank { existing.room }
                )
                dao.updateTimetableSlot(updated)
                return@withContext existing.id
            }
        }
        dao.insertTimetableSlot(slot)
    }

    suspend fun updateTimetableSlot(slot: TimetableSlotEntity) = withContext(Dispatchers.IO) {
        dao.updateTimetableSlot(slot)
    }

    suspend fun deleteTimetableSlot(slot: TimetableSlotEntity) = withContext(Dispatchers.IO) {
        dao.deleteTimetableSlot(slot)
    }

    suspend fun clearTimetableSlotsForDay(dayOfWeek: Int) = withContext(Dispatchers.IO) {
        dao.clearTimetableSlotsForDay(dayOfWeek)
    }

    suspend fun insertHoliday(holiday: HolidayEntity): Long = withContext(Dispatchers.IO) {
        if (holiday.id == 0L) {
            val existing = dao.findMatchingHoliday(holiday.name, holiday.dateString)
            if (existing != null) {
                dao.insertHoliday(existing.copy(isExam = holiday.isExam))
                return@withContext existing.id
            }
        }
        dao.insertHoliday(holiday)
    }

    suspend fun deleteHoliday(holiday: HolidayEntity) = withContext(Dispatchers.IO) {
        dao.deleteHoliday(holiday)
    }
}
