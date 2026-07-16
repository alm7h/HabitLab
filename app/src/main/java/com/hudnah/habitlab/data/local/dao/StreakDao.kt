package com.hudnah.habitlab.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.hudnah.habitlab.data.local.entity.Streak
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Insert
    suspend fun insert(streak: Streak): Long

    @Update
    suspend fun update(streak: Streak)

    @Query("SELECT * FROM streak WHERE habitId = :habitId LIMIT 1")
    suspend fun getByHabitSnapshot(habitId: Long): Streak?

    @Query("SELECT * FROM streak WHERE habitId = :habitId LIMIT 1")
    fun getByHabit(habitId: Long): Flow<Streak?>

    @Query("SELECT * FROM streak")
    suspend fun getAllSnapshot(): List<Streak>
}
