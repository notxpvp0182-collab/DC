package com.example.features.developer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AppConfig
import com.example.core.LogEntry
import com.example.core.LogLevel
import com.example.core.ModuxEvent
import com.example.developer.DeveloperTab
import com.example.developer.DeveloperViewModel
import com.example.developer.SystemTelemetry
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSearchBar
import com.example.ui.components.ModuxSectionHeader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeveloperScreen(
    developerViewModel: DeveloperViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by developerViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("developer_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Developer Console",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Live diagnostics, event stream & debug tools",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ModuxBadge(
                text = "ACTIVE",
                containerColor = Color(0xFFEF4444).copy(alpha = 0.15f),
                contentColor = Color(0xFFEF4444),
                icon = Icons.Default.BugReport
            )
        }

        // Navigation Tabs
        TabRow(
            selectedTabIndex = uiState.activeTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[uiState.activeTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            DeveloperTab.entries.forEach { tab ->
                Tab(
                    selected = uiState.activeTab == tab,
                    onClick = { developerViewModel.selectTab(tab) },
                    text = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_dev_${tab.name.lowercase()}")
                )
            }
        }

        when (uiState.activeTab) {
            DeveloperTab.CONSOLE -> {
                ConsoleLogsTabContent(
                    uiState = uiState,
                    developerViewModel = developerViewModel,
                    onExportClick = {
                        val logs = developerViewModel.getExportableLogs()
                        clipboardManager.setText(AnnotatedString(logs))
                        Toast.makeText(context, "Exported ${uiState.logs.size} log lines to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            DeveloperTab.EVENTS -> {
                EventsTabContent(
                    events = uiState.events,
                    onClearClick = { developerViewModel.clearEvents() }
                )
            }
            DeveloperTab.TELEMETRY -> {
                TelemetryTabContent(telemetry = uiState.telemetry)
            }
            DeveloperTab.TOOLS -> {
                ToolsTabContent(
                    developerViewModel = developerViewModel,
                    onGcClick = {
                        val msg = developerViewModel.triggerGarbageCollection()
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    },
                    onReloadPlugins = {
                        developerViewModel.reloadPlugins()
                        Toast.makeText(context, "Reloaded all active plugins!", Toast.LENGTH_SHORT).show()
                    },
                    onReloadThemes = {
                        developerViewModel.reloadThemes()
                        Toast.makeText(context, "Theme definitions reloaded!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun ConsoleLogsTabContent(
    uiState: com.example.developer.DeveloperUiState,
    developerViewModel: DeveloperViewModel,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search and Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ModuxSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { developerViewModel.setSearchQuery(it) },
                    placeholder = "Filter logs..."
                )
            }

            IconButton(
                onClick = onExportClick,
                modifier = Modifier.testTag("export_logs_btn")
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy logs", tint = MaterialTheme.colorScheme.primary)
            }

            IconButton(
                onClick = { developerViewModel.clearLogs() },
                modifier = Modifier.testTag("clear_logs_btn")
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear logs", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Log Level Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedLogLevel == null,
                    onClick = { developerViewModel.setFilterLevel(null) },
                    label = { Text("ALL (${uiState.logs.size})", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
            items(LogLevel.entries) { level ->
                val isSelected = uiState.selectedLogLevel == level
                val count = uiState.logs.count { it.level == level }
                FilterChip(
                    selected = isSelected,
                    onClick = { developerViewModel.setFilterLevel(level) },
                    label = { Text("${level.name} ($count)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getLogLevelColor(level).copy(alpha = 0.2f),
                        selectedLabelColor = getLogLevelColor(level)
                    )
                )
            }
        }

        // Log List Terminal Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .testTag("dev_logs_list"),
            color = Color(0xFF090D16),
            border = BorderStroke(1.dp, Color(0xFF1F293D))
        ) {
            if (uiState.filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No logs matching current filter",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uiState.filteredLogs) { entry ->
                        LogLineItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun LogLineItem(entry: LogEntry) {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    val timeStr = sdf.format(Date(entry.timestamp))
    val levelColor = getLogLevelColor(entry.level)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = timeStr,
            color = Color(0xFF64748B),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = entry.level.name.take(1),
            color = levelColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "[${entry.tag}]",
            color = Color(0xFF94A3B8),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = entry.message,
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EventsTabContent(
    events: List<ModuxEvent>,
    onClearClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Broadcast Events (${events.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClearClick) {
                Icon(Icons.Default.Delete, contentDescription = "Clear events", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No broadcast events recorded yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events.reversed()) { event ->
                    EventItemCard(event = event)
                }
            }
        }
    }
}

@Composable
fun EventItemCard(event: ModuxEvent) {
    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    val time = sdf.format(Date(event.timestamp))

    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
        shapeRadius = 10.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = event.eventType,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = time,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "Source: ${event.source}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (event.payload.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
                        event.payload.forEach { (k, v) ->
                            Text(
                                text = "$k: $v",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TelemetryTabContent(telemetry: SystemTelemetry) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModuxCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "JVM Heap Memory Allocation",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val ratio = if (telemetry.totalMemoryMb > 0) {
                        (telemetry.usedMemoryMb.toFloat() / telemetry.totalMemoryMb.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Used: ${telemetry.usedMemoryMb} MB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Allocated: ${telemetry.totalMemoryMb} MB",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Max: ${telemetry.maxMemoryMb} MB",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            ModuxCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Runtime Environment",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TelemetryInfoRow("Device Model", telemetry.deviceModel)
                    TelemetryInfoRow("Android Release", telemetry.androidVersion)
                    TelemetryInfoRow("Active Threads", "${telemetry.activeThreads} threads")
                    TelemetryInfoRow("Uptime", "${telemetry.uptimeSeconds} seconds")
                    TelemetryInfoRow("ModuX Version", AppConfig.APP_VERSION)
                }
            }
        }
    }
}

@Composable
fun TelemetryInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun ToolsTabContent(
    developerViewModel: DeveloperViewModel,
    onGcClick: () -> Unit,
    onReloadPlugins: () -> Unit,
    onReloadThemes: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModuxCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Lifecycle & Hot Reload",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = onReloadPlugins,
                        modifier = Modifier.fillMaxWidth().testTag("dev_reload_plugins_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Hot Reload All Active Plugins")
                    }

                    OutlinedButton(
                        onClick = onReloadThemes,
                        modifier = Modifier.fillMaxWidth().testTag("dev_reload_themes_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reload Theme Definitions")
                    }
                }
            }
        }

        item {
            ModuxCard(
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Memory Management",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Forces JVM System.gc() invocation to measure memory delta and reclaim unused plugin object handles.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onGcClick,
                        modifier = Modifier.fillMaxWidth().testTag("dev_gc_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Invoke Garbage Collector")
                    }
                }
            }
        }
    }
}

fun getLogLevelColor(level: LogLevel): Color {
    return when (level) {
        LogLevel.DEBUG -> Color(0xFF38BDF8)
        LogLevel.INFO -> Color(0xFF10B981)
        LogLevel.WARNING -> Color(0xFFF59E0B)
        LogLevel.ERROR -> Color(0xFFEF4444)
    }
}
