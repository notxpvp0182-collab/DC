package com.example.developer

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppConfig
import com.example.core.AppLogger
import com.example.core.EventBus
import com.example.core.LogEntry
import com.example.core.LogLevel
import com.example.core.ModuxEvent
import com.example.plugin.PluginManager
import com.example.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SystemTelemetry(
    val androidVersion: String = "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})",
    val deviceModel: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val usedMemoryMb: Long = 0,
    val freeMemoryMb: Long = 0,
    val totalMemoryMb: Long = 0,
    val maxMemoryMb: Long = 0,
    val activeThreads: Int = 0,
    val uptimeSeconds: Long = 0
)

data class DeveloperUiState(
    val logs: List<LogEntry> = emptyList(),
    val filteredLogs: List<LogEntry> = emptyList(),
    val events: List<ModuxEvent> = emptyList(),
    val selectedLogLevel: LogLevel? = null,
    val searchQuery: String = "",
    val telemetry: SystemTelemetry = SystemTelemetry(),
    val activeTab: DeveloperTab = DeveloperTab.CONSOLE
)

enum class DeveloperTab {
    CONSOLE, EVENTS, TELEMETRY, TOOLS
}

class DeveloperViewModel(
    private val pluginManager: PluginManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeveloperUiState())
    val uiState: StateFlow<DeveloperUiState> = _uiState.asStateFlow()

    private val startTime = System.currentTimeMillis()

    init {
        // Collect real-time logs
        viewModelScope.launch {
            _uiState.update { it.copy(logs = AppLogger.getLogs()) }
            applyFilter()
            AppLogger.logFlow.collect { entry ->
                _uiState.update { current ->
                    val newLogs = current.logs + entry
                    val trimmed = if (newLogs.size > 300) newLogs.takeLast(300) else newLogs
                    current.copy(logs = trimmed)
                }
                applyFilter()
            }
        }

        // Collect real-time events
        viewModelScope.launch {
            _uiState.update { it.copy(events = EventBus.getHistory()) }
            EventBus.events.collect { event ->
                _uiState.update { current ->
                    val newEvents = current.events + event
                    val trimmed = if (newEvents.size > 200) newEvents.takeLast(200) else newEvents
                    current.copy(events = trimmed)
                }
            }
        }

        // Update telemetry periodically
        viewModelScope.launch {
            while (isActive) {
                updateTelemetry()
                delay(2000)
            }
        }
    }

    private fun updateTelemetry() {
        val runtime = Runtime.getRuntime()
        val total = runtime.totalMemory() / (1024 * 1024)
        val free = runtime.freeMemory() / (1024 * 1024)
        val max = runtime.maxMemory() / (1024 * 1024)
        val used = total - free
        val threads = Thread.activeCount()
        val uptime = (System.currentTimeMillis() - startTime) / 1000

        _uiState.update { current ->
            current.copy(
                telemetry = current.telemetry.copy(
                    usedMemoryMb = used,
                    freeMemoryMb = free,
                    totalMemoryMb = total,
                    maxMemoryMb = max,
                    activeThreads = threads,
                    uptimeSeconds = uptime
                )
            )
        }
    }

    fun selectTab(tab: DeveloperTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun setFilterLevel(level: LogLevel?) {
        _uiState.update { it.copy(selectedLogLevel = level) }
        applyFilter()
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilter()
    }

    private fun applyFilter() {
        val current = _uiState.value
        val filtered = current.logs.filter { entry ->
            val matchesLevel = current.selectedLogLevel == null || entry.level == current.selectedLogLevel
            val matchesSearch = current.searchQuery.isBlank() ||
                    entry.message.contains(current.searchQuery, ignoreCase = true) ||
                    entry.tag.contains(current.searchQuery, ignoreCase = true)
            matchesLevel && matchesSearch
        }
        _uiState.update { it.copy(filteredLogs = filtered) }
    }

    fun clearLogs() {
        AppLogger.clearLogs()
        _uiState.update { it.copy(logs = emptyList(), filteredLogs = emptyList()) }
    }

    fun clearEvents() {
        EventBus.clear()
        _uiState.update { it.copy(events = emptyList()) }
    }

    fun reloadPlugins() {
        pluginManager.reloadAllPlugins()
        AppLogger.i("DeveloperConsole", "Triggered hot reload for all active plugins.")
    }

    fun reloadThemes() {
        themeManager.reloadThemes()
        AppLogger.i("DeveloperConsole", "Triggered hot reload for theme catalog.")
    }

    fun triggerGarbageCollection(): String {
        val before = Runtime.getRuntime().freeMemory() / (1024 * 1024)
        System.gc()
        val after = Runtime.getRuntime().freeMemory() / (1024 * 1024)
        updateTelemetry()
        val msg = "System.gc() invoked. Free memory: ${before}MB -> ${after}MB (Δ ${after - before}MB)"
        AppLogger.i("DeveloperConsole", msg)
        return msg
    }

    fun getExportableLogs(): String = AppLogger.exportLogs()
}
