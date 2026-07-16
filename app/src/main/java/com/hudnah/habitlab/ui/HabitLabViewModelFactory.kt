package com.hudnah.habitlab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hudnah.habitlab.di.AppContainer
import com.hudnah.habitlab.di.HabitLabClock
import com.hudnah.habitlab.ui.dashboard.DashboardViewModel
import com.hudnah.habitlab.ui.habits.HabitViewModel
import com.hudnah.habitlab.ui.stats.StatsViewModel

class HabitLabViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(
                container.habitService,
                container.entryService,
                container.streakService,
                container.statisticsService
            ) { HabitLabClock.today() }

            modelClass.isAssignableFrom(HabitViewModel::class.java) -> HabitViewModel(
                container.habitService,
                container.entryService,
                container.streakService
            ) { HabitLabClock.today() }

            modelClass.isAssignableFrom(StatsViewModel::class.java) -> StatsViewModel(
                container.statisticsService
            )

            else -> throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        } as T
    }
}
