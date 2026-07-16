package com.hudnah.habitlab

import com.hudnah.habitlab.domain.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/** Unit-Tests der Streak-Berechnungslogik (TWP §6.3). */
class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 7, 17)
    private val t = today.toEpochDay()
    private val calc = StreakCalculator { today }

    @Test
    fun StreakCalculator_currentStreak() {
        // Lückenlose, aufeinanderfolgende Tage bis heute.
        val days = listOf(t - 2, t - 1, t)
        assertEquals(3, calc.currentStreak(days))
    }

    @Test
    fun StreakCalculator_brokenStreak() {
        // Lücke bei t-2 => aktuelle Serie bricht ab (nur t-1, t).
        val days = listOf(t - 4, t - 3, t - 1, t)
        assertEquals(2, calc.currentStreak(days))
    }

    @Test
    fun StreakCalculator_bestStreak() {
        // Längste historische Serie = 3 (t-2..t).
        val days = listOf(t - 6, t - 5, t - 4, t - 2, t - 1, t)
        assertEquals(3, calc.bestStreak(days))
    }

    @Test
    fun StreakCalculator_noEntries() {
        assertEquals(0, calc.currentStreak(emptyList()))
        assertEquals(0, calc.bestStreak(emptyList()))
    }

    @Test
    fun StreakCalculator_todayMissing() {
        // Heute fehlt => aktuelle Streak 0, bestStreak bleibt erhalten.
        val days = listOf(t - 3, t - 2, t - 1)
        assertEquals(0, calc.currentStreak(days))
        assertEquals(3, calc.bestStreak(days))
    }

    @Test
    fun StreakCalculator_bestUnchanged() {
        // Historische Serie 4, aktuelle nur 1 => bestStreak bleibt 4.
        val days = listOf(t - 6, t - 5, t - 4, t - 3, t)
        assertEquals(1, calc.currentStreak(days))
        assertEquals(4, calc.bestStreak(days))
    }

    @Test
    fun StreakCalculator_duplicateDayCountedOnce() {
        // Mehrere Einträge am selben Tag zählen nur einmal.
        val days = listOf(t, t, t - 1, t - 1)
        assertEquals(2, calc.currentStreak(days))
        assertEquals(2, calc.bestStreak(days))
    }
}
