package com.hudnah.habitlab.data.repository

import com.hudnah.habitlab.data.local.dao.EntryDao
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.EntryStatus
import com.hudnah.habitlab.domain.service.IEntryService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Entry Komponente – Implementierung von IEntryService (TWP §6.2).
 * Erfasst und entfernt Tages-Einträge (ein Datensatz pro abgeschlossenem Habit-Tag).
 */
class EntryRepository(
    private val entryDao: EntryDao,
    private val today: () -> LocalDate = { LocalDate.now() }
) : IEntryService {

    /**
     * Erstellt einen Eintrag für heute und gibt die generierte ID zurück.
     * Idempotent: existiert bereits ein Eintrag für heute, wird dessen ID zurückgegeben
     * (UNIQUE(habitId, date)).
     */
    override suspend fun createEntry(habitId: Long): Long {
        val todayEpoch = today().toEpochDay()
        val existing = entryDao.getByHabitAndDate(habitId, todayEpoch)
        if (existing != null) return existing.id
        return entryDao.insert(
            Entry(
                habitId = habitId,
                date = todayEpoch,
                completedAt = System.currentTimeMillis(),
                status = EntryStatus.DONE
            )
        )
    }

    override suspend fun deleteEntry(entryId: Long) {
        entryDao.deleteById(entryId)
    }

    override suspend fun isCompletedToday(habitId: Long): Boolean {
        return entryDao.getByHabitAndDate(habitId, today().toEpochDay()) != null
    }

    override fun getEntriesByHabit(habitId: Long): Flow<List<Entry>> =
        entryDao.getByHabit(habitId)

    override fun getAllEntries(): Flow<List<Entry>> = entryDao.getAll()
}
