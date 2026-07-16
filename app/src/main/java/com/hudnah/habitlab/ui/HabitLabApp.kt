package com.hudnah.habitlab.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hudnah.habitlab.di.AppContainer
import com.hudnah.habitlab.ui.dashboard.DashboardScreen
import com.hudnah.habitlab.ui.dashboard.DashboardViewModel
import com.hudnah.habitlab.ui.habits.HabitViewModel
import com.hudnah.habitlab.ui.habits.HabitsScreen
import com.hudnah.habitlab.ui.stats.StatsScreen
import com.hudnah.habitlab.ui.stats.StatsViewModel

private enum class Dest(val route: String, val label: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Home, TestTags.NAV_DASHBOARD),
    HABITS("habits", "Habits", Icons.Filled.CheckCircle, TestTags.NAV_HABITS),
    STATS("stats", "Statistik", Icons.Filled.DateRange, TestTags.NAV_STATS)
}

@Composable
fun HabitLabApp(container: AppContainer) {
    val navController = rememberNavController()
    val factory = HabitLabViewModelFactory(container)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentDest = backStackEntry?.destination
            NavigationBar {
                Dest.entries.forEach { dest ->
                    val selected = currentDest?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                // Kein saveState: beim Wechsel wird der Screen neu aufgebaut und
                                // die Kennzahlen werden für den aktuellen Tag neu berechnet.
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        modifier = Modifier.testTag(dest.tag)
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Dest.DASHBOARD.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.DASHBOARD.route) {
                val dashboardVm: DashboardViewModel = viewModel(factory = factory)
                val habitVm: HabitViewModel = viewModel(factory = factory)
                DashboardScreen(
                    viewModel = dashboardVm,
                    onToggle = { habitVm.toggleCompletion(it) }
                )
            }
            composable(Dest.HABITS.route) {
                val habitVm: HabitViewModel = viewModel(factory = factory)
                HabitsScreen(viewModel = habitVm)
            }
            composable(Dest.STATS.route) {
                val statsVm: StatsViewModel = viewModel(factory = factory)
                StatsScreen(viewModel = statsVm)
            }
        }
    }
}
