package com.hudnah.habitlab

import com.hudnah.habitlab.data.local.dao.EntryDao
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.repository.EntryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** Unit-Tests der Entry-Komponente mit gemocktem DAO (TWP §6.2). */
class EntryRepositoryTest {

    private val today = LocalDate.of(2026, 7, 17)
    private val todayEpoch = today.toEpochDay()
    private val dao: EntryDao = mockk(relaxed = true)
    private val repo = EntryRepository(dao) { today }

    @Test
    fun EntryRepository_createEntry() = runTest {
        coEvery { dao.getByHabitAndDate(1L, todayEpoch) } returns null
        coEvery { dao.insert(any()) } returns 10L

        repo.createEntry(1L)

        coVerify(exactly = 1) {
            dao.insert(match { it.habitId == 1L && it.date == todayEpoch })
        }
    }

    @Test
    fun EntryRepository_createEntry_returnsId() = runTest {
        coEvery { dao.getByHabitAndDate(any(), any()) } returns null
        coEvery { dao.insert(any()) } returns 42L

        assertEquals(42L, repo.createEntry(1L))
    }

    @Test
    fun EntryRepository_isCompletedToday_true() = runTest {
        coEvery { dao.getByHabitAndDate(1L, todayEpoch) } returns
            Entry(id = 1, habitId = 1L, date = todayEpoch)

        assertTrue(repo.isCompletedToday(1L))
    }

    @Test
    fun EntryRepository_isCompletedToday_false() = runTest {
        coEvery { dao.getByHabitAndDate(1L, todayEpoch) } returns null

        assertFalse(repo.isCompletedToday(1L))
    }

    @Test
    fun EntryRepository_idempotentEntry() = runTest {
        // Es existiert bereits ein Eintrag für heute -> kein zweiter wird angelegt.
        val existing = Entry(id = 7, habitId = 1L, date = todayEpoch)
        coEvery { dao.getByHabitAndDate(1L, todayEpoch) } returns existing

        val id = repo.createEntry(1L)

        assertEquals(7L, id)
        coVerify(exactly = 0) { dao.insert(any()) }
    }

    @Test
    fun EntryRepository_deleteEntry() = runTest {
        repo.deleteEntry(5L)

        coVerify(exactly = 1) { dao.deleteById(5L) }
    }

    @Test
    fun EntryRepository_getEntriesByHabit() = runTest {
        val entries = listOf(
            Entry(id = 1, habitId = 1L, date = todayEpoch),
            Entry(id = 2, habitId = 1L, date = todayEpoch - 1)
        )
        every { dao.getByHabit(1L) } returns flowOf(entries)

        val result = repo.getEntriesByHabit(1L).first()

        assertEquals(entries, result)
        assertTrue(result.all { it.habitId == 1L })
    }

    @Test
    fun EntryRepository_getAllEntries() = runTest {
        val entries = listOf(
            Entry(id = 1, habitId = 1L, date = todayEpoch),
            Entry(id = 2, habitId = 2L, date = todayEpoch)
        )
        every { dao.getAll() } returns flowOf(entries)

        assertEquals(entries, repo.getAllEntries().first())
    }
}
