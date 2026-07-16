package com.hudnah.habitlab.integration

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hudnah.habitlab.data.local.AppDatabase
import com.hudnah.habitlab.data.repository.EntryRepository
import com.hudnah.habitlab.data.repository.HabitRepository
import com.hudnah.habitlab.data.repository.StatsRepository
import com.hudnah.habitlab.data.repository.StreakRepository
import com.hudnah.habitlab.domain.StreakCalculator
import org.junit.After
import org.junit.Before
import java.time.LocalDate

/**
 * Basis für Integrationstests mit echter In-Memory-Room-DB (TWP §7).
 * Der Testtag ist deterministisch über [today] steuerbar.
 */
abstract class IntegrationTestBase {

    protected lateinit var db: AppDatabase
    protected lateinit var habitRepo: HabitRepository
    protected lateinit var entryRepo: EntryRepository
    protected lateinit var streakRepo: StreakRepository
    protected lateinit var statsRepo: StatsRepository

    protected var today: LocalDate = LocalDate.of(2026, 7, 17)
    protected val todayProvider: () -> LocalDate = { today }

    @Before
    fun setUpDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            // Foreign-Key-Constraints (Cascade) auch in-memory erzwingen.
            .allowMainThreadQueries()
            .build()
        wireRepositories()
    }

    private fun wireRepositories() {
        habitRepo = HabitRepository(db.habitDao())
        entryRepo = EntryRepository(db.entryDao(), todayProvider)
        streakRepo = StreakRepository(db.streakDao(), db.entryDao(), StreakCalculator(todayProvider))
        statsRepo = StatsRepository(entryRepo, habitRepo, todayProvider)
    }

    @After
    fun closeDb() {
        db.close()
    }
}
