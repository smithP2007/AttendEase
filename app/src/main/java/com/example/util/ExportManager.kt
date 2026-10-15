package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.db.SubjectEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportManager {

    fun generateCsv(subjects: List<SubjectEntity>, targetPercentage: Double): String {
        val sb = StringBuilder()
        sb.append("Subject Name,Course Code,Attended Classes,Total Classes,Percentage (%),Target (%),Status,Safe Bunks Allowed,Classes Needed to Reach Target\n")
        for (s in subjects) {
            val res = AttendanceCalculator.calculate(s.attendedClasses, s.totalClasses, targetPercentage)
            val nameEscaped = "\"${s.name.replace("\"", "\"\"")}\""
            val codeEscaped = "\"${s.code.replace("\"", "\"\"")}\""
            val status = res.status.name
            sb.append("$nameEscaped,$codeEscaped,${s.attendedClasses},${s.totalClasses},${String.format(Locale.US, "%.1f", res.percentage)},${String.format(Locale.US, "%.1f", targetPercentage)},$status,${res.safeBunks},${res.classesNeeded}\n")
        }
        return sb.toString()
    }

    fun generateSummaryText(
        subjects: List<SubjectEntity>,
        targetPercentage: Double,
        overallPercentage: Double,
        studentName: String = "Student"
    ): String {
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy 'at' hh:mm a", Locale.getDefault())
        val timestamp = dateFormat.format(Date())

        val sb = StringBuilder()
        sb.append("=========================================\n")
        sb.append("        ATTENDEASE SEMESTER REPORT       \n")
        sb.append("=========================================\n")
        sb.append("Student: $studentName\n")
        sb.append("Generated: $timestamp\n")
        sb.append("Target Requirement: ${String.format(Locale.US, "%.1f", targetPercentage)}%\n")
        sb.append("Overall Attendance: ${String.format(Locale.US, "%.1f", overallPercentage)}%\n\n")

        val inDanger = subjects.filter {
            val res = AttendanceCalculator.calculate(it.attendedClasses, it.totalClasses, targetPercentage)
            res.status != AttendanceStatus.SAFE
        }

        if (inDanger.isEmpty()) {
            sb.append("STATUS: ALL COURSES SAFE! Above target threshold.\n\n")
        } else {
            sb.append("ATTENTION: ${inDanger.size} course(s) require attendance recovery.\n\n")
        }

        sb.append("-----------------------------------------\n")
        sb.append("COURSE BREAKDOWN:\n")
        sb.append("-----------------------------------------\n")

        for (s in subjects) {
            val res = AttendanceCalculator.calculate(s.attendedClasses, s.totalClasses, targetPercentage)
            val codeStr = if (s.code.isNotBlank()) " [${s.code}]" else ""
            sb.append("• ${s.name}$codeStr:\n")
            sb.append("    Attended: ${s.attendedClasses} / ${s.totalClasses} (${String.format(Locale.US, "%.1f", res.percentage)}%)\n")
            if (res.safeBunks > 0) {
                sb.append("    Safe Bunks: Can miss ${res.safeBunks} more class(es)\n")
            } else if (res.classesNeeded > 0) {
                sb.append("    Alert: Attend next ${res.classesNeeded} consecutive class(es)\n")
            } else {
                sb.append("    Status: At target line. Cannot miss next lecture.\n")
            }
            sb.append("\n")
        }

        sb.append("=========================================\n")
        sb.append("Generated with AttendEase for Android\n")
        return sb.toString()
    }

    fun shareCsv(context: Context, csvData: String) {
        try {
            val fileName = "attendance_report_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvData)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Semester Attendance CSV Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Attendance CSV")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareText(context: Context, textData: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textData)
                putExtra(Intent.EXTRA_SUBJECT, "Semester Attendance Statement")
            }
            val chooser = Intent.createChooser(intent, "Share Attendance Summary")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(context: Context, textData: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Attendance Report", textData)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Attendance report copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
