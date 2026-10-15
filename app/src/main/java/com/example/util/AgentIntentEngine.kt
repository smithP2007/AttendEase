package com.example.util

import com.example.data.db.SubjectEntity
import com.example.data.remote.AiAction
import java.util.Locale

object AgentIntentEngine {

    fun extractActions(prompt: String, existingSubjects: List<SubjectEntity>, hasImage: Boolean = false): List<AiAction> {
        val actions = mutableListOf<AiAction>()
        val text = prompt.trim()
        val lower = text.lowercase(Locale.ROOT)

        // 1. Holiday & Online Calendar Import
        if (lower.contains("holiday") || lower.contains("online calendar") || lower.contains("import holiday") ||
            lower.contains("academic calendar") || lower.contains("import from online") || lower.contains("semester holidays")
        ) {
            actions.add(AiAction.ImportOfficialHolidays("Official Semester Calendar"))
        }

        // 2. Global Target / Threshold Adjustment
        val targetMatch = Regex("(?:target|criteria|threshold|percentage)\\s*(?:to|as|is|=|of)?\\s*(\\d{1,2}(?:\\.\\d+)?)\\s*%?").find(lower)
            ?: Regex("(?:set|change|update)\\s*(?:attendance)?\\s*(?:criteria|target)?\\s*to\\s*(\\d{1,2})\\s*%").find(lower)
        if (targetMatch != null) {
            val num = targetMatch.groupValues[1].toDoubleOrNull()
            if (num != null && num in 50.0..100.0) {
                actions.add(AiAction.UpdateTarget(num))
            }
        }

        // 3. Forecast Simulator Adjustment
        if (lower.contains("forecast") || lower.contains("simulator") || lower.contains("simulate") ||
            lower.contains("remaining week") || lower.contains("classes per week") || lower.contains("future attended")
        ) {
            val weeks = Regex("(\\d+)\\s*(?:week|wk)").find(lower)?.groupValues?.get(1)?.toIntOrNull()
            val classes = Regex("(\\d+)\\s*(?:class|lecture)").find(lower)?.groupValues?.get(1)?.toIntOrNull()
            val futAtt = Regex("(?:attend|attended)\\s*(\\d+)").find(lower)?.groupValues?.get(1)?.toIntOrNull()
            val futMiss = Regex("(?:miss|missed|absent)\\s*(\\d+)").find(lower)?.groupValues?.get(1)?.toIntOrNull()
            if (weeks != null || classes != null || futAtt != null || futMiss != null) {
                actions.add(
                    AiAction.UpdateForecast(
                        weeksRemaining = weeks,
                        classesPerWeek = classes,
                        futureAttended = futAtt,
                        futureMissed = futMiss
                    )
                )
            }
        }

        // 4. Per-Subject Attendance Modification
        val matchedSubject = findBestMatchingSubject(lower, existingSubjects)
        if (matchedSubject != null) {
            var actionAdded = false
            val countMatch = Regex("(\\d+)\\s*(?:/|out of)\\s*(\\d+)").find(lower)
            if (countMatch != null) {
                val att = countMatch.groupValues[1].toIntOrNull()
                val tot = countMatch.groupValues[2].toIntOrNull()
                if (att != null && tot != null && tot >= att) {
                    actions.add(
                        AiAction.UpdateAttendance(
                            subjectName = matchedSubject.name,
                            setAttended = att,
                            setTotal = tot
                        )
                    )
                    actionAdded = true
                }
            }

            if (!actionAdded) {
                val targetPct = Regex("(\\d{1,3})\\s*%").find(lower)?.groupValues?.get(1)?.toDoubleOrNull()
                    ?: Regex("(?:attendance|percent|%)\\s*(?:to|as|is|=)?\\s*(\\d{1,3})").find(lower)?.groupValues?.get(1)?.toDoubleOrNull()
                if (targetPct != null && targetPct in 0.0..100.0) {
                    val total = if (matchedSubject.totalClasses > 0) matchedSubject.totalClasses else 20
                    val neededAttended = kotlin.math.round((targetPct / 100.0) * total).toInt().coerceIn(0, total)
                    actions.add(
                        AiAction.UpdateAttendance(
                            subjectName = matchedSubject.name,
                            setAttended = neededAttended,
                            setTotal = total
                        )
                    )
                    actionAdded = true
                }
            }

            if (!actionAdded) {
                val deltaAttMatch = Regex("(?:attended|attended another|add)\\s*(\\d+)\\s*(?:more)?\\s*class").find(lower)
                if (deltaAttMatch != null) {
                    val delta = deltaAttMatch.groupValues[1].toIntOrNull() ?: 1
                    actions.add(
                        AiAction.UpdateAttendance(
                            subjectName = matchedSubject.name,
                            setAttended = matchedSubject.attendedClasses + delta,
                            setTotal = matchedSubject.totalClasses + delta
                        )
                    )
                    actionAdded = true
                }
            }

            if (!actionAdded) {
                val isAbsent = lower.contains("absent") || lower.contains("missed") || lower.contains("bunk") || lower.contains("bunked")
                val isPresent = lower.contains("present") || lower.contains("attended") || lower.contains("attend") || lower.contains("mark")
                if (isAbsent || isPresent) {
                    val status = if (isAbsent && !lower.contains("not absent")) "ABSENT" else "PRESENT"
                    actions.add(
                        AiAction.UpdateAttendance(
                            subjectName = matchedSubject.name,
                            status = status
                        )
                    )
                }
            }
        } else {
            val directSubPctMatch = Regex("(?:set|update|change|make)\\s+([a-zA-Z0-9 &._-]+?)\\s+(?:attendance|percentage)?\\s*(?:to|as|is|=)?\\s*(\\d{1,3})\\s*(?:%|percent)", RegexOption.IGNORE_CASE).find(text)
                ?: Regex("(?:set|update|change|make)\\s+(?:attendance|percentage)?\\s*(?:of|for)?\\s*([a-zA-Z0-9 &._-]+?)\\s+(?:attendance|percentage)?\\s*(?:to|as|is|=)?\\s*(\\d{1,3})\\s*(?:%|percent)", RegexOption.IGNORE_CASE).find(text)
            if (directSubPctMatch != null) {
                val rawName = directSubPctMatch.groupValues[1].trim()
                    .replace(Regex("(?i)^(the|my|course|subject)\\s+"), "")
                    .trim()
                val pct = directSubPctMatch.groupValues[2].toDoubleOrNull()
                if (rawName.length >= 2 && pct != null && pct in 0.0..100.0 && !rawName.equals("global", true) && !rawName.equals("target", true)) {
                    val total = 20
                    val neededAttended = kotlin.math.round((pct / 100.0) * total).toInt().coerceIn(0, total)
                    actions.add(
                        AiAction.UpdateAttendance(
                            subjectName = rawName.replaceFirstChar { it.uppercase() },
                            setAttended = neededAttended,
                            setTotal = total
                        )
                    )
                }
            } else {
                val markMatch = Regex("(?:mark|set)\\s+([a-zA-Z0-9 &._-]+?)\\s+(?:as\\s+)?(present|absent)", RegexOption.IGNORE_CASE).find(text)
                if (markMatch != null) {
                    val rawName = markMatch.groupValues[1].trim()
                        .replace(Regex("(?i)^(the|my|course|subject)\\s+"), "")
                        .trim()
                    val status = markMatch.groupValues[2].uppercase()
                    if (rawName.length >= 2 && !rawName.equals("today", true)) {
                        actions.add(
                            AiAction.UpdateAttendance(
                                subjectName = rawName.replaceFirstChar { it.uppercase() },
                                status = status
                            )
                        )
                    }
                }
            }
        }

        // 5. Add New Subject
        val addSubMatch = Regex("(?:add|create|new)\\s+(?:subject|course|class)\\s+([a-zA-Z0-9 &._-]+)").find(text)
        if (addSubMatch != null && matchedSubject == null) {
            val nameCandidate = addSubMatch.groupValues[1].trim()
                .replace(Regex("(?i)\\s+(with|on|at|for|attendance|target).*"), "")
                .trim()
            if (nameCandidate.length >= 2 && !nameCandidate.equals("slot", true) && !nameCandidate.equals("holiday", true)) {
                actions.add(
                    AiAction.AddSubject(
                        name = nameCandidate,
                        code = generateCodeFromName(nameCandidate),
                        attended = 15,
                        total = 18,
                        target = 75.0,
                        credits = 3,
                        room = "Room 301"
                    )
                )
            }
        }

        // 6. Timetable Schedule Slot Addition
        if (lower.contains("schedule") || (lower.contains("timetable") && (lower.contains("add") || lower.contains("slot")))) {
            val dayOfWeek = when {
                lower.contains("mon") -> 1
                lower.contains("tue") -> 2
                lower.contains("wed") -> 3
                lower.contains("thu") -> 4
                lower.contains("fri") -> 5
                lower.contains("sat") -> 6
                lower.contains("sun") -> 7
                else -> 1
            }
            val targetSub = matchedSubject?.name ?: existingSubjects.firstOrNull()?.name ?: "Lecture"
            actions.add(
                AiAction.AddTimetableSlot(
                    subjectName = targetSub,
                    dayOfWeek = dayOfWeek,
                    startTime = "09:00 AM",
                    endTime = "10:00 AM",
                    room = "Room 302"
                )
            )
        }

        return actions
    }

