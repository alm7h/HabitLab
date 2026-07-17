package com.hudnah.habitlab.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Auswählbare Icons (iconKey) für die Habit-Erstellung (TWP §5.1). */
val HabitIcons: List<Pair<String, ImageVector>> = listOf(
    "reading" to Icons.Filled.MenuBook,
    "cycling" to Icons.Filled.DirectionsBike,
    "coding" to Icons.Filled.Code,
    "running" to Icons.Filled.DirectionsRun,
    "sport" to Icons.Filled.SportsSoccer,
    "fitness" to Icons.Filled.FitnessCenter,
    "meditation" to Icons.Filled.SelfImprovement,
    "other" to Icons.Filled.Star
)

fun iconForKey(key: String): ImageVector =
    HabitIcons.firstOrNull { it.first == key }?.second ?: Icons.Filled.Star

/** Auswählbare Farben (colorHex) für die Habit-Erstellung (TWP §5.1). */
val HabitColors: List<String> = listOf(
    "#EF5350", // rot
    "#EC407A", // pink
    "#AB47BC", // violett
    "#5C6BC0", // indigo
    "#42A5F5", // blau
    "#26A69A", // teal
    "#66BB6A", // grün
    "#FFA726", // orange
    "#2E7D32", // dunkelgrün
    "#FDD835", // gelb
)

fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color(0xFF42A5F5)
}
