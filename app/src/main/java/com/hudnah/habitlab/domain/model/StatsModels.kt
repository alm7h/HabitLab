package com.hudnah.habitlab.domain.model

import com.hudnah.habitlab.data.local.entity.Habit

/** Auswertung der laufenden Woche (TWP §6.4). */
data class WeeklyStats(
    val completionRate: Float,
    val completionCount: Int
)

/** Monatliche Auswertung, je Habit aufgeschlüsselt (TWP §6.4). */
data class MonthlyStats(
    val completionCount: Int,
    val perHabit: Map<Habit, Int>
)

/** Ein Tag der Aktivitäts-Heatmap: Abschlussquote (0.0–1.0) an einem Kalendertag. */
data class HeatmapDay(
    /** Kalendertag als Epoch-Day. */
    val date: Long,
    val rate: Float
)

/** Aktiver Habit inkl. heutigem Status und aktueller Streak (TWP §6.5). */
data class HabitWithProgress(
    val habit: Habit,
    val completedToday: Boolean,
    val currentStreak: Int
)
