package com.hudnah.habitlab.di

import com.hudnah.habitlab.data.local.AppDatabase
import com.hudnah.habitlab.data.repository.EntryRepository
import com.hudnah.habitlab.data.repository.HabitRepository
import com.hudnah.habitlab.data.repository.StatsRepository
import com.hudnah.habitlab.data.repository.StreakRepository
import com.hudnah.habitlab.domain.StreakCalculator
import com.hudnah.habitlab.domain.service.IEntryService
import com.hudnah.habitlab.domain.service.IHabitService
import com.hudnah.habitlab.domain.service.IStatisticsService
import com.hudnah.habitlab.domain.service.IStreakService
import java.time.LocalDate

/**
 * Manuelle Dependency-Injection: verdrahtet DB → Repositories → Services (TWP §3).
 */
class AppContainer(
    private val db: AppDatabase,
    private val today: () -> LocalDate = { HabitLabClock.today() }
) {
    val habitService: IHabitService = HabitRepository(db.habitDao())
    val entryService: IEntryService = EntryRepository(db.entryDao(), today)
    val streakService: IStreakService =
        StreakRepository(db.streakDao(), db.entryDao(), StreakCalculator(today))
    val statisticsService: IStatisticsService =
        StatsRepository(entryService, habitService, today)
}
