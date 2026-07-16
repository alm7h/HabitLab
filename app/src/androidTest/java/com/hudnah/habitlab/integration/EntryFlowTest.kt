package com.hudnah.habitlab.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.data.local.entity.Habit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Integrationstests der Entry-Flows mit echter In-Memory-Room-DB (TWP §7.1). */
@RunWith(AndroidJUnit4::class)
class EntryFlowTest : IntegrationTestBase() {

    private fun newHabit(name: String = "Lesen"): Long = runBlocking {
        habitRepo.createHabit(Habit(name = name, iconKey = "star", colorHex = "#EF5350"))
    }

    @Test
    fun EntryFlow_createAndQuery() = runBlocking {
        val habitId = newHabit()

        val entryId = entryRepo.createEntry(habitId)

        assertTrue(entryId > 0)
        val entries = entryRepo.getEntriesByHabit(habitId).first()
        assertEquals(1, entries.size)
        assertEquals(habitId, entries.first().habitId)
        assertEquals(today.toEpochDay(), entries.first().date)
    }

    @Test
    fun EntryFlow_idempotentPersistence() = runBlocking {
        val habitId = newHabit()

        entryRepo.createEntry(habitId)
        // Zweiter Abschluss am selben Tag wird durch den UNIQUE-Index verhindert.
        entryRepo.createEntry(habitId)

        val entries = entryRepo.getEntriesByHabit(habitId).first()
        assertEquals(1, entries.size)
    }
}
