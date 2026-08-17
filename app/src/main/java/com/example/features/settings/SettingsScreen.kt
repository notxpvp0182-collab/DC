package com.example.features.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.AppConfig
import com.example.settings.AnimationLevel
import com.example.settings.AppSettingsState
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeManager
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSectionHeader

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    themeManager: ThemeManager,
    onNavigateToThemes: () -> Unit,
    onNavigateToDeveloper: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by settingsViewModel.uiState.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }
    var exportJsonString by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("settings_screen_scroll"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            ModuxSectionHeader(title = "App Settings", subtitle = "Preferences, Appearance & System Config")
        }

        // Section 1: General
        item {
            SettingsCategoryCard(title = "General", icon = Icons.Default.Tune) {
                // Default Startup Screen
                SettingsDropdownRow(
                    title = "Default Launch Screen",
                    subtitle = "Screen opened when launching ModuX",
                    options = listOf("Home", "Plugins", "Themes", "Settings"),
                    selectedOption = uiState.defaultScreen,
                    onSelect = { settingsViewModel.setDefaultScreen(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // App Language
                SettingsDropdownRow(
                    title = "App Language",
                    subtitle = "Interface localization",
                    options = listOf("English (US)", "Spanish (ES)", "Japanese (JA)", "German (DE)", "French (FR)"),
                    selectedOption = uiState.language,
                    onSelect = { settingsViewModel.setLanguage(it) }
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Confirmation Dialogs
                SettingsSwitchRow(
                    title = "Action Confirmations",
                    subtitle = "Confirm before disabling or removing plugins",
                    checked = uiState.requireConfirmations,
                    onCheckedChange = { settingsViewModel.setConfirmations(it) },
                    testTag = "setting_confirmations_switch"
                )
            }
        }

        // Section 2: Appearance
        item {
            SettingsCategoryCard(title = "Appearance", icon = Icons.Default.Palette) {
                // Active Theme shortcut
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToThemes)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(currentTheme.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                    OutlinedButton(onClick = onNavigateToThemes, shape = RoundedCornerShape(8.dp)) {
                        Text("Change", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Compact UI
                SettingsSwitchRow(
                    title = "Compact Mode",
                    subtitle = "Reduces vertical padding for high information density",
                    checked = uiState.isCompactMode,
                    onCheckedChange = { settingsViewModel.setCompactMode(it) },
                    testTag = "setting_compact_switch"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Dynamic Material You Colors
                SettingsSwitchRow(
                    title = "Dynamic System Colors",
                    subtitle = "Derive primary accents from wallpaper on Android 12+",
                    checked = uiState.isDynamicColors,
                    onCheckedChange = { settingsViewModel.setDynamicColors(it) },
                    testTag = "setting_dynamic_colors_switch"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Animation Level
                SettingsDropdownRow(
                    title = "Animation Level",
                    subtitle = "Transitions and card motion speed",
                    options = listOf(AnimationLevel.FULL.name, AnimationLevel.REDUCED.name, AnimationLevel.OFF.name),
                    selectedOption = uiState.animationLevel.name,
                    onSelect = {
                        try {
                            settingsViewModel.setAnimationLevel(AnimationLevel.valueOf(it))
                        } catch (_: Exception) {}
                    }
                )
            }
        }

        // Section 3: Plugin Settings
        item {
            SettingsCategoryCard(title = "Plugin System", icon = Icons.Default.Extension) {
                SettingsSwitchRow(
                    title = "Auto-enable Installed Plugins",
                    subtitle = "Automatically activate plugins upon successful installation",
                    checked = uiState.autoEnableInstalledPlugins,
                    onCheckedChange = { settingsViewModel.setAutoEnablePlugins(it) },
                    testTag = "setting_auto_enable_switch"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                SettingsSwitchRow(
                    title = "Check for Plugin Updates",
                    subtitle = "Periodically verify catalog plugin versions",
                    checked = uiState.checkPluginUpdates,
                    onCheckedChange = { settingsViewModel.setCheckUpdates(it) },
                    testTag = "setting_check_updates_switch"
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                SettingsSwitchRow(
                    title = "Show Experimental Plugins",
                    subtitle = "Allow installing beta-level plugins in the catalog",
                    checked = uiState.showExperimentalPlugins,
                    onCheckedChange = { settingsViewModel.setExperimentalPlugins(it) },
                    testTag = "setting_experimental_switch"
                )
            }
        }

        // Section 4: Storage & Backup
        item {
            SettingsCategoryCard(title = "Storage & Backup", icon = Icons.Default.Storage) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportJsonString = settingsViewModel.exportFullConfiguration()
                            showExportDialog = true
                        },
                        modifier = Modifier.weight(1f).testTag("export_config_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export JSON", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f).testTag("import_config_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import JSON", fontSize = 12.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reset All Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Restore factory defaults for themes and settings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(
                        onClick = { showResetConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("reset_all_settings_btn")
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section 5: Developer Mode (Unlocked or Unlocking)
        item {
            SettingsCategoryCard(title = "Developer Mode", icon = Icons.Default.BugReport) {
                if (uiState.isDeveloperModeEnabled) {
                    SettingsSwitchRow(
                        title = "Enable Developer Console",
                        subtitle = "Inspect live logs, telemetry, and memory diagnostics",
                        checked = uiState.isDeveloperModeEnabled,
                        onCheckedChange = { settingsViewModel.setDeveloperMode(it) },
                        testTag = "setting_developer_mode_switch"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = onNavigateToDeveloper,
                        modifier = Modifier.fillMaxWidth().testTag("open_developer_console_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.BugReport, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Developer Console", fontSize = 12.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Developer Mode is currently locked. Tap 'App Version' below ${AppConfig.DEVELOPER_TAP_THRESHOLD} times to unlock.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Section 6: About
        item {
            SettingsCategoryCard(title = "About ModuX", icon = Icons.Default.Info) {
                // Version Row with Easter Egg Tap Target
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            val msg = settingsViewModel.onVersionTapped()
                            if (msg != null) {
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("app_version_tap_target"),
                    color = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("App Version", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            Text("Click to verify build version", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ModuxBadge(
                            text = AppConfig.APP_VERSION,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Architecture & Framework", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "100% Original Kotlin & Jetpack Compose customization hub. Built with modular plugin lifecycle management, sandboxed contexts, and non-intrusive theme injection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("License", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("MIT Open Source", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Export Dialog
    if (showExportDialog) {
        Dialog(
            onDismissRequest = { showExportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("export_dialog"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Export Configuration Snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Copy or backup your active themes, enabled plugins, and custom settings:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = exportJsonString,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(exportJsonString))
                                Toast.makeText(context, "Copied JSON configuration to clipboard!", Toast.LENGTH_SHORT).show()
                                showExportDialog = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy to Clipboard")
                        }
                        OutlinedButton(onClick = { showExportDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Import Dialog
    if (showImportDialog) {
        var importInput by remember { mutableStateOf("") }
        Dialog(
            onDismissRequest = { showImportDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("import_dialog"),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Import Configuration Snapshot", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Paste a valid ModuX configuration JSON payload to restore settings:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    OutlinedTextField(
                        value = importInput,
                        onValueChange = { importInput = it },
                        placeholder = { Text("Paste JSON snapshot here...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val success = settingsViewModel.importFullConfiguration(importInput)
                                if (success) {
                                    Toast.makeText(context, "Configuration restored successfully!", Toast.LENGTH_SHORT).show()
                                    showImportDialog = false
                                } else {
                                    Toast.makeText(context, "Failed to parse JSON configuration.", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply Backup")
                        }
                        OutlinedButton(onClick = { showImportDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Reset Confirm Dialog
    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset All Settings?") },
            text = { Text("This will clear all local preferences, disable extra plugins, and restore default theme configurations.") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetAllSettings()
                        showResetConfirmDialog = false
                        Toast.makeText(context, "Settings reset to defaults.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            }
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag),
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun SettingsDropdownRow(
    title: String,
    subtitle: String,
    options: List<String>,
    selectedOption: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Box {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expanded = true }
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(selectedOption, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Select", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, fontSize = 13.sp) },
                        onClick = {
                            onSelect(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
