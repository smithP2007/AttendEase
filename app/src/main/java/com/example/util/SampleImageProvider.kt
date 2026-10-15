package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object SampleImageProvider {
    fun createPortalScreenshot(): Bitmap {
        val width = 720
        val height = 960
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val bgPaint = Paint().apply { color = Color.parseColor("#0F172A") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val headerPaint = Paint().apply { color = Color.parseColor("#1E293B") }
        canvas.drawRect(0f, 0f, width.toFloat(), 120f, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
        }
        canvas.drawText("UNIVERSAL INSTITUTE OF TECHNOLOGY", 30f, 55f, titlePaint)

        val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 20f
        }
        canvas.drawText("Student ERP • Semester Attendance Report", 30f, 95f, subtitlePaint)

        val cardPaint = Paint().apply { color = Color.parseColor("#131C31") }
        val cardRect = RectF(30f, 140f, (width - 30).toFloat(), 240f)
        canvas.drawRoundRect(cardRect, 16f, 16f, cardPaint)

        val infoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 22f
            isFakeBoldText = true
        }
        canvas.drawText("Student: Registered Student (2026-CS-42)", 50f, 185f, infoPaint)

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#38BDF8")
            textSize = 18f
        }
        canvas.drawText("Semester VI • Target Threshold: 75.0%", 50f, 220f, metaPaint)

        val thPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#6366F1")
            textSize = 20f
            isFakeBoldText = true
        }
        canvas.drawText("Course Name", 40f, 290f, thPaint)
        canvas.drawText("Attended / Total", 380f, 290f, thPaint)
        canvas.drawText("Status", 580f, 290f, thPaint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#334155")
            strokeWidth = 2f
        }
        canvas.drawLine(30f, 310f, (width - 30).toFloat(), 310f, linePaint)

        val courses = listOf(
            Triple("Computer Networks (CS301)", "22 / 25  (88.0%)", "#10B981"),
            Triple("Database Systems (CS302)", "15 / 22  (68.2%)", "#F43F5E"),
            Triple("Operating Systems Lab (CS303L)", "18 / 20  (90.0%)", "#10B981"),
            Triple("Software Engineering (CS304)", "19 / 24  (79.2%)", "#10B981"),
            Triple("Artificial Intelligence (CS305)", "14 / 16  (87.5%)", "#10B981"),
            Triple("Cloud Architecture (CS306)", "17 / 20  (85.0%)", "#10B981")
        )

        var y = 360f
        val rowTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
        }

        for ((name, counts, statusColor) in courses) {
            val rowBg = Paint().apply { color = Color.parseColor("#1E293B") }
            canvas.drawRoundRect(RectF(30f, y - 30f, (width - 30).toFloat(), y + 30f), 10f, 10f, rowBg)

            canvas.drawText(name, 45f, y + 6f, rowTextPaint)
            canvas.drawText(counts, 380f, y + 6f, rowTextPaint)

            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor(statusColor)
                textSize = 16f
                isFakeBoldText = true
            }
            val statusLabel = if (statusColor == "#10B981") "SAFE" else "CRITICAL"
            canvas.drawText(statusLabel, 580f, y + 6f, badgePaint)

            y += 80f
        }

        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 16f
        }
        canvas.drawText("Generated from Academic ERP Server • Date: 2026-10-13", 50f, y + 30f, footerPaint)

        return bitmap
    }
}
