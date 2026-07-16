package com.hudnah.habitlab

import com.hudnah.habitlab.data.local.dao.HabitDao
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.repository.HabitRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit-Tests der Habit-Komponente mit gemocktem DAO (TWP §6.1). */
class HabitRepositoryTest {

    private val dao: HabitDao = mockk(relaxed = true)
    private val repo = HabitRepository(dao)

    @Test
    fun HabitRepository_createHabit() = runTest {
        coEvery { dao.insert(any()) } returns 1L
        val habit = Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350")

        val id = repo.createHabit(habit)

        assertEquals(1L, id)
        coVerify(exactly = 1) {
            dao.insert(match {
                it.name == "Lesen" && it.iconKey == "star" && it.colorHex == "#EF5350"
            })
        }
    }

    @Test
    fun HabitRepository_updateHabit() = runTest {
        val habit = Habit(id = 5, name = "Sport", iconKey = "home", colorHex = "#42A5F5")

        repo.updateHabit(habit)

        coVerify(exactly = 1) { dao.update(habit) }
    }

    @Test
    fun HabitRepository_deleteHabit() = runTest {
        // Kaskadierendes Löschen der Einträge erfolgt per ON DELETE CASCADE in der DB;
        // hier wird geprüft, dass das Löschen des Habits ausgelöst wird.
        repo.deleteHabit(7L)

        coVerify(exactly = 1) { dao.deleteById(7L) }
    }

    @Test
    fun HabitRepository_archiveHabit() = runTest {
        repo.archiveHabit(3L)

        // Habit bleibt in der DB, nur isArchived wird gesetzt.
        coVerify(exactly = 1) { dao.setArchived(3L) }
        coVerify(exactly = 0) { dao.deleteById(any()) }
    }

    @Test
    fun HabitRepository_getAllHabits_activeOnly() = runTest {
        val active = listOf(
            Habit(id = 1, name = "A", iconKey = "star", colorHex = "#EF5350", isArchived = false),
            Habit(id = 2, name = "B", iconKey = "home", colorHex = "#42A5F5", isArchived = false)
        )
        every { dao.getAllActive() } returns flowOf(active)

        val result = repo.getAllHabits().first()

        assertEquals(active, result)
        assertTrue(result.none { it.isArchived })
    }

    @Test
    fun HabitRepository_getHabitById_found() = runTest {
        val habit = Habit(id = 9, name = "Wasser", iconKey = "star", colorHex = "#26A69A")
        coEvery { dao.getById(9L) } returns habit

        assertEquals(habit, repo.getHabitById(9L))
    }

    @Test
    fun HabitRepository_getHabitById_unknown() = runTest {
        coEvery { dao.getById(999L) } returns null

        assertNull(repo.getHabitById(999L))
    }

    @Test
    fun HabitRepository_rejectEmptyName() = runTest {
        val invalid = Habit(name = "   ", iconKey = "star", colorHex = "#EF5350")

        var threw = false
        try {
            repo.createHabit(invalid)
        } catch (e: IllegalArgumentException) {
            threw = true
        }

        assertTrue("Empty name must be rejected", threw)
        coVerify(exactly = 0) { dao.insert(any()) }
    }
}
