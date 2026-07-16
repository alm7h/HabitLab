package com.hudnah.habitlab.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hudnah.habitlab.data.local.dao.EntryDao
import com.hudnah.habitlab.data.local.dao.HabitDao
import com.hudnah.habitlab.data.local.dao.StreakDao
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.local.entity.Streak

/**
 * Lokale Room-Datenbank (TWP §4). Vollständig offline-fähig.
 */
@Database(
    entities = [Habit::class, Entry::class, Streak::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun entryDao(): EntryDao
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habitlab.db"
                )
                    // Room enforces foreign keys per-connection; enabled by default for FKs.
                    // Bei inkompatiblem Schema (z. B. Alt-Installation) neu aufbauen statt abstürzen.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
