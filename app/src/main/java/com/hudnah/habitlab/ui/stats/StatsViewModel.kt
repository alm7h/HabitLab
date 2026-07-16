package com.hudnah.habitlab.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.model.HeatmapDay
import com.hudnah.habitlab.domain.model.MonthlyStats
import com.hudnah.habitlab.domain.model.WeeklyStats
import com.hudnah.habitlab.domain.service.IStatisticsService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** ViewModel für das Statistik-Dashboard und die Heatmap (TWP §5.4 / §5.5). */
class StatsViewModel(
    private val statisticsService: IStatisticsService
) : ViewModel() {

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = StatsUiState(
                weekly = statisticsService.generateWeeklyStats(),
                monthly = statisticsService.generateMonthlyStats(),
                heatmap = statisticsService.generateHeatmap(),
                completionsByHabit = statisticsService.getCompletionsByHabit()
            )
        }
    }
}

data class StatsUiState(
    val weekly: WeeklyStats = WeeklyStats(0f, 0),
    val monthly: MonthlyStats = MonthlyStats(0, emptyMap()),
    val heatmap: List<HeatmapDay> = emptyList(),
    val completionsByHabit: Map<Habit, Int> = emptyMap()
)
