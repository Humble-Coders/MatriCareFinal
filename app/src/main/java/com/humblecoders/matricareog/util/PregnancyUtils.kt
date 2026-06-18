package com.humblecoders.matricareog.util

import com.humblecoders.matricareog.model.User
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object PregnancyUtils {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun capitalizeName(name: String): String =
        name.trim().split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

    fun getTrimester(weeks: Int): Int = when {
        weeks <= 0 -> 0
        weeks <= 13 -> 1
        weeks <= 27 -> 2
        else -> 3
    }

    /** Tab index 0–2 for maternal guide screens. */
    fun trimesterTabIndex(weeks: Int): Int = when (getTrimester(weeks)) {
        1 -> 0
        2 -> 1
        3 -> 2
        else -> 0
    }

    fun getTrimesterLabel(weeks: Int): String = when (getTrimester(weeks)) {
        1 -> "1st Trimester"
        2 -> "2nd Trimester"
        else -> "3rd Trimester"
    }

    fun getTrimesterColor(weeks: Int): Long = when (getTrimester(weeks)) {
        1 -> 0xFF4CAF50
        2 -> 0xFFFF9800
        else -> 0xFFE91E63
    }

    /** Due date = today + remaining weeks until 40 weeks gestation. */
    fun calculateDueDateFromWeeks(weeksPregnant: Int): String {
        if (weeksPregnant <= 0 || weeksPregnant > 42) return ""
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, 40 - weeksPregnant)
        return dateFormat.format(calendar.time)
    }

    /** Weeks pregnant from a due date (approximate, based on 40-week gestation). */
    fun calculateWeeksFromDueDate(dueDateMillis: Long): Int {
        val today = Calendar.getInstance()
        val due = Calendar.getInstance().apply { timeInMillis = dueDateMillis }
        val diffMs = due.timeInMillis - today.timeInMillis
        val weeksUntilDue = TimeUnit.MILLISECONDS.toDays(diffMs) / 7.0
        val weeksPregnant = (40 - weeksUntilDue).toInt()
        return weeksPregnant.coerceIn(1, 42)
    }

    fun parseDueDate(dueDate: String): Long? {
        if (dueDate.isBlank()) return null
        return try {
            dateFormat.parse(dueDate)?.time
        } catch (_: Exception) {
            null
        }
    }

    fun formatDueDate(millis: Long): String = dateFormat.format(millis)

    fun hasDueDate(dueDate: String): Boolean = dueDate.isNotBlank()

    /** Prefer live calculation from due date; fall back to stored weeks from Firestore. */
    fun currentWeeksPregnant(dueDate: String, storedWeeks: String): Int {
        parseDueDate(dueDate)?.let { return calculateWeeksFromDueDate(it) }
        return storedWeeks.toIntOrNull()?.coerceIn(0, 42) ?: 0
    }

    /** Default picker date ≈ 20 weeks pregnant (due date ~20 weeks from today). */
    fun defaultDueDatePickerMillis(): Long {
        return Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 20) }.timeInMillis
    }

    fun applyDueDateToUser(user: User, dueDateMillis: Long): User {
        return user.copy(
            dueDate = formatDueDate(dueDateMillis),
            weeksPregnant = calculateWeeksFromDueDate(dueDateMillis).toString()
        )
    }
}
