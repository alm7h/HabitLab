package com.hudnah.habitlab.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hudnah.habitlab.domain.model.HabitWithProgress
import com.hudnah.habitlab.ui.TestTags
import com.hudnah.habitlab.ui.colorFromHex
import com.hudnah.habitlab.ui.iconForKey

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onToggle: (HabitWithProgress) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Beim (erneuten) Betreten des Screens für den aktuellen Tag neu berechnen.
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Heute", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = "${state.todayCompletedCount}/${state.activeHabitCount} erledigt",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.testTag(TestTags.DASHBOARD_PROGRESS)
        )
        LinearProgressIndicator(
            progress = { state.todayProgress },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            items(state.habits, key = { it.habit.id }) { hp ->
                DashboardHabitRow(hp, onToggle)
            }
        }
    }
}

@Composable
private fun DashboardHabitRow(hp: HabitWithProgress, onToggle: (HabitWithProgress) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = rememberVectorPainter(iconForKey(hp.habit.iconKey)),
                    contentDescription = null,
                    tint = colorFromHex(hp.habit.colorHex),
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(hp.habit.name, fontWeight = FontWeight.Medium)
                    Text(
                        text = "🔥 ${hp.currentStreak}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag(TestTags.dashStreak(hp.habit.name))
                    )
                }
            }
            if (hp.completedToday) {
                Button(
                    onClick = { onToggle(hp) },
                    modifier = Modifier.testTag(TestTags.dashToggle(hp.habit.name))
                ) { Text("Erledigt") }
            } else {
                OutlinedButton(
                    onClick = { onToggle(hp) },
                    modifier = Modifier.testTag(TestTags.dashToggle(hp.habit.name))
                ) { Text("Offen") }
            }
        }
    }
}
