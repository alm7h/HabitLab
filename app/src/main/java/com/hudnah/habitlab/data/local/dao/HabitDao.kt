package com.hudnah.habitlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hudnah.habitlab.data.local.entity.Habit
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert
    suspend fun insert(habit: Habit): Long

    @Update
    suspend fun update(habit: Habit)

    /** Löscht den Habit; zugehörige entry/streak werden per ON DELETE CASCADE entfernt. */
    @Query("DELETE FROM habit WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE habit SET isArchived = 1 WHERE id = :id")
    suspend fun setArchived(id: Long)

    @Query("SELECT * FROM habit WHERE id = :id")
    suspend fun getById(id: Long): Habit?

    /** Reaktiver Stream aller aktiven (nicht archivierten) Habits. */
    @Query("SELECT * FROM habit WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun getAllActive(): Flow<List<Habit>>

    @Query("SELECT * FROM habit WHERE isArchived = 0 ORDER BY createdAt ASC")
    suspend fun getAllActiveSnapshot(): List<Habit>

    @Query("SELECT * FROM habit ORDER BY createdAt ASC")
    suspend fun getAllSnapshot(): List<Habit>
}
