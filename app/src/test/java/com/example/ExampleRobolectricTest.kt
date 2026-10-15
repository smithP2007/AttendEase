package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.SubjectEntity
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AttendEase", appName)
    }

    @Test
    fun `insertSubject deduplicates courses with same code or name`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repo = AttendanceRepository(db)

        val firstId = repo.insertSubject(
            SubjectEntity(
                name = "Artificial Intelligence",
                code = "CS-305",
                attendedClasses = 1,
                totalClasses = 1,
                roomLocation = "AI Lab 405"
            )
        )
        val secondId = repo.insertSubject(
            SubjectEntity(
                name = "Artificial Intelligence",
                code = "CS-305",
                attendedClasses = 14,
                totalClasses = 16,
                roomLocation = "AI Lab 405"
            )
        )

        val allSubjects = repo.subjects.first()
        assertEquals(firstId, secondId)
        assertEquals(1, allSubjects.size)
        assertEquals(14, allSubjects.first().attendedClasses)
        assertEquals(16, allSubjects.first().totalClasses)
        db.close()
    }
}
