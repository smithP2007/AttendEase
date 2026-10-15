package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val totalClasses: Int = 0,
    val attendedClasses: Int = 0,
    val targetPercentage: Double = 75.0,
    val colorHex: String = "#6366F1",
    val creditHours: Int = 3,
    val roomLocation: String = ""
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val dateString: String, // "YYYY-MM-DD"
    val status: String,     // "PRESENT", "ABSENT", "CANCELLED"
    val notes: String = ""
)

@Entity(tableName = "timetable_slots")
data class TimetableSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val startTime: String,
    val endTime: String,
    val subjectId: Long,
    val subjectName: String,
    val room: String = ""
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateString: String,
    val isExam: Boolean = false
)
