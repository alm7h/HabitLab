package com.hudnah.habitlab.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.data.local.entity.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Integrationstests der Streak-Flows mit echter Room-DB (TWP §7.1). */
@RunWith(AndroidJUnit4::class)
class StreakFlowTest : IntegrationTestBase() {

    private suspend fun completeOn(habitId: Long, day: LocalDate) {
        today = day
        entryRepo.createEntry(habitId)
        streakRepo.updateStreak(habitId)
    }

    @Test
    fun StreakFlow_afterEntry() = runBlocking {
        val habitId = habitRepo.createHabit(Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350"))

        completeOn(habitId, LocalDate.of(2026, 7, 16))
        assertEquals(1, streakRepo.getStreakForHabit(habitId).first()!!.currentStreak)

        completeOn(habitId, LocalDate.of(2026, 7, 17))
        val streak = streakRepo.getStreakForHabit(habitId).first()!!
        assertEquals(2, streak.currentStreak)
        assertEquals(2, streak.bestStreak)
    }

    @Test
    fun StreakFlow_resetAfterGap() = runBlocking {
        val habitId = habitRepo.createHabit(Habit(name = "Sport", iconKey = "home", colorHex = "#42A5F5"))

        completeOn(habitId, LocalDate.of(2026, 7, 14))
        completeOn(habitId, LocalDate.of(2026, 7, 15))
        assertEquals(2, streakRepo.getStreakForHabit(habitId).first()!!.currentStreak)

        // 16. Juli ausgelassen -> beim Abschluss am 17. beginnt die Serie neu.
        completeOn(habitId, LocalDate.of(2026, 7, 17))
        val streak = streakRepo.getStreakForHabit(habitId).first()!!
        assertEquals(1, streak.currentStreak)
        assertEquals(2, streak.bestStreak)
    }
}
