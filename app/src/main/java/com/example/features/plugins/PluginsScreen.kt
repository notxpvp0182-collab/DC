package com.example.features.plugins

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.home.getPluginIcon
import com.example.plugin.PluginManager
import com.example.plugin.PluginRuntimeInfo
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSearchBar

enum class PluginTab(val title: String) {
    INSTALLED("Installed"),
    AVAILABLE("Available"),
    UPDATES("Updates")
}

@Composable
fun PluginsScreen(
    pluginManager: PluginManager
) {
    val pluginStates by pluginManager.pluginStates.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableStateOf(PluginTab.INSTALLED) }

    var activeDialogPluginInfo by remember { mutableStateOf<PluginRuntimeInfo?>(null) }
    var activeSettingsPluginInfo by remember { mutableStateOf<PluginRuntimeInfo?>(null) }

    val categories = listOf("All", "Utility", "Notifications", "Appearance", "Developer", "Audio")

    val allPlugins = pluginStates.values.toList()

    val filteredPlugins = allPlugins.filter { info ->
        val matchesTab = when (selectedTab) {
            PluginTab.INSTALLED -> info.isInstalled
            PluginTab.AVAILABLE -> !info.isInstalled
            PluginTab.UPDATES -> info.hasUpdate
        }
        val matchesCategory = selectedCategory == "All" || info.manifest.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() ||
                info.manifest.name.contains(searchQuery, ignoreCase = true) ||
                info.manifest.description.contains(searchQuery, ignoreCase = true) ||
                info.manifest.author.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesCategory && matchesSearch
    }

    val installedCount = allPlugins.count { it.isInstalled }
    val availableCount = allPlugins.count { !it.isInstalled }
    val updatesCount = allPlugins.count { it.hasUpdate }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("plugins_screen_scroll"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Search Bar
                ModuxSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search extensions, authors, or categories..."
                )
            }

            // Tab Navigation Row
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == PluginTab.INSTALLED,
                        onClick = { selectedTab = PluginTab.INSTALLED },
                        text = {
                            Text("Installed ($installedCount)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        },
                        modifier = Modifier.testTag("tab_installed_plugins")
                    )
                    Tab(
                        selected = selectedTab == PluginTab.AVAILABLE,
                        onClick = { selectedTab = PluginTab.AVAILABLE },
                        text = {
                            Text("Catalog ($availableCount)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        },
                        modifier = Modifier.testTag("tab_available_plugins")
                    )
                    Tab(
                        selected = selectedTab == PluginTab.UPDATES,
                        onClick = { selectedTab = PluginTab.UPDATES },
                        text = {
                            Text("Updates ($updatesCount)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        },
                        modifier = Modifier.testTag("tab_updates_plugins")
                    )
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                        )
                    }
                }
            }

            // Empty State
            if (filteredPlugins.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = when (selectedTab) {
                                    PluginTab.INSTALLED -> "No installed plugins matching query"
                                    PluginTab.AVAILABLE -> "No catalog plugins found"
                                    PluginTab.UPDATES -> "All plugins are up to date"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredPlugins) { pluginInfo ->
                    PluginCardItem(
                        info = pluginInfo,
                        onToggle = { isChecked ->
                            pluginManager.togglePlugin(pluginInfo.manifest.id, isChecked)
                        },
                        onConfigureClick = {
                            activeSettingsPluginInfo = pluginInfo
                        },
                        onDetailsClick = {
                            activeDialogPluginInfo = pluginInfo
                        },
                        onInstallClick = {
                            pluginManager.installPlugin(pluginInfo.manifest)
                        },
                        onUninstallClick = {
                            pluginManager.uninstallPlugin(pluginInfo.manifest.id)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Details Dialog
        activeDialogPluginInfo?.let { info ->
            // Fetch latest live state
            val latestInfo = pluginStates[info.manifest.id] ?: info
            PluginDetailDialog(
                pluginInfo = latestInfo,
                pluginManager = pluginManager,
                onConfigureClick = {
                    activeDialogPluginInfo = null
                    activeSettingsPluginInfo = latestInfo
                },
                onDismiss = { activeDialogPluginInfo = null }
            )
        }

        // Settings Dialog
        activeSettingsPluginInfo?.let { info ->
            val latestInfo = pluginStates[info.manifest.id] ?: info
            PluginSettingsDialog(
                pluginInfo = latestInfo,
                pluginManager = pluginManager,
                onDismiss = { activeSettingsPluginInfo = null }
            )
        }
    }
}

@Composable
fun PluginCardItem(
    info: PluginRuntimeInfo,
    onToggle: (Boolean) -> Unit,
    onConfigureClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onInstallClick: () -> Unit,
    onUninstallClick: () -> Unit
) {
    val manifest = info.manifest

    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = if (info.isEnabled) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (info.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPluginIcon(manifest.iconName),
                            contentDescription = null,
                            tint = if (info.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = manifest.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            ModuxBadge(
                                text = "v${manifest.version}",
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "by ${manifest.author} • ${manifest.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Switch or Install Button
                if (info.isInstalled) {
                    Switch(
                        checked = info.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.testTag("plugin_switch_${manifest.id}"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                } else {
                    Button(
                        onClick = onInstallClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("install_btn_${manifest.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get", fontSize = 12.sp)
                    }
                }
            }

            // Description
            Text(
                text = manifest.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Error notice if error state
            if (info.lastErrorMessage != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Error: ${info.lastErrorMessage}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // Bottom Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onDetailsClick,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("details_btn_${manifest.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Details", fontSize = 11.sp)
                    }

                    if (info.isInstalled && manifest.settingsSchema.isNotEmpty()) {
                        OutlinedButton(
                            onClick = onConfigureClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("settings_btn_${manifest.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Configure", fontSize = 11.sp)
                        }
                    }
                }

                if (info.isInstalled && !manifest.id.startsWith("modux.quicksettings")) {
                    IconButton(
                        onClick = onUninstallClick,
                        modifier = Modifier.testTag("uninstall_btn_${manifest.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Uninstall",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
