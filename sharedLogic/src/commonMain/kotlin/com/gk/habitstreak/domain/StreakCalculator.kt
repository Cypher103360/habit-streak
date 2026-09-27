package com.gk.habitstreak.domain

import com.gk.habitstreak.model.DayStatus
import com.gk.habitstreak.model.Habit
import com.gk.habitstreak.model.HabitItemUi
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

object StreakCalculator {

    fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
        return Clock.System.todayIn(timeZone)
    }

    fun toUiItem(
        habit: Habit,
        currentDate: LocalDate = today()
    ): HabitItemUi {
        val todayStr = currentDate.toString()
        val isCompletedToday = habit.completedDates.contains(todayStr)
        val currentStreak = calculateCurrentStreak(habit.completedDates, currentDate)
        val longestStreak = calculateLongestStreak(habit.completedDates)
        val last7Days = generateLast7Days(habit.completedDates, currentDate)

        return HabitItemUi(
            habit = habit,
            isCompletedToday = isCompletedToday,
            currentStreak = currentStreak,
            longestStreak = maxOf(longestStreak, currentStreak),
            last7Days = last7Days
        )
    }

    fun calculateCurrentStreak(
        completedDates: Set<String>,
        currentDate: LocalDate = today()
    ): Int {
        if (completedDates.isEmpty()) return 0

        val todayStr = currentDate.toString()
        val yesterdayStr = currentDate.minus(1, DateTimeUnit.DAY).toString()

        val isCompletedToday = completedDates.contains(todayStr)
        val isCompletedYesterday = completedDates.contains(yesterdayStr)

        if (!isCompletedToday && !isCompletedYesterday) {
            return 0
        }

        var streak = 0
        var checkDate = if (isCompletedToday) currentDate else currentDate.minus(1, DateTimeUnit.DAY)

        while (completedDates.contains(checkDate.toString())) {
            streak++
            checkDate = checkDate.minus(1, DateTimeUnit.DAY)
        }

        return streak
    }

    fun calculateLongestStreak(completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0

        val sortedDates = completedDates.mapNotNull {
            try {
                LocalDate.parse(it)
            } catch (_: Exception) {
                null
            }
        }.sorted()

        if (sortedDates.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1

        for (i in 1 until sortedDates.size) {
            val prev = sortedDates[i - 1]
            val curr = sortedDates[i]

            val expectedNext = prev.plus(1, DateTimeUnit.DAY)
            if (curr == expectedNext) {
                currentStreak++
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                }
            } else if (curr != prev) {
                currentStreak = 1
            }
        }

        return maxStreak
    }

    fun generateLast7Days(
        completedDates: Set<String>,
        currentDate: LocalDate = today()
    ): List<DayStatus> {
        val result = mutableListOf<DayStatus>()

        for (offset in 6 downTo 0) {
            val date = currentDate.minus(offset, DateTimeUnit.DAY)
            val dateStr = date.toString()
            val dayInitial = when (date.dayOfWeek.name) {
                "MONDAY" -> "M"
                "TUESDAY" -> "T"
                "WEDNESDAY" -> "W"
                "THURSDAY" -> "T"
                "FRIDAY" -> "F"
                "SATURDAY" -> "S"
                "SUNDAY" -> "S"
                else -> date.dayOfWeek.name.take(1)
            }

            result.add(
                DayStatus(
                    dayLabel = dayInitial,
                    dateString = dateStr,
                    isCompleted = completedDates.contains(dateStr),
                    isToday = offset == 0
                )
            )
        }

        return result
    }
}
