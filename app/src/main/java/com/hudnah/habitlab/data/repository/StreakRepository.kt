package com.hudnah.habitlab.data.repository

import com.hudnah.habitlab.data.local.dao.EntryDao
import com.hudnah.habitlab.data.local.dao.StreakDao
import com.hudnah.habitlab.data.local.entity.Streak
import com.hudnah.habitlab.domain.StreakCalculator
import com.hudnah.habitlab.domain.service.IStreakService
import kotlinx.coroutines.flow.Flow

/**
 * Streak Komponente – Implementierung von IStreakService (TWP §6.3).
 * Berechnet Serien aus den Einträgen (EntryDao) und persistiert sie via StreakDao.
 */
class StreakRepository(
    private val streakDao: StreakDao,
    private val entryDao: EntryDao,
    private val calculator: StreakCalculator = StreakCalculator()
) : IStreakService {

    private suspend fun daysOf(habitId: Long): List<Long> =
        entryDao.getByHabitSnapshot(habitId).map { it.date }

    override suspend fun calculateCurrentStreak(habitId: Long): Int =
        calculator.currentStreak(daysOf(habitId))

    override suspend fun calculateBestStreak(habitId: Long): Int =
        calculator.bestStreak(daysOf(habitId))

    /** Aktualisiert und persistiert currentStreak, bestStreak und lastActiveDate. */
    override suspend fun updateStreak(habitId: Long) {
        val days = daysOf(habitId)
        val current = calculator.currentStreak(days)
        val best = calculator.bestStreak(days)
        val lastActive = days.maxOrNull()
        val existing = streakDao.getByHabitSnapshot(habitId)
        if (existing == null) {
            streakDao.insert(
                Streak(
                    habitId = habitId,
                    currentStreak = current,
                    bestStreak = best,
                    lastActiveDate = lastActive,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            streakDao.update(
                existing.copy(
                    currentStreak = current,
                    bestStreak = maxOf(best, existing.bestStreak),
                    lastActiveDate = lastActive,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override fun getStreakForHabit(habitId: Long): Flow<Streak?> =
        streakDao.getByHabit(habitId)
}
