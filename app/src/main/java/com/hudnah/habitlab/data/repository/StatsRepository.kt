package com.hudnah.habitlab.data.repository

import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.model.HeatmapDay
import com.hudnah.habitlab.domain.model.MonthlyStats
import com.hudnah.habitlab.domain.model.WeeklyStats
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import com.hudnah.habitlab.domain.service.IStatisticsService
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Statistics Komponente – Implementierung von IStatisticsService (TWP §6.4).
 * Aggregiert Produktivitätsdaten auf Basis der gespeicherten Einträge.
 */
class StatsRepository(
    private val entryService: IEntryService,
    private val habitService: IHabitService,
    private val today: () -> LocalDate = { LocalDate.now() }
) : IStatisticsService {

    /** Abschlussquote eines Tages: erledigte aktive Habits / aktive Habits (0.0–1.0). */
    private fun dayRate(day: Long, entries: List<Entry>, activeHabits: List<Habit>): Float {
        if (activeHabits.isEmpty()) return 0f
        val activeIds = activeHabits.map { it.id }.toSet()
        val completed = entries.asSequence()
            .filter { it.date == day && it.habitId in activeIds }
            .map { it.habitId }
            .toSet()
            .size
        return completed.toFloat() / activeHabits.size
    }

    override suspend fun generateWeeklyStats(): WeeklyStats {
        val habits = habitService.getAllHabits().first()
        val entries = entryService.getAllEntries().first()
        val start = today().with(DayOfWeek.MONDAY)
        val activeIds = habits.map { it.id }.toSet()
        val weekDays = (0..6).map { start.plusDays(it.toLong()).toEpochDay() }.toSet()
        val count = entries.count { it.date in weekDays && it.habitId in activeIds }
        val rate = if (habits.isEmpty()) 0f else count.toFloat() / (habits.size * 7)
        return WeeklyStats(completionRate = rate, completionCount = count)
    }

    override suspend fun generateMonthlyStats(): MonthlyStats {
        val habits = habitService.getAllHabits().first()
        val entries = entryService.getAllEntries().first()
        val now = today()
        val activeIds = habits.map { it.id }.toSet()
        val monthEntries = entries.filter {
            val d = LocalDate.ofEpochDay(it.date)
            d.year == now.year && d.monthValue == now.monthValue && it.habitId in activeIds
        }
        val perHabit = habits.associateWith { h -> monthEntries.count { it.habitId == h.id } }
        return MonthlyStats(completionCount = monthEntries.size, perHabit = perHabit)
    }

    override suspend fun generateHeatmap(weeks: Int): List<HeatmapDay> {
        val habits = habitService.getAllHabits().first()
        val entries = entryService.getAllEntries().first()
        val days = weeks * 7
        val end = today()
        val start = end.minusDays((days - 1).toLong())
        return (0 until days).map { offset ->
            val epochDay = start.plusDays(offset.toLong()).toEpochDay()
            HeatmapDay(date = epochDay, rate = dayRate(epochDay, entries, habits))
        }
    }

    override suspend fun getCompletionsByHabit(): Map<Habit, Int> {
        val habits = habitService.getAllHabits().first()
        val entries = entryService.getAllEntries().first()
        return habits.associateWith { h -> entries.count { it.habitId == h.id } }
    }
}
