package com.hudnah.habitlab.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.data.local.AppDatabase
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.local.entity.Streak
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/**
 * Integrationstest der Offline-Persistenz mit einer dateibasierten Room-DB (TWP §7.1).
 * Prüft, dass Daten nach Schließen und erneutem Öffnen vollständig vorhanden sind.
 */
@RunWith(AndroidJUnit4::class)
class PersistenceTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dbName = "persistence_test.db"

    @Before
    fun clean() {
        context.deleteDatabase(dbName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun openDb(): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, dbName).build()

    @Test
    fun Persistence_survivesReopen() = runBlocking {
        val today = LocalDate.of(2026, 7, 17).toEpochDay()

        // Erste Sitzung: Daten schreiben, dann DB schließen.
        val db1 = openDb()
        val habitId = db1.habitDao().insert(
            Habit(name = "Lesen", iconKey = "star", colorHex = "#EF5350")
        )
        db1.entryDao().insert(Entry(habitId = habitId, date = today))
        db1.streakDao().insert(
            Streak(habitId = habitId, currentStreak = 1, bestStreak = 1, lastActiveDate = today)
        )
        db1.close()

        // Zweite Sitzung: DB erneut öffnen und Vollständigkeit prüfen.
        val db2 = openDb()
        val habits = db2.habitDao().getAllSnapshot()
        val entries = db2.entryDao().getAllSnapshot()
        val streak = db2.streakDao().getByHabitSnapshot(habitId)

        assertEquals(1, habits.size)
        assertEquals("Lesen", habits.first().name)
        assertEquals(1, entries.size)
        assertEquals(today, entries.first().date)
        assertNotNull(streak)
        assertEquals(1, streak!!.currentStreak)
        db2.close()
    }
}
