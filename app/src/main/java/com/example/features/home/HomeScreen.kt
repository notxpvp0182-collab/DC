package com.example.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.AppConfig
import com.example.plugin.PluginManager
import com.example.plugin.PluginRuntimeInfo
import com.example.settings.AppSettingsState
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeDefinition
import com.example.theme.ThemeManager
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSectionHeader

@Composable
fun HomeScreen(
    pluginManager: PluginManager,
    themeManager: ThemeManager,
    settingsViewModel: SettingsViewModel,
    onNavigateToPlugins: () -> Unit,
    onNavigateToThemes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDeveloper: () -> Unit
) {
    val pluginStates by pluginManager.pluginStates.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()

    val activePlugins = pluginStates.values.filter { it.isInstalled && it.isEnabled }
    val installedPlugins = pluginStates.values.filter { it.isInstalled }

    val isWelcomePluginActive = pluginStates["modux.welcome"]?.isEnabled == true
    val isQuickSettingsActive = pluginStates["modux.quicksettings"]?.isEnabled == true

    val customGreeting = pluginStates["modux.welcome"]?.settingsValues?.get("custom_greeting_name")?.toString() ?: "Customizer"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Hero App Header
            ModuxHeroHeader(
                appName = AppConfig.APP_NAME,
                version = AppConfig.APP_VERSION,
                activeCount = activePlugins.size,
                currentTheme = currentTheme,
                isDeveloperMode = settingsState.isDeveloperModeEnabled
            )
        }

        // Welcome Hub Plugin Widget (if active)
        if (isWelcomePluginActive) {
            item {
                ModuxWelcomeCard(
                    greetingName = customGreeting,
                    onExploreClick = onNavigateToPlugins
                )
            }
        }

        // Quick Settings Plugin Widget (if active)
        if (isQuickSettingsActive) {
            item {
                ModuxQuickTilesCard(
                    settingsState = settingsState,
                    settingsViewModel = settingsViewModel,
                    themeManager = themeManager,
                    currentTheme = currentTheme
                )
            }
        }

        // Status & Quick Navigation Grid
        item {
            ModuxSectionHeader(
                title = "Customization Hub",
                subtitle = "Quick shortcuts to manage your experience"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuxHubActionTile(
                    title = "Plugins",
                    subtitle = "${activePlugins.size} active of ${installedPlugins.size}",
                    icon = Icons.Default.Extension,
                    modifier = Modifier.weight(1f),
                    testTag = "home_nav_plugins_btn",
                    onClick = onNavigateToPlugins
                )
                ModuxHubActionTile(
                    title = "Themes",
                    subtitle = currentTheme.name,
                    icon = Icons.Default.Palette,
                    modifier = Modifier.weight(1f),
                    testTag = "home_nav_themes_btn",
                    onClick = onNavigateToThemes
                )
            }
        }

        // Secondary Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModuxHubActionTile(
                    title = "Settings",
                    subtitle = "App preferences",
                    icon = Icons.Default.Settings,
                    modifier = Modifier.weight(1f),
                    testTag = "home_nav_settings_btn",
                    onClick = onNavigateToSettings
                )
                if (settingsState.isDeveloperModeEnabled) {
                    ModuxHubActionTile(
                        title = "Developer",
                        subtitle = "Logs & Telemetry",
                        icon = Icons.Default.BugReport,
                        modifier = Modifier.weight(1f),
                        testTag = "home_nav_dev_btn",
                        onClick = onNavigateToDeveloper
                    )
                }
            }
        }

        // Active Plugins Quick Toggle Section
        item {
            ModuxSectionHeader(
                title = "Installed Plugins",
                subtitle = "Fast toggle switches for active extensions",
                action = {
                    Text(
                        text = "View All (${installedPlugins.size})",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable(onClick = onNavigateToPlugins)
                            .padding(4.dp)
                            .testTag("home_view_all_plugins_btn")
                    )
                }
            )
        }

        items(installedPlugins.take(4)) { pluginInfo ->
            ModuxMiniPluginCard(
                info = pluginInfo,
                onToggle = { isChecked ->
                    pluginManager.togglePlugin(pluginInfo.manifest.id, isChecked)
                },
                onClick = onNavigateToPlugins
            )
        }

        // Performance & Health Summary Card
        item {
            ModuxPerformanceSummaryCard(
                activePluginCount = activePlugins.size,
                isDevMode = settingsState.isDeveloperModeEnabled,
                onDevClick = onNavigateToDeveloper
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ModuxHeroHeader(
    appName: String,
    version: String,
    activeCount: Int,
    currentTheme: ThemeDefinition,
    isDeveloperMode: Boolean
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // App Icon / Logo Box
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "App Logo",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Customization Hub",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Version & Dev Badge
                Column(horizontalAlignment = Alignment.End) {
                    ModuxBadge(
                        text = "v$version",
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                    if (isDeveloperMode) {
                        Spacer(modifier = Modifier.height(4.dp))
                        ModuxBadge(
                            text = "DEV MODE",
                            containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                            contentColor = Color(0xFFEF4444),
                            icon = Icons.Default.BugReport
                        )
                    }
                }
            }

            // Quick Info Badges Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Active Plugins",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$activeCount running",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(currentTheme.toAccentColor())
                        )
                        Column {
                            Text(
                                text = "Current Theme",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentTheme.name,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModuxWelcomeCard(
    greetingName: String,
    onExploreClick: () -> Unit
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Welcome back, $greetingName!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Tip of the day: Pair the 'Cyberpunk Violet' theme with 'Custom Accent Styler' for glowing highlight borders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModuxQuickTilesCard(
    settingsState: AppSettingsState,
    settingsViewModel: SettingsViewModel,
    themeManager: ThemeManager,
    currentTheme: ThemeDefinition
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Quick Action Tiles",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Tile 1: Compact Mode
                QuickTileButton(
                    title = "Compact",
                    isActive = settingsState.isCompactMode,
                    icon = Icons.Default.Tune,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        settingsViewModel.setCompactMode(!settingsState.isCompactMode)
                    }
                )

                // Tile 2: Dynamic Colors
                QuickTileButton(
                    title = "Dynamic",
                    isActive = settingsState.isDynamicColors,
                    icon = Icons.Default.Palette,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        settingsViewModel.setDynamicColors(!settingsState.isDynamicColors)
                    }
                )

                // Tile 3: Dark/Light Quick Switch
                QuickTileButton(
                    title = if (currentTheme.isDark) "AMOLED" else "Dark",
                    isActive = currentTheme.id == "amoled_black",
                    icon = Icons.Default.FlashOn,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val next = if (currentTheme.id == "amoled_black") "default_dark" else "amoled_black"
                        themeManager.applyTheme(next)
                    }
                )
            }
        }
    }
}

