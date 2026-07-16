package com.hudnah.habitlab.domain

import java.time.LocalDate

/**
 * Reine Berechnungslogik der Aktivitätsserien (TWP §6.3).
 * Arbeitet auf Epoch-Day-Werten (LocalDate.toEpochDay()); mehrere Einträge am
 * selben Tag zählen nur einmal.
 */
class StreakCalculator(
    private val today: () -> LocalDate = { LocalDate.now() }
) {

    /**
     * Zählt aufeinanderfolgende aktive Tage bis heute. Fehlt der heutige Tag,
     * ist die aktuelle Streak 0.
     */
    fun currentStreak(entryDays: List<Long>): Int {
        val days = entryDays.toHashSet()
        val todayEpoch = today().toEpochDay()
        if (todayEpoch !in days) return 0
        var count = 0
        var day = todayEpoch
        while (day in days) {
            count++
            day--
        }
        return count
    }

    /** Längste je erreichte, lückenlose Aktivitätsserie. */
    fun bestStreak(entryDays: List<Long>): Int {
        if (entryDays.isEmpty()) return 0
        val sorted = entryDays.toSortedSet().toList()
        var best = 1
        var run = 1
        for (i in 1 until sorted.size) {
            run = if (sorted[i] == sorted[i - 1] + 1) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }
}
