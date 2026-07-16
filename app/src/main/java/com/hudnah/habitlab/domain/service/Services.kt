package com.hudnah.habitlab.domain.service

import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.local.entity.Streak
import com.hudnah.habitlab.domain.model.HeatmapDay
import com.hudnah.habitlab.domain.model.MonthlyStats
import com.hudnah.habitlab.domain.model.WeeklyStats
import kotlinx.coroutines.flow.Flow

/** Habit Komponente (TWP §6.1). */
interface IHabitService {
    suspend fun createHabit(habit: Habit): Long
    suspend fun updateHabit(habit: Habit)
    suspend fun deleteHabit(id: Long)
    suspend fun archiveHabit(id: Long)
    fun getAllHabits(): Flow<List<Habit>>
    suspend fun getHabitById(id: Long): Habit?
}

/** Entry Komponente (TWP §6.2). */
interface IEntryService {
    suspend fun createEntry(habitId: Long): Long
    suspend fun deleteEntry(entryId: Long)
    suspend fun isCompletedToday(habitId: Long): Boolean
    fun getEntriesByHabit(habitId: Long): Flow<List<Entry>>
    fun getAllEntries(): Flow<List<Entry>>
}

/** Streak Komponente (TWP §6.3). */
interface IStreakService {
    suspend fun calculateCurrentStreak(habitId: Long): Int
    suspend fun calculateBestStreak(habitId: Long): Int
    suspend fun updateStreak(habitId: Long)
    fun getStreakForHabit(habitId: Long): Flow<Streak?>
}

/** Statistics Komponente (TWP §6.4). */
interface IStatisticsService {
    suspend fun generateWeeklyStats(): WeeklyStats
    suspend fun generateMonthlyStats(): MonthlyStats
    suspend fun generateHeatmap(weeks: Int = 4): List<HeatmapDay>
    suspend fun getCompletionsByHabit(): Map<Habit, Int>
}
