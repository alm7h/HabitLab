package com.hudnah.habitlab

import com.hudnah.habitlab.data.local.dao.EntryDao
import com.hudnah.habitlab.data.local.dao.StreakDao
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.repository.StreakRepository
import com.hudnah.habitlab.domain.StreakCalculator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

/** Unit-Test der Streak-Persistenz mit gemocktem DAO (TWP §6.3). */
class StreakRepositoryTest {

    private val today = LocalDate.of(2026, 7, 17)
    private val t = today.toEpochDay()
    private val streakDao: StreakDao = mockk(relaxed = true)
    private val entryDao: EntryDao = mockk(relaxed = true)
    private val repo = StreakRepository(streakDao, entryDao, StreakCalculator { today })

    @Test
    fun StreakRepository_updateStreakPersists() = runTest {
        // Zwei aufeinanderfolgende Tage bis heute -> currentStreak = bestStreak = 2.
        coEvery { entryDao.getByHabitSnapshot(1L) } returns listOf(
            Entry(id = 1, habitId = 1L, date = t - 1),
            Entry(id = 2, habitId = 1L, date = t)
        )
        coEvery { streakDao.getByHabitSnapshot(1L) } returns null

        repo.updateStreak(1L)

        coVerify(exactly = 1) {
            streakDao.insert(match {
                it.habitId == 1L &&
                    it.currentStreak == 2 &&
                    it.bestStreak == 2 &&
                    it.lastActiveDate == t
            })
        }
    }
}
