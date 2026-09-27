package com.gk.habitstreak.viewmodel

import com.gk.habitstreak.domain.StreakCalculator
import com.gk.habitstreak.model.HabitFilter
import com.gk.habitstreak.model.HabitItemUi
import com.gk.habitstreak.model.HabitSummary
import com.gk.habitstreak.model.HabitUiState
import com.gk.habitstreak.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(
    private val repository: HabitRepository = defaultRepository
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _filter = MutableStateFlow(HabitFilter.ALL)

    private var cachedHabits: List<com.gk.habitstreak.model.Habit>? = null
    private var cachedAllUiItems: List<HabitItemUi>? = null
    private var cachedSummary: HabitSummary? = null

    private fun computeState(habits: List<com.gk.habitstreak.model.Habit>, filter: HabitFilter): HabitUiState {
        val today = StreakCalculator.today()

        // Reuse precomputed items and summary if habits list hasn't changed (e.g. user just switched tabs)
        val allUiItems = if (habits === cachedHabits && cachedAllUiItems != null) {
            cachedAllUiItems!!
        } else {
            val items = habits.map { habit ->
                StreakCalculator.toUiItem(habit, today)
            }
            val completedToday = items.count { it.isCompletedToday }
            val total = items.size
            val percentage = if (total > 0) (completedToday * 100) / total else 0
            val bestStreak = items.maxOfOrNull { it.longestStreak } ?: 0
            val activeStreaks = items.count { it.currentStreak > 0 }

            cachedSummary = HabitSummary(
                totalHabits = total,
                completedTodayCount = completedToday,
                completionPercentage = percentage,
                bestStreak = bestStreak,
                activeStreaksCount = activeStreaks
            )
            cachedHabits = habits
            cachedAllUiItems = items
            items
        }

        val filteredItems = when (filter) {
            HabitFilter.ALL -> allUiItems
            HabitFilter.PENDING_TODAY -> allUiItems.filter { !it.isCompletedToday }
            HabitFilter.COMPLETED_TODAY -> allUiItems.filter { it.isCompletedToday }
        }

        return HabitUiState(
            items = filteredItems,
            summary = cachedSummary ?: HabitSummary(),
            filter = filter,
            todayFormatted = today.toString()
        )
    }

    val uiState: StateFlow<HabitUiState> = combine(
        repository.habitsFlow,
        _filter
    ) { habits, filter ->
        computeState(habits, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = computeState(repository.habitsFlow.value, HabitFilter.ALL)
    )

    fun toggleHabit(habitId: String) {
        repository.toggleHabitToday(habitId)
    }

    fun addHabit(
        title: String,
        category: String = "General",
        icon: String = "⚡️",
        colorHex: Long = 0xFF4ECDC4
    ) {
        if (title.isNotBlank()) {
            repository.addHabit(title, category, icon, colorHex)
        }
    }

    fun deleteHabit(habitId: String) {
        repository.deleteHabit(habitId)
    }

    fun setFilter(filter: HabitFilter) {
        _filter.value = filter
    }

    /**
     * Swift-friendly observer for SwiftUI.
     * Returns a cleanup cancellation lambda.
     */
    fun observeUiState(onChange: (HabitUiState) -> Unit): () -> Unit {
        val job = viewModelScope.launch {
            uiState.collect { state ->
                onChange(state)
            }
        }
        return { job.cancel() }
    }

    companion object {
        val defaultRepository = HabitRepository()
    }
}
