package com.hudnah.habitlab.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.data.local.entity.Habit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Integrationstests von Statistik/Heatmap gegen echte DB-Inhalte (TWP §7.1). */
@RunWith(AndroidJUnit4::class)
class StatsFlowTest : IntegrationTestBase() {

    @Test
    fun StatsFlow_entryAggregation() = runBlocking {
        val h1 = habitRepo.createHabit(Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350"))
        val h2 = habitRepo.createHabit(Habit(name = "Sport", iconKey = "home", colorHex = "#42A5F5"))

        today = LocalDate.of(2026, 7, 17)
        entryRepo.createEntry(h1)
        entryRepo.createEntry(h2)
        today = LocalDate.of(2026, 7, 16)
        entryRepo.createEntry(h1)

        val completions = statsRepo.getCompletionsByHabit()
        assertEquals(2, completions.entries.first { it.key.id == h1 }.value)
        assertEquals(1, completions.entries.first { it.key.id == h2 }.value)

        today = LocalDate.of(2026, 7, 17)
        val weekly = statsRepo.generateWeeklyStats()
        // 3 Abschlüsse insgesamt in der laufenden Woche.
        assertEquals(3, weekly.completionCount)
    }

    @Test
    fun HeatmapFlow_reflectsEntries() = runBlocking {
        val h1 = habitRepo.createHabit(Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350"))
        val h2 = habitRepo.createHabit(Habit(name = "Sport", iconKey = "home", colorHex = "#42A5F5"))

        today = LocalDate.of(2026, 7, 17)
        entryRepo.createEntry(h1) // 1 von 2 aktiven Habits heute erledigt

        val heatmap = statsRepo.generateHeatmap(4)
        val todayCell = heatmap.first { it.date == today.toEpochDay() }
        assertEquals(0.5f, todayCell.rate, 0.0001f)

        // Zweiter Habit ebenfalls erledigt -> Quote heute 100 %.
        entryRepo.createEntry(h2)
        val fullDayCell = statsRepo.generateHeatmap(4).first { it.date == today.toEpochDay() }
        assertEquals(1f, fullDayCell.rate, 0.0001f)
    }
}
