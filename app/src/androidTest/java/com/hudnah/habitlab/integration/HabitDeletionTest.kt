package com.hudnah.habitlab.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.data.local.entity.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** Integrationstest: ON DELETE CASCADE über die echte DB (TWP §7.1). */
@RunWith(AndroidJUnit4::class)
class HabitDeletionTest : IntegrationTestBase() {

    @Test
    fun HabitDeletion_cascadesEntries() = runBlocking {
        today = LocalDate.of(2026, 7, 17)
        val habitId = habitRepo.createHabit(Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350"))
        entryRepo.createEntry(habitId)
        streakRepo.updateStreak(habitId)

        // Vorbedingung: Eintrag und Streak existieren.
        assertEquals(1, entryRepo.getEntriesByHabit(habitId).first().size)
        assertTrue(db.streakDao().getByHabitSnapshot(habitId) != null)

        habitRepo.deleteHabit(habitId)

        // Habit, zugehörige Einträge und Streak-Datensatz werden mitkaskadiert.
        assertNull(habitRepo.getHabitById(habitId))
        assertEquals(0, entryRepo.getEntriesByHabit(habitId).first().size)
        assertNull(db.streakDao().getByHabitSnapshot(habitId))
    }
}
