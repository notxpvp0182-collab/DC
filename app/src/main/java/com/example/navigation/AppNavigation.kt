package com.example.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.developer.DeveloperViewModel
import com.example.features.developer.DeveloperScreen
import com.example.features.home.HomeScreen
import com.example.features.plugins.PluginsScreen
import com.example.features.settings.SettingsScreen
import com.example.features.themes.ThemesScreen
import com.example.plugin.PluginManager
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeManager

@Composable
fun ModuxAppNavigation(
    navController: NavHostController,
    pluginManager: PluginManager,
    themeManager: ThemeManager,
    settingsViewModel: SettingsViewModel,
    developerViewModel: DeveloperViewModel
) {
    val settingsState by settingsViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val navigationItems = buildList {
        add(Screen.Home)
        add(Screen.Plugins)
        add(Screen.Themes)
        add(Screen.Settings)
        if (settingsState.isDeveloperModeEnabled) {
            add(Screen.Developer)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                tonalElevation = 4.dp
            ) {
                navigationItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}"),
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(220)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    pluginManager = pluginManager,
                    themeManager = themeManager,
                    settingsViewModel = settingsViewModel,
                    onNavigateToPlugins = { navController.navigate(Screen.Plugins.route) },
                    onNavigateToThemes = { navController.navigate(Screen.Themes.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                )
            }

            composable(Screen.Plugins.route) {
                PluginsScreen(pluginManager = pluginManager)
            }

            composable(Screen.Themes.route) {
                ThemesScreen(themeManager = themeManager)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    themeManager = themeManager,
                    onNavigateToThemes = { navController.navigate(Screen.Themes.route) },
                    onNavigateToDeveloper = { navController.navigate(Screen.Developer.route) }
                )
            }

            composable(Screen.Developer.route) {
                DeveloperScreen(developerViewModel = developerViewModel)
            }
        }
    }
}