@Composable
fun QuickTileButton(
    title: String,
    isActive: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
        border = BorderStroke(
            1.dp,
            if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ModuxHubActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ModuxMiniPluginCard(
    info: PluginRuntimeInfo,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (info.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (info.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getPluginIcon(info.manifest.iconName),
                        contentDescription = null,
                        tint = if (info.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = info.manifest.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "v${info.manifest.version}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = info.manifest.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Switch(
                checked = info.isEnabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("switch_${info.manifest.id}"),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    }
}

@Composable
fun ModuxPerformanceSummaryCard(
    activePluginCount: Int,
    isDevMode: Boolean,
    onDevClick: () -> Unit
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "System Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Optimal",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                text = "ModuX modular framework is operating cleanly with $activePluginCount plugins hooked into memory. No active bottlenecks detected.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isDevMode) {
                OutlinedButton(
                    onClick = onDevClick,
                    modifier = Modifier.fillMaxWidth().testTag("home_inspect_diagnostics_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inspect Developer Diagnostics", fontSize = 12.sp)
                }
            }
        }
    }
}

fun getPluginIcon(name: String): ImageVector {
    return when (name) {
        "tune" -> Icons.Default.Tune
        "notifications" -> Icons.Default.FlashOn
        "palette" -> Icons.Default.Palette
        "view_compact" -> Icons.Default.Tune
        "bug_report" -> Icons.Default.BugReport
        "auto_awesome" -> Icons.Default.AutoAwesome
        "volume_up" -> Icons.Default.FlashOn
        "font_download" -> Icons.Default.Palette
        "touch_app" -> Icons.Default.Extension
        else -> Icons.Default.Extension
    }
}
