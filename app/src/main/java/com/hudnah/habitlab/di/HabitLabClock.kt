package com.hudnah.habitlab.di

import java.time.LocalDate

/**
 * App-weite Zeitquelle. Standardmäßig das reale Datum; von Instrumented-Tests
 * (E2E, mehrtägige Streaks) überschreibbar, um Tage deterministisch zu simulieren.
 */
object HabitLabClock {
    @Volatile
    var today: () -> LocalDate = { LocalDate.now() }

    fun reset() {
        today = { LocalDate.now() }
    }
}
