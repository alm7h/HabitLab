package com.hudnah.habitlab.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/** Auswählbare Icons (iconKey) für die Habit-Erstellung (TWP §5.1). */
val HabitIcons: List<Pair<String, ImageVector>> = listOf(
    "star" to Icons.Filled.Star,
    "favorite" to Icons.Filled.Favorite,
    "home" to Icons.Filled.Home,
    "person" to Icons.Filled.Person,
    "settings" to Icons.Filled.Settings,
    "notifications" to Icons.Filled.Notifications,
    "cart" to Icons.Filled.ShoppingCart,
    "calendar" to Icons.Filled.DateRange,
    "check" to Icons.Filled.CheckCircle
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
    "#FFA726"  // orange
)

fun colorFromHex(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: IllegalArgumentException) {
    Color(0xFF42A5F5)
}
