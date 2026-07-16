package com.hudnah.habitlab

import app.cash.turbine.test
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.model.WeeklyStats
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import com.hudnah.habitlab.domain.service.IStatisticsService
import com.hudnah.habitlab.domain.service.IStreakService
import com.hudnah.habitlab.ui.dashboard.DashboardViewModel
import com.hudnah.habitlab.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** Unit-Tests des Dashboard-ViewModels mit gemockten Service-Interfaces (TWP §6.5). */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val today = LocalDate.of(2026, 7, 17)
    private val t = today.toEpochDay()

    private val habitService: IHabitService = mockk()
    private val entryService: IEntryService = mockk()
    private val streakService: IStreakService = mockk()
    private val statisticsService: IStatisticsService = mockk()

    private val h1 = Habit(id = 1, name = "Lesen", iconKey = "star", colorHex = "#EF5350")
    private val h2 = Habit(id = 2, name = "Sport", iconKey = "home", colorHex = "#42A5F5")

    private fun vm() = DashboardViewModel(
        habitService, entryService, streakService, statisticsService
    ) { today }

    @Test
    fun DashboardViewModel_todayProgress() = runTest {
        every { habitService.getAllHabits() } returns flowOf(listOf(h1, h2))
        coEvery { entryService.isCompletedToday(1L) } returns true
        coEvery { entryService.isCompletedToday(2L) } returns false

        assertEquals(0.5f, vm().getTodayProgress(), 0.0001f)
    }

    @Test
    fun DashboardViewModel_todayProgress_noHabits() = runTest {
        every { habitService.getAllHabits() } returns flowOf(emptyList())

        // Keine aktiven Habits => 0.0 (keine Division durch Null).
        assertEquals(0f, vm().getTodayProgress(), 0.0001f)
    }

    @Test
    fun DashboardViewModel_weeklyProgress() = runTest {
        coEvery { statisticsService.generateWeeklyStats() } returns WeeklyStats(0.4f, 5)

        assertEquals(0.4f, vm().getWeeklyProgress(), 0.0001f)
    }

    @Test
    fun DashboardViewModel_todayCompletedCount() = runTest {
        every { habitService.getAllHabits() } returns flowOf(listOf(h1, h2))
        coEvery { entryService.isCompletedToday(1L) } returns true
        coEvery { entryService.isCompletedToday(2L) } returns true

        assertEquals(2, vm().getTodayCompletedCount())
    }

    @Test
    fun DashboardViewModel_activeHabitsExcludeArchived() = runTest {
        // getAllHabits liefert bereits nur aktive Habits.
        every { habitService.getAllHabits() } returns flowOf(listOf(h1))
        every { entryService.getAllEntries() } returns flowOf(listOf(Entry(id = 1, habitId = 1L, date = t)))
        coEvery { streakService.calculateCurrentStreak(1L) } returns 5

        val result = vm().getActiveHabits().first()

        assertEquals(1, result.size)
        assertEquals(h1, result[0].habit)
        assertEquals(true, result[0].completedToday)
        assertEquals(5, result[0].currentStreak)
    }

    @Test
    fun DashboardViewModel_stateUpdatesOnEntry() = runTest {
        val entriesFlow = MutableStateFlow<List<Entry>>(emptyList())
        every { habitService.getAllHabits() } returns flowOf(listOf(h1))
        every { entryService.getAllEntries() } returns entriesFlow
        coEvery { streakService.calculateCurrentStreak(1L) } returns 1

        vm().uiState.test {
            // Ausgangszustand: noch nichts erledigt.
            var state = awaitItem()
            while (state.todayCompletedCount == 0 && state.activeHabitCount == 0) {
                state = awaitItem()
            }
            assertEquals(0, state.todayCompletedCount)

            // Neuer Eintrag -> UI-State aktualisiert sich reaktiv.
            entriesFlow.value = listOf(Entry(id = 1, habitId = 1L, date = t))
            val updated = awaitItem()
            assertEquals(1, updated.todayCompletedCount)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
