package com.hudnah.habitlab.e2e

import android.content.Context
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.hudnah.habitlab.MainActivity
import com.hudnah.habitlab.data.local.AppDatabase
import com.hudnah.habitlab.di.HabitLabClock
import com.hudnah.habitlab.data.local.entity.Entry
import com.hudnah.habitlab.data.local.entity.Habit
import com.hudnah.habitlab.data.local.entity.Streak
import com.hudnah.habitlab.ui.TestTags
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import java.time.LocalDate

/**
 * Basis für End-to-End-Tests über die reale UI (TWP §7.2).
 * Die DB wird pro Test geleert; der Testtag ist über [HabitLabClock] steuerbar.
 */
@OptIn(ExperimentalTestApi::class)
abstract class E2ETestBase {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    protected val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun prepare() {
        // Deterministischer Ausgangstag und leere DB.
        HabitLabClock.today = { LocalDate.of(2026, 7, 17) }
        AppDatabase.getInstance(context).clearAllTables()
    }

    @After
    fun cleanup() {
        AppDatabase.getInstance(context).clearAllTables()
        HabitLabClock.reset()
    }

    protected fun setDay(date: LocalDate) {
        HabitLabClock.today = { date }
    }

    protected fun navigateTo(tag: String) {
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    protected fun waitForTag(tag: String, timeoutMs: Long = 5_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    protected fun waitForText(text: String, timeoutMs: Long = 5_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    protected fun tagGone(tag: String, timeoutMs: Long = 5_000) {
        composeRule.waitUntil(timeoutMs) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
        }
    }

    // --- DB-Inspektion (Persistenz-Prüfungen) ---
    private fun db() = AppDatabase.getInstance(context)
    protected fun allHabits(): List<Habit> = runBlocking { db().habitDao().getAllSnapshot() }
    protected fun activeHabits(): List<Habit> = runBlocking { db().habitDao().getAllActiveSnapshot() }
    protected fun allEntries(): List<Entry> = runBlocking { db().entryDao().getAllSnapshot() }
    protected fun streakOf(habitId: Long): Streak? = runBlocking { db().streakDao().getByHabitSnapshot(habitId) }

    /** Legt über den Habits-Screen eine Gewohnheit an. */
    protected fun createHabit(name: String) {
        navigateTo(TestTags.NAV_HABITS)
        composeRule.onNodeWithTag(TestTags.ADD_HABIT_FAB).performClick()
        composeRule.onNodeWithTag(TestTags.HABIT_NAME_FIELD).performTextInput(name)
        composeRule.onNodeWithTag(TestTags.SAVE_HABIT_BUTTON).performClick()
        waitForTag(TestTags.habitRow(name))
    }
}
