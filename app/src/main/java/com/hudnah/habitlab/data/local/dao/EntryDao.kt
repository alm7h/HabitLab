package com.hudnah.habitlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hudnah.habitlab.data.local.entity.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    /** UNIQUE(habitId, date): bei Duplikat wird ignoriert und -1 zurückgegeben. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: Entry): Long

    @Query("DELETE FROM entry WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM entry WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getByHabitAndDate(habitId: Long, date: Long): Entry?

    @Query("SELECT * FROM entry WHERE habitId = :habitId ORDER BY date ASC")
    fun getByHabit(habitId: Long): Flow<List<Entry>>

    @Query("SELECT * FROM entry WHERE habitId = :habitId ORDER BY date ASC")
    suspend fun getByHabitSnapshot(habitId: Long): List<Entry>

    @Query("SELECT * FROM entry ORDER BY date ASC")
    fun getAll(): Flow<List<Entry>>

    @Query("SELECT * FROM entry ORDER BY date ASC")
    suspend fun getAllSnapshot(): List<Entry>
}
