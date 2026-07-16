package com.hudnah.habitlab.data.repository

import com.hudnah.habitlab.data.local.dao.HabitDao
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.service.IHabitService
import kotlinx.coroutines.flow.Flow

/**
 * Habit Komponente – Implementierung von IHabitService (TWP §6.1).
 * Kapselt Zugriffe auf Gewohnheiten über den HabitDao und validiert Eingaben.
 */
class HabitRepository(
    private val habitDao: HabitDao
) : IHabitService {

    override suspend fun createHabit(habit: Habit): Long {
        require(habit.name.isNotBlank()) { "Habit name must not be empty" }
        return habitDao.insert(habit)
    }

    override suspend fun updateHabit(habit: Habit) {
        require(habit.name.isNotBlank()) { "Habit name must not be empty" }
        habitDao.update(habit)
    }

    /** Löscht Habit und – per ON DELETE CASCADE – die zugehörigen Einträge/Streak. */
    override suspend fun deleteHabit(id: Long) {
        habitDao.deleteById(id)
    }

    override suspend fun archiveHabit(id: Long) {
        habitDao.setArchived(id)
    }

    override fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllActive()

    override suspend fun getHabitById(id: Long): Habit? = habitDao.getById(id)
}
