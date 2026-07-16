package com.hudnah.habitlab.ui.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.model.HabitWithProgress
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import com.hudnah.habitlab.domain.service.IStreakService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** ViewModel für Habit-Management und Tages-Abschluss (TWP §5.1 / §5.2). */
class HabitViewModel(
    private val habitService: IHabitService,
    private val entryService: IEntryService,
    private val streakService: IStreakService,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0L)

    fun refresh() {
        refreshTrigger.value = refreshTrigger.value + 1
    }

    val habits: StateFlow<List<HabitWithProgress>> =
        combine(
            habitService.getAllHabits(),
            entryService.getAllEntries(),
            refreshTrigger
        ) { habits, entries, _ ->
            val todayEpoch = today().toEpochDay()
            habits.map { habit ->
                HabitWithProgress(
                    habit = habit,
                    completedToday = entries.any { it.habitId == habit.id && it.date == todayEpoch },
                    currentStreak = streakService.calculateCurrentStreak(habit.id)
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun createHabit(name: String, iconKey: String, colorHex: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            habitService.createHabit(
                Habit(name = name.trim(), iconKey = iconKey, colorHex = colorHex)
            )
        }
    }

    fun updateHabit(habit: Habit) {
        if (habit.name.isBlank()) return
        viewModelScope.launch { habitService.updateHabit(habit) }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch { habitService.deleteHabit(id) }
    }

    fun archiveHabit(id: Long) {
        viewModelScope.launch { habitService.archiveHabit(id) }
    }

    /** Markiert einen Habit für heute als erledigt bzw. macht den Abschluss rückgängig. */
    fun toggleCompletion(habitWithProgress: HabitWithProgress) {
        viewModelScope.launch {
            val habitId = habitWithProgress.habit.id
            if (habitWithProgress.completedToday) {
                val todayEpoch = today().toEpochDay()
                val entry = entryService.getEntriesByHabit(habitId).first()
                    .firstOrNull { it.date == todayEpoch }
                if (entry != null) entryService.deleteEntry(entry.id)
            } else {
                entryService.createEntry(habitId)
            }
            streakService.updateStreak(habitId)
        }
    }
}