    private fun findBestMatchingSubject(lowerPrompt: String, subjects: List<SubjectEntity>): SubjectEntity? {
        if (subjects.isEmpty()) return null
        for (sub in subjects) {
            if (lowerPrompt.contains(sub.name.lowercase())) return sub
            if (sub.code.isNotBlank() && lowerPrompt.contains(sub.code.lowercase())) return sub
        }
        for (sub in subjects) {
            val words = sub.name.lowercase().split(" ", "-", "_").filter { it.length >= 3 && it !in listOf("and", "the", "for", "lab") }
            for (w in words) {
                if (lowerPrompt.contains(w)) return sub
            }
        }
        if (lowerPrompt.contains("dbms") || lowerPrompt.contains("database")) {
            return subjects.find { it.name.contains("Database", ignoreCase = true) || it.code.contains("DB", ignoreCase = true) }
        }
        if (lowerPrompt.contains("os") || lowerPrompt.contains("operating")) {
            return subjects.find { it.name.contains("Operating", ignoreCase = true) || it.code.contains("OS", ignoreCase = true) }
        }
        if (lowerPrompt.contains("cn") || lowerPrompt.contains("network")) {
            return subjects.find { it.name.contains("Network", ignoreCase = true) || it.code.contains("CN", ignoreCase = true) }
        }
        if (lowerPrompt.contains("se") || lowerPrompt.contains("software")) {
            return subjects.find { it.name.contains("Software", ignoreCase = true) || it.code.contains("SE", ignoreCase = true) }
        }
        if (lowerPrompt.contains("ai") || lowerPrompt.contains("robotics")) {
            return subjects.find { it.name.contains("Intelligence", ignoreCase = true) || it.code.contains("AI", ignoreCase = true) }
        }
        if (lowerPrompt.contains("math") || lowerPrompt.contains("stats") || lowerPrompt.contains("prob")) {
            return subjects.find { it.name.contains("Math", ignoreCase = true) || it.name.contains("Prob", ignoreCase = true) }
        }
        return null
    }

    private fun generateCodeFromName(name: String): String {
        val parts = name.split(" ").filter { it.isNotBlank() }
        val prefix = parts.map { it.first().uppercaseChar() }.joinToString("")
        return if (prefix.length in 2..4) "$prefix-301" else "CS-${(100..499).random()}"
    }
}
