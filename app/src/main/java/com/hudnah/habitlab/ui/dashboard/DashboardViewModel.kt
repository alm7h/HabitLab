package com.hudnah.habitlab.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hudnah.habitlab.domain.model.HabitWithProgress
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import com.hudnah.habitlab.domain.service.IStatisticsService
import com.hudnah.habitlab.domain.service.IStreakService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

/**
 * Dashboard Komponente – aggregiert die Kennzahlen des Startbildschirms (TWP §6.5).
 * Konsumiert IHabitService, IEntryService, IStreakService und IStatisticsService.
 */
class DashboardViewModel(
    private val habitService: IHabitService,
    private val entryService: IEntryService,
    private val streakService: IStreakService,
    private val statisticsService: IStatisticsService,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    // Erzwingt eine Neuberechnung für den aktuellen Tag (z. B. beim erneuten Betreten des Screens).
    private val refreshTrigger = MutableStateFlow(0L)

    fun refresh() {
        refreshTrigger.value = refreshTrigger.value + 1
    }

    /** Tagesfortschritt: erledigte / aktive Habits (0.0–1.0). */
    suspend fun getTodayProgress(): Float {
        val habits = habitService.getAllHabits().first()
        if (habits.isEmpty()) return 0f
        val completed = habits.count { entryService.isCompletedToday(it.id) }
        return completed.toFloat() / habits.size
    }

    /** Wochenfortschritt als Anteilswert (0.0–1.0). */
    suspend fun getWeeklyProgress(): Float =
        statisticsService.generateWeeklyStats().completionRate

    /** Anzahl heute bereits erledigter Habits. */
    suspend fun getTodayCompletedCount(): Int {
        val habits = habitService.getAllHabits().first()
        return habits.count { entryService.isCompletedToday(it.id) }
    }

    /** Aktive Habits mit heutigem Status und aktueller Streak. */
    fun getActiveHabits(): Flow<List<HabitWithProgress>> =
        combine(habitService.getAllHabits(), entryService.getAllEntries()) { habits, entries ->
            habits to entries
        }.map { (habits, entries) ->
            val todayEpoch = today().toEpochDay()
            habits.map { habit ->
                val done = entries.any { it.habitId == habit.id && it.date == todayEpoch }
                HabitWithProgress(
                    habit = habit,
                    completedToday = done,
                    currentStreak = streakService.calculateCurrentStreak(habit.id)
                )
            }
        }

    /** Reaktiver UI-State, der sich nach jedem neuen Eintrag aktualisiert. */
    val uiState: StateFlow<DashboardUiState> by lazy {
        combine(
            habitService.getAllHabits(),
            entryService.getAllEntries(),
            refreshTrigger
        ) { habits, entries, _ ->
            val todayEpoch = today().toEpochDay()
            val completedCount = habits.count { h ->
                entries.any { it.habitId == h.id && it.date == todayEpoch }
            }
            val progress = if (habits.isEmpty()) 0f else completedCount.toFloat() / habits.size
            val withProgress = habits.map { habit ->
                HabitWithProgress(
                    habit = habit,
                    completedToday = entries.any { it.habitId == habit.id && it.date == todayEpoch },
                    currentStreak = streakService.calculateCurrentStreak(habit.id)
                )
            }
            DashboardUiState(
                todayProgress = progress,
                todayCompletedCount = completedCount,
                activeHabitCount = habits.size,
                habits = withProgress
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState()
        )
    }
}

data class DashboardUiState(
    val todayProgress: Float = 0f,
    val todayCompletedCount: Int = 0,
    val activeHabitCount: Int = 0,
    val habits: List<HabitWithProgress> = emptyList()
)
