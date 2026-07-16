package com.hudnah.habitlab.ui

/** Zentrale Test-Tags, damit UI und E2E-Tests konsistent bleiben (TWP §7.2). */
object TestTags {
    // Navigation
    const val NAV_DASHBOARD = "nav_dashboard"
    const val NAV_HABITS = "nav_habits"
    const val NAV_STATS = "nav_stats"

    // Dashboard
    const val DASHBOARD_PROGRESS = "dashboard_today_progress"
    fun dashToggle(name: String) = "dash_toggle_$name"
    fun dashStreak(name: String) = "dash_streak_$name"

    // Habits
    const val ADD_HABIT_FAB = "add_habit_fab"
    const val HABIT_NAME_FIELD = "habit_name_field"
    const val SAVE_HABIT_BUTTON = "save_habit_button"
    fun habitRow(name: String) = "habit_row_$name"
    fun toggleHabit(name: String) = "toggle_$name"
    fun archiveHabit(name: String) = "archive_$name"
    fun deleteHabit(name: String) = "delete_$name"

    // Stats
    const val WEEKLY_RATE = "stats_weekly_rate"
    const val HEATMAP_TODAY = "heatmap_today"
}
