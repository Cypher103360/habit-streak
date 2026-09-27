package com.gk.habitstreak.repository

import com.gk.habitstreak.domain.StreakCalculator
import com.gk.habitstreak.model.Habit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus

class HabitRepository {

    private val _habits = MutableStateFlow<List<Habit>>(initialSeedHabits())
    val habitsFlow: StateFlow<List<Habit>> = _habits.asStateFlow()

    fun addHabit(
        title: String,
        category: String = "General",
        icon: String = "⚡️",
        colorHex: Long = 0xFF4ECDC4
    ): Habit {
        val today = StreakCalculator.today()
        val newHabit = Habit(
            id = "habit_${generateRandomId()}",
            title = title.trim(),
            category = category,
            icon = icon,
            colorHex = colorHex,
            completedDates = emptySet(),
            createdAt = today.toString()
        )
        _habits.value = _habits.value + newHabit
        return newHabit
    }

    fun toggleHabitToday(habitId: String) {
        val todayStr = StreakCalculator.today().toString()
        _habits.value = _habits.value.map { habit ->
            if (habit.id == habitId) {
                val updatedDates = if (habit.completedDates.contains(todayStr)) {
                    habit.completedDates - todayStr
                } else {
                    habit.completedDates + todayStr
                }
                habit.copy(completedDates = updatedDates)
            } else {
                habit
            }
        }
    }

    fun deleteHabit(habitId: String) {
        _habits.value = _habits.value.filterNot { it.id == habitId }
    }

    private fun generateRandomId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        val timestamp = StreakCalculator.today().toString().replace("-", "")
        val randomSuffix = (1..6).map { chars.random() }.joinToString("")
        return "${timestamp}_$randomSuffix"
    }

    companion object {
        fun initialSeedHabits(): List<Habit> {
            val today = StreakCalculator.today()
            val y1 = today.minus(1, DateTimeUnit.DAY).toString()
            val y2 = today.minus(2, DateTimeUnit.DAY).toString()
            val y3 = today.minus(3, DateTimeUnit.DAY).toString()
            val y4 = today.minus(4, DateTimeUnit.DAY).toString()
            val todayStr = today.toString()

            return listOf(
                Habit(
                    id = "seed_1",
                    title = "Morning 5km Run",
                    category = "Fitness",
                    icon = "🏃‍♂️",
                    colorHex = 0xFFFF6B6B,
                    completedDates = setOf(todayStr, y1, y2, y3, y4),
                    createdAt = y4
                ),
                Habit(
                    id = "seed_2",
                    title = "Drink 2.5L Water",
                    category = "Health",
                    icon = "💧",
                    colorHex = 0xFF4ECDC4,
                    completedDates = setOf(y1, y2, y3),
                    createdAt = y3
                ),
                Habit(
                    id = "seed_3",
                    title = "Read 20 Pages",
                    category = "Mind",
                    icon = "📚",
                    colorHex = 0xFF5C6BC0,
                    completedDates = setOf(todayStr, y1, y2),
                    createdAt = y2
                ),
                Habit(
                    id = "seed_4",
                    title = "Evening Meditation",
                    category = "Wellness",
                    icon = "🧘",
                    colorHex = 0xFF26A69A,
                    completedDates = setOf(y1),
                    createdAt = y1
                )
            )
        }
    }
}
