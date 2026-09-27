package com.gk.habitstreak.model

data class Habit(
    val id: String,
    val title: String,
    val category: String = "General",
    val icon: String = "⚡️",
    val colorHex: Long = 0xFF4ECDC4,
    val completedDates: Set<String> = emptySet(), // Format: "YYYY-MM-DD"
    val createdAt: String
)

data class DayStatus(
    val dayLabel: String,   // "M", "T", "W", "T", "F", "S", "S"
    val dateString: String, // "YYYY-MM-DD"
    val isCompleted: Boolean,
    val isToday: Boolean
)

data class HabitItemUi(
    val habit: Habit,
    val isCompletedToday: Boolean,
    val currentStreak: Int,
    val longestStreak: Int,
    val last7Days: List<DayStatus>
)

enum class HabitFilter {
    ALL,
    PENDING_TODAY,
    COMPLETED_TODAY
}

data class HabitSummary(
    val totalHabits: Int = 0,
    val completedTodayCount: Int = 0,
    val completionPercentage: Int = 0,
    val bestStreak: Int = 0,
    val activeStreaksCount: Int = 0
)

data class HabitUiState(
    val items: List<HabitItemUi> = emptyList(),
    val summary: HabitSummary = HabitSummary(),
    val filter: HabitFilter = HabitFilter.ALL,
    val todayFormatted: String = ""
)
