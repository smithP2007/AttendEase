package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY name ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE id = :id LIMIT 1")
    fun getSubjectById(id: Long): Flow<SubjectEntity?>

    @Query("SELECT * FROM subjects WHERE LOWER(TRIM(code)) = LOWER(TRIM(:code)) AND TRIM(:code) != '' LIMIT 1")
    suspend fun getSubjectByCode(code: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getSubjectByName(name: String): SubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity): Long

    @Update
    suspend fun updateSubject(subject: SubjectEntity)

    @Delete
    suspend fun deleteSubject(subject: SubjectEntity)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()

    // Attendance Records
    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId ORDER BY timestamp DESC")
    fun getRecordsForSubject(subjectId: Long): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int = 100): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AttendanceRecordEntity): Long

    @Delete
    suspend fun deleteRecord(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT * FROM attendance_records WHERE dateString = :dateString")
    fun getRecordsForDate(dateString: String): Flow<List<AttendanceRecordEntity>>

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId")
    suspend fun deleteRecordsForSubject(subjectId: Long)

    @Query("SELECT * FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString LIMIT 1")
    suspend fun getRecordForSubjectAndDate(subjectId: Long, dateString: String): AttendanceRecordEntity?

    @Query("DELETE FROM attendance_records WHERE subjectId = :subjectId AND dateString = :dateString")
    suspend fun deleteRecordForSubjectAndDate(subjectId: Long, dateString: String)

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM attendance_records WHERE dateString = :dateString")
    suspend fun deleteRecordsForDate(dateString: String)

    // Timetable Slots
    @Query("SELECT * FROM timetable_slots ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllTimetableSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getTimetableSlotsForDay(dayOfWeek: Int): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek AND LOWER(TRIM(startTime)) = LOWER(TRIM(:startTime)) AND LOWER(TRIM(subjectName)) = LOWER(TRIM(:subjectName)) LIMIT 1")
    suspend fun findMatchingTimetableSlot(dayOfWeek: Int, startTime: String, subjectName: String): TimetableSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity): Long

    @Update
    suspend fun updateTimetableSlot(slot: TimetableSlotEntity)

    @Delete
    suspend fun deleteTimetableSlot(slot: TimetableSlotEntity)

    @Query("DELETE FROM timetable_slots WHERE dayOfWeek = :dayOfWeek")
    suspend fun clearTimetableSlotsForDay(dayOfWeek: Int)

    @Query("DELETE FROM timetable_slots")
    suspend fun deleteAllTimetableSlots()

    // Holidays
    @Query("SELECT * FROM holidays ORDER BY dateString ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE dateString = :dateString AND LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun findMatchingHoliday(name: String, dateString: String): HolidayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays")
    suspend fun deleteAllHolidays()
}
