package com.hudnah.habitlab.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hudnah.habitlab.domain.model.HeatmapDay
import com.hudnah.habitlab.ui.TestTags
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    viewModel: StatsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val todayEpoch = LocalDate.now().toEpochDay()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Statistik", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Diese Woche", fontWeight = FontWeight.Medium)
                Text(
                    text = "${(state.weekly.completionRate * 100).roundToInt()} %  (${state.weekly.completionCount} Abschlüsse)",
                    modifier = Modifier.testTag(TestTags.WEEKLY_RATE)
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Diesen Monat", fontWeight = FontWeight.Medium)
                Text("${state.monthly.completionCount} Abschlüsse gesamt")
                state.monthly.perHabit.forEach { (habit, count) ->
                    Text("• ${habit.name}: $count")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Abschlüsse je Habit", fontWeight = FontWeight.Medium)
                state.completionsByHabit.forEach { (habit, count) ->
                    Text("• ${habit.name}: $count")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Aktivität (Heatmap)", fontWeight = FontWeight.Medium)
                Heatmap(days = state.heatmap, todayEpoch = todayEpoch)
            }
        }
    }
}

@Composable
private fun Heatmap(days: List<HeatmapDay>, todayEpoch: Long) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp).size(width = 400.dp, height = 140.dp)
    ) {
        items(days, key = { it.date }) { day ->
            val filled = day.rate > 0f
            val base = MaterialTheme.colorScheme.primary
            val cellColor = if (filled) base.copy(alpha = 0.25f + 0.75f * day.rate) else Color(0xFFE0E0E0)
            val tagModifier = if (day.date == todayEpoch) Modifier.testTag(TestTags.HEATMAP_TODAY) else Modifier
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(cellColor, RoundedCornerShape(3.dp))
                    .then(tagModifier)
                    .semantics { contentDescription = if (filled) "filled" else "empty" }
            )
        }
    }
}
