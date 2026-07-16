package com.hudnah.habitlab

import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.repository.StatsRepository
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/** Unit-Tests der Statistik-Aggregation mit definierten Eintragsmengen (TWP §6.4). */
class StatsRepositoryTest {

    private val today = LocalDate.of(2026, 7, 17)
    private val t = today.toEpochDay()
    private val monday = today.with(DayOfWeek.MONDAY)

    private val habitService: IHabitService = mockk()
    private val entryService: IEntryService = mockk()
    private val repo = StatsRepository(entryService, habitService) { today }

    private val h1 = Habit(id = 1, name = "Lesen", iconKey = "star", colorHex = "#EF5350")
    private val h2 = Habit(id = 2, name = "Sport", iconKey = "home", colorHex = "#42A5F5")

    private fun habits(list: List<Habit>) {
        every { habitService.getAllHabits() } returns flowOf(list)
    }

    private fun entries(list: List<Entry>) {
        every { entryService.getAllEntries() } returns flowOf(list)
    }

    @Test
    fun StatsRepository_weeklyRate() = runTest {
        habits(listOf(h1, h2))
        // 3 Abschlüsse in der laufenden Woche.
        entries(
            listOf(
                Entry(id = 1, habitId = 1L, date = monday.toEpochDay()),
                Entry(id = 2, habitId = 1L, date = monday.plusDays(1).toEpochDay()),
                Entry(id = 3, habitId = 2L, date = monday.plusDays(2).toEpochDay())
            )
        )

        val stats = repo.generateWeeklyStats()

        assertEquals(3, stats.completionCount)
        assertEquals(3f / (2 * 7), stats.completionRate, 0.0001f)
    }

    @Test
    fun StatsRepository_monthlyStats() = runTest {
        habits(listOf(h1, h2))
        entries(
            listOf(
                Entry(id = 1, habitId = 1L, date = monday.toEpochDay()),
                Entry(id = 2, habitId = 1L, date = monday.plusDays(1).toEpochDay()),
                Entry(id = 3, habitId = 2L, date = monday.plusDays(2).toEpochDay())
            )
        )

        val stats = repo.generateMonthlyStats()

        assertEquals(3, stats.completionCount)
        assertEquals(2, stats.perHabit[h1])
        assertEquals(1, stats.perHabit[h2])
    }

    @Test
    fun StatsRepository_heatmap4Weeks() = runTest {
        habits(listOf(h1, h2))
        entries(listOf(Entry(id = 1, habitId = 1L, date = t)))

        val heatmap = repo.generateHeatmap(4)

        assertEquals(28, heatmap.size)
        // Heute: 1 von 2 aktiven Habits erledigt => 0.5.
        assertEquals(0.5f, heatmap.last().rate, 0.0001f)
        assertEquals(t, heatmap.last().date)
    }

    @Test
    fun StatsRepository_heatmapDefaultWeeks() = runTest {
        habits(listOf(h1))
        entries(emptyList())

        // Ohne Parameter: Standard von 4 Wochen (28 Tage).
        assertEquals(28, repo.generateHeatmap().size)
    }

    @Test
    fun StatsRepository_completionsByHabit() = runTest {
        habits(listOf(h1, h2))
        entries(
            listOf(
                Entry(id = 1, habitId = 1L, date = t),
                Entry(id = 2, habitId = 1L, date = t - 1),
                Entry(id = 3, habitId = 1L, date = t - 2),
                Entry(id = 4, habitId = 2L, date = t)
            )
        )

        val map = repo.getCompletionsByHabit()

        assertEquals(3, map[h1])
        assertEquals(1, map[h2])
    }

    @Test
    fun StatsRepository_rateNoActiveHabits() = runTest {
        habits(emptyList())
        entries(listOf(Entry(id = 1, habitId = 1L, date = t)))

        // Keine aktiven Habits => 0 % (keine Division durch Null).
        assertEquals(0f, repo.generateWeeklyStats().completionRate, 0.0001f)
        assertEquals(0f, repo.generateHeatmap().last().rate, 0.0001f)
    }

    @Test
    fun StatsRepository_rateFullDay() = runTest {
        habits(listOf(h1, h2))
        // Alle aktiven Habits heute erledigt => Tagesquote 100 %.
        entries(
            listOf(
                Entry(id = 1, habitId = 1L, date = t),
                Entry(id = 2, habitId = 2L, date = t)
            )
        )

        assertEquals(1f, repo.generateHeatmap().last().rate, 0.0001f)
    }
}
