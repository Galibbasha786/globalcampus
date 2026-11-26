package com.globalcampus.support.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavHostController

sealed class BottomTab(val route: String, val label: String) {
    data object Home : BottomTab(Routes.HOME, "Home")
    data object Community : BottomTab(Routes.COMMUNITY, "Community")
    data object Help : BottomTab(Routes.HELP, "Help")
    data object Chat : BottomTab(Routes.CHAT, "AI Chat")
    data object Profile : BottomTab(Routes.PROFILE, "Profile")
}

@Composable
fun BottomBar(navController: NavHostController) {
    val tabs = listOf(BottomTab.Home, BottomTab.Community, BottomTab.Help, BottomTab.Chat, BottomTab.Profile)
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        when (tab) {
                            BottomTab.Home -> Icons.Default.Home
                            BottomTab.Community -> Icons.Default.Groups
                            BottomTab.Help -> Icons.Default.Support
                            BottomTab.Chat -> Icons.Default.SmartToy
                            BottomTab.Profile -> Icons.Default.Person
                        }, tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}