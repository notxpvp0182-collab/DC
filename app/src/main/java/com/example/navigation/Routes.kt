package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Dashboard)
    data object Plugins : Screen("plugins", "Plugins", Icons.Default.Extension)
    data object Themes : Screen("themes", "Themes", Icons.Default.Palette)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Developer : Screen("developer", "Developer", Icons.Default.BugReport)
}
