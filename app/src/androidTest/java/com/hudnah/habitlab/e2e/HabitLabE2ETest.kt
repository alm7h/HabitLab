package com.hudnah.habitlab.e2e

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hudnah.habitlab.ui.TestTags
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

/** End-to-End-Tests über die reale UI (TWP §7.2). */
@RunWith(AndroidJUnit4::class)
class HabitLabE2ETest : E2ETestBase() {

    private fun toggleOnDashboard(name: String) {
        waitForTag(TestTags.dashToggle(name))
        composeRule.onNodeWithTag(TestTags.dashToggle(name)).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun E2E_HabitLifecycle() {
        createHabit("Lesen")

        navigateTo(TestTags.NAV_DASHBOARD)
        waitForText("Lesen")

        toggleOnDashboard("Lesen")
        waitForText("1/1 erledigt")
        composeRule.onNodeWithTag(TestTags.DASHBOARD_PROGRESS).assertTextEquals("1/1 erledigt")
        composeRule.onNodeWithTag(TestTags.dashStreak("Lesen")).assertTextEquals("🔥 1")

        navigateTo(TestTags.NAV_STATS)
        waitForTag(TestTags.HEATMAP_TODAY)
        composeRule.onNodeWithTag(TestTags.HEATMAP_TODAY).assertContentDescriptionEquals("filled")
    }

    @Test
    fun E2E_CompleteAndUndo() {
        createHabit("Wasser")
        navigateTo(TestTags.NAV_DASHBOARD)

        toggleOnDashboard("Wasser")
        waitForText("1/1 erledigt")
        composeRule.onNodeWithTag(TestTags.dashStreak("Wasser")).assertTextEquals("🔥 1")

        // Rückgängig machen.
        toggleOnDashboard("Wasser")
        waitForText("0/1 erledigt")
        composeRule.onNodeWithTag(TestTags.dashStreak("Wasser")).assertTextEquals("🔥 0")
    }

    /** Wartet auf den Tages-Reset, markiert erledigt und erwartet die Ziel-Streak. */
    private fun completeDayExpectingStreak(name: String, expectedStreak: Int) {
        // Auf Neuberechnung des aktuellen Tages warten (Habit noch offen).
        waitForText("0/1 erledigt")
        toggleOnDashboard(name)
        waitForText("🔥 $expectedStreak")
    }

    @Test
    fun E2E_MultiDayStreak() {
        setDay(LocalDate.of(2026, 7, 15))
        createHabit("Serie")
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Serie", 1)

        setDay(LocalDate.of(2026, 7, 16))
        navigateTo(TestTags.NAV_HABITS)
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Serie", 2)

        setDay(LocalDate.of(2026, 7, 17))
        navigateTo(TestTags.NAV_HABITS)
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Serie", 3)

        val habitId = allHabits().first().id
        assertEquals(3, streakOf(habitId)!!.currentStreak)
        assertEquals(3, streakOf(habitId)!!.bestStreak)

        navigateTo(TestTags.NAV_STATS)
        waitForTag(TestTags.HEATMAP_TODAY)
        composeRule.onNodeWithTag(TestTags.HEATMAP_TODAY).assertContentDescriptionEquals("filled")
    }

    @Test
    fun E2E_StreakBreaksOnGap() {
        setDay(LocalDate.of(2026, 7, 14))
        createHabit("Gap")
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Gap", 1)

        setDay(LocalDate.of(2026, 7, 15))
        navigateTo(TestTags.NAV_HABITS)
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Gap", 2)

        // 16. Juli ausgelassen -> Serie beginnt beim nächsten Abschluss neu.
        setDay(LocalDate.of(2026, 7, 17))
        navigateTo(TestTags.NAV_HABITS)
        navigateTo(TestTags.NAV_DASHBOARD)
        completeDayExpectingStreak("Gap", 1)

        val habitId = allHabits().first().id
        assertEquals(1, streakOf(habitId)!!.currentStreak)
        // bestStreak bleibt erhalten.
        assertEquals(2, streakOf(habitId)!!.bestStreak)
    }

    @Test
    fun E2E_ArchiveHabit() {
        createHabit("Archiviert")

        composeRule.onNodeWithTag(TestTags.archiveHabit("Archiviert")).performClick()
        tagGone(TestTags.habitRow("Archiviert"))

        // Verschwindet aus der aktiven Liste, bleibt aber in der DB.
        assertTrue(activeHabits().isEmpty())
        assertEquals(1, allHabits().size)
        assertTrue(allHabits().first().isArchived)

        navigateTo(TestTags.NAV_DASHBOARD)
        waitForText("0/0 erledigt")
    }

    @Test
    fun E2E_DeleteHabitRemovesData() {
        createHabit("Loeschen")
        // Als erledigt markieren, damit ein Eintrag existiert.
        composeRule.onNodeWithTag(TestTags.toggleHabit("Loeschen")).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TestTags.deleteHabit("Loeschen")).performClick()
        tagGone(TestTags.habitRow("Loeschen"))

        // Habit und Einträge sind aus der DB (und allen Screens) verschwunden.
        assertTrue(allHabits().isEmpty())
        assertTrue(allEntries().isEmpty())

        navigateTo(TestTags.NAV_DASHBOARD)
        waitForText("0/0 erledigt")
        navigateTo(TestTags.NAV_STATS)
        waitForText("Statistik")
    }

    @Test
    fun E2E_Navigation() {
        // Habits-Screen: FAB sichtbar.
        navigateTo(TestTags.NAV_HABITS)
        waitForTag(TestTags.ADD_HABIT_FAB)
        composeRule.onNodeWithTag(TestTags.ADD_HABIT_FAB).assertIsDisplayed()

        // Stats-Screen: Wochenquote sichtbar.
        navigateTo(TestTags.NAV_STATS)
        waitForTag(TestTags.WEEKLY_RATE)
        composeRule.onNodeWithTag(TestTags.WEEKLY_RATE).assertIsDisplayed()

        // Dashboard-Screen: Tagesfortschritt sichtbar.
        navigateTo(TestTags.NAV_DASHBOARD)
        waitForTag(TestTags.DASHBOARD_PROGRESS)
        composeRule.onNodeWithTag(TestTags.DASHBOARD_PROGRESS).assertIsDisplayed()
    }

    @Test
    fun E2E_OfflinePersistence() {
        createHabit("Offline")
        navigateTo(TestTags.NAV_DASHBOARD)
        toggleOnDashboard("Offline")
        waitForText("1/1 erledigt")

        // Prozess-/Activity-Neustart: Daten stammen aus der persistenten Room-DB.
        composeRule.activityRule.scenario.recreate()
        composeRule.waitForIdle()

        waitForText("Offline")
        composeRule.onNodeWithTag(TestTags.DASHBOARD_PROGRESS).assertTextEquals("1/1 erledigt")

        val habitId = allHabits().first().id
        assertNotNull(streakOf(habitId))
        assertEquals(1, allEntries().size)
    }
}
