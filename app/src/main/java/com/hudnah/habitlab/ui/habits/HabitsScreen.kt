package com.hudnah.habitlab.ui.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.domain.model.HabitWithProgress
import com.hudnah.habitlab.ui.HabitColors
import com.hudnah.habitlab.ui.HabitIcons
import com.hudnah.habitlab.ui.TestTags
import com.hudnah.habitlab.ui.colorFromHex
import com.hudnah.habitlab.ui.iconForKey

@Composable
fun HabitsScreen(
    viewModel: HabitViewModel,
    modifier: Modifier = Modifier
) {
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    var editing by remember { mutableStateOf<Habit?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; showDialog = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Habit hinzufügen") },
                text = { Text("Neu") },
                modifier = Modifier.testTag(TestTags.ADD_HABIT_FAB)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.habit.id }) { hp ->
                HabitRow(
                    hp = hp,
                    onToggle = { viewModel.toggleCompletion(hp) },
                    onEdit = { editing = hp.habit; showDialog = true },
                    onArchive = { viewModel.archiveHabit(hp.habit.id) },
                    onDelete = { viewModel.deleteHabit(hp.habit.id) }
                )
            }
        }
    }

    if (showDialog) {
        HabitEditorDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { name, iconKey, colorHex ->
                val current = editing
                if (current == null) {
                    viewModel.createHabit(name, iconKey, colorHex)
                } else {
                    viewModel.updateHabit(current.copy(name = name, iconKey = iconKey, colorHex = colorHex))
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun HabitRow(
    hp: HabitWithProgress,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().testTag(TestTags.habitRow(hp.habit.name))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = rememberVectorPainter(iconForKey(hp.habit.iconKey)),
                contentDescription = null,
                tint = colorFromHex(hp.habit.colorHex),
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(hp.habit.name, fontWeight = FontWeight.Medium)
                Text("🔥 ${hp.currentStreak}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(
                onClick = onToggle,
                modifier = Modifier.testTag(TestTags.toggleHabit(hp.habit.name))
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = if (hp.completedToday) "Erledigt" else "Offen",
                    tint = if (hp.completedToday) colorFromHex(hp.habit.colorHex) else Color.Gray
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_${hp.habit.name}")) {
                Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten")
            }
            IconButton(
                onClick = onArchive,
                modifier = Modifier.testTag(TestTags.archiveHabit(hp.habit.name))
            ) {
                Text("A")
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.testTag(TestTags.deleteHabit(hp.habit.name))
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Löschen")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitEditorDialog(
    initial: Habit?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String, colorHex: String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var iconKey by remember { mutableStateOf(initial?.iconKey ?: HabitIcons.first().first) }
    var colorHex by remember { mutableStateOf(initial?.colorHex ?: HabitColors.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Neue Gewohnheit" else "Gewohnheit bearbeiten") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.HABIT_NAME_FIELD)
                )
                Text("Icon", modifier = Modifier.padding(top = 12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitIcons.forEach { (key, icon) ->
                        Icon(
                            painter = rememberVectorPainter(icon),
                            contentDescription = key,
                            tint = if (key == iconKey) MaterialTheme.colorScheme.primary else Color.Gray,
                            modifier = Modifier.size(32.dp).clickable { iconKey = key }
                        )
                    }
                }
                Text("Farbe", modifier = Modifier.padding(top = 12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HabitColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(colorFromHex(hex), CircleShape)
                                .border(
                                    width = if (hex == colorHex) 3.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    shape = CircleShape
                                )
                                .clickable { colorHex = hex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), iconKey, colorHex) },
                modifier = Modifier.testTag(TestTags.SAVE_HABIT_BUTTON)
            ) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
