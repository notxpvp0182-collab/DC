package com.example.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AppConfig
import com.example.core.AppLogger
import com.example.core.EventBus
import com.example.plugin.PluginManager
import com.example.theme.ThemeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "modux_settings")

enum class AnimationLevel {
    FULL, REDUCED, OFF
}

data class AppSettingsState(
    val language: String = "English (US)",
    val defaultScreen: String = "Discord Web",
    val startupBehavior: String = "Discord Web",
    val requireConfirmations: Boolean = true,
    val animationLevel: AnimationLevel = AnimationLevel.FULL,
    val isCompactMode: Boolean = false,
    val isDynamicColors: Boolean = false,
    val autoEnableInstalledPlugins: Boolean = false,
    val checkPluginUpdates: Boolean = true,
    val showExperimentalPlugins: Boolean = false,
    val isDeveloperModeEnabled: Boolean = false,
    val developerTapCount: Int = 0,

    // Background Image Customization
    val customBackgroundUri: String? = null,
    val backgroundBlur: Float = 0f,
    val backgroundOpacity: Float = 0.85f,
    val backgroundDarkOverlay: Float = 0.35f,
    val backgroundScaleMode: String = "Crop",

    // Performance Mode
    val performanceMode: String = "Balanced",
    val hardwareAcceleration: Boolean = true,

    // Message Tools Preferences
    val enableMessageFormattingPreview: Boolean = true,
    val timestampFormat: String = "12-Hour (3:45 PM)",
    val quickEmojiBar: Boolean = true,

    // Notification Preferences
    val enableInAppNotifications: Boolean = true,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val notificationPriority: String = "High",

    // Voice & Video Preferences
    val noiseSuppression: Boolean = true,
    val preferredVideoQuality: String = "720p HD",
    val autoStartCamera: Boolean = false,

    // Keybinds Map
    val keybinds: Map<String, String> = mapOf(
        "Open Client Hub" to "Ctrl + K",
        "Toggle Theme" to "Ctrl + T",
        "Toggle Compact UI" to "Ctrl + Shift + C",
        "Reload Discord Web" to "Ctrl + R",
        "Open Dev Console" to "Ctrl + Shift + D"
    )
)

class SettingsRepository(private val context: Context) {
    private val dataStore = context.dataStore

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val DEFAULT_SCREEN = stringPreferencesKey("default_screen")
        val STARTUP_BEHAVIOR = stringPreferencesKey("startup_behavior")
        val CONFIRMATIONS = booleanPreferencesKey("require_confirmations")
        val ANIMATION_LEVEL = stringPreferencesKey("animation_level")
        val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val AUTO_ENABLE_PLUGINS = booleanPreferencesKey("auto_enable_plugins")
        val CHECK_UPDATES = booleanPreferencesKey("check_updates")
        val EXPERIMENTAL_PLUGINS = booleanPreferencesKey("experimental_plugins")
        val DEVELOPER_MODE = booleanPreferencesKey("developer_mode")

        val BG_URI = stringPreferencesKey("bg_uri")
        val BG_BLUR = floatPreferencesKey("bg_blur")
        val BG_OPACITY = floatPreferencesKey("bg_opacity")
        val BG_OVERLAY = floatPreferencesKey("bg_overlay")
        val BG_SCALE_MODE = stringPreferencesKey("bg_scale_mode")

        val PERFORMANCE_MODE = stringPreferencesKey("performance_mode")
        val HARDWARE_ACCEL = booleanPreferencesKey("hardware_accel")

        val MSG_PREVIEW = booleanPreferencesKey("msg_preview")
        val TIMESTAMP_FORMAT = stringPreferencesKey("timestamp_format")
        val QUICK_EMOJI = booleanPreferencesKey("quick_emoji")

        val NOTIF_ENABLED = booleanPreferencesKey("notif_enabled")
        val NOTIF_SOUND = booleanPreferencesKey("notif_sound")
        val NOTIF_VIB = booleanPreferencesKey("notif_vib")
        val NOTIF_PRIORITY = stringPreferencesKey("notif_priority")

        val NOISE_SUPPRESSION = booleanPreferencesKey("noise_suppression")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val AUTO_START_CAMERA = booleanPreferencesKey("auto_start_camera")
        val KEYBINDS_JSON = stringPreferencesKey("keybinds_json")
    }

    val settingsFlow = dataStore.data.stateIn(
        scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO),
        started = SharingStarted.Eagerly,
        initialValue = androidx.datastore.preferences.core.emptyPreferences()
    )

    suspend fun updateLanguage(lang: String) = dataStore.edit { it[Keys.LANGUAGE] = lang }
    suspend fun updateDefaultScreen(screen: String) = dataStore.edit { it[Keys.DEFAULT_SCREEN] = screen }
    suspend fun updateStartupBehavior(behavior: String) = dataStore.edit { it[Keys.STARTUP_BEHAVIOR] = behavior }
    suspend fun updateConfirmations(enabled: Boolean) = dataStore.edit { it[Keys.CONFIRMATIONS] = enabled }
    suspend fun updateAnimationLevel(level: AnimationLevel) = dataStore.edit { it[Keys.ANIMATION_LEVEL] = level.name }
    suspend fun updateCompactMode(enabled: Boolean) = dataStore.edit { it[Keys.COMPACT_MODE] = enabled }
    suspend fun updateDynamicColors(enabled: Boolean) = dataStore.edit { it[Keys.DYNAMIC_COLORS] = enabled }
    suspend fun updateAutoEnablePlugins(enabled: Boolean) = dataStore.edit { it[Keys.AUTO_ENABLE_PLUGINS] = enabled }
    suspend fun updateCheckUpdates(enabled: Boolean) = dataStore.edit { it[Keys.CHECK_UPDATES] = enabled }
    suspend fun updateExperimentalPlugins(enabled: Boolean) = dataStore.edit { it[Keys.EXPERIMENTAL_PLUGINS] = enabled }
    suspend fun updateDeveloperMode(enabled: Boolean) = dataStore.edit { it[Keys.DEVELOPER_MODE] = enabled }

    suspend fun updateCustomBackground(uri: String?, blur: Float, opacity: Float, overlay: Float, scaleMode: String) = dataStore.edit {
        if (uri != null) it[Keys.BG_URI] = uri else it.remove(Keys.BG_URI)
        it[Keys.BG_BLUR] = blur
        it[Keys.BG_OPACITY] = opacity
        it[Keys.BG_OVERLAY] = overlay
        it[Keys.BG_SCALE_MODE] = scaleMode
    }

    suspend fun updatePerformanceMode(mode: String) = dataStore.edit { it[Keys.PERFORMANCE_MODE] = mode }
    suspend fun updateHardwareAcceleration(enabled: Boolean) = dataStore.edit { it[Keys.HARDWARE_ACCEL] = enabled }

    suspend fun updateMessagePreferences(preview: Boolean, timestamp: String, quickEmoji: Boolean) = dataStore.edit {
        it[Keys.MSG_PREVIEW] = preview
        it[Keys.TIMESTAMP_FORMAT] = timestamp
        it[Keys.QUICK_EMOJI] = quickEmoji
    }

    suspend fun updateNotificationPreferences(enabled: Boolean, sound: Boolean, vib: Boolean, priority: String) = dataStore.edit {
        it[Keys.NOTIF_ENABLED] = enabled
        it[Keys.NOTIF_SOUND] = sound
        it[Keys.NOTIF_VIB] = vib
        it[Keys.NOTIF_PRIORITY] = priority
    }

    suspend fun updateVoiceVideo(noise: Boolean, quality: String, autoCamera: Boolean) = dataStore.edit {
        it[Keys.NOISE_SUPPRESSION] = noise
        it[Keys.VIDEO_QUALITY] = quality
        it[Keys.AUTO_START_CAMERA] = autoCamera
    }

    suspend fun updateKeybinds(map: Map<String, String>) = dataStore.edit {
        val obj = JSONObject()
        map.forEach { (k, v) -> obj.put(k, v) }
        it[Keys.KEYBINDS_JSON] = obj.toString()
    }

    suspend fun clearAll() = dataStore.edit { it.clear() }
}

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val pluginManager: PluginManager,
    private val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppSettingsState())
    val uiState: StateFlow<AppSettingsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect { prefs ->
                val level = try {
                    AnimationLevel.valueOf(prefs[stringPreferencesKey("animation_level")] ?: AnimationLevel.FULL.name)
                } catch (_: Exception) {
                    AnimationLevel.FULL
                }

                val keybindsJson = prefs[stringPreferencesKey("keybinds_json")]
                val keybindsMap = if (!keybindsJson.isNullOrBlank()) {
                    try {
                        val obj = JSONObject(keybindsJson)
                        val m = mutableMapOf<String, String>()
                        val keys = obj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            m[k] = obj.getString(k)
                        }
                        m
                    } catch (_: Exception) {
                        _uiState.value.keybinds
                    }
                } else {
                    _uiState.value.keybinds
                }

                _uiState.update { current ->
                    current.copy(
                        language = prefs[stringPreferencesKey("app_language")] ?: "English (US)",
                        defaultScreen = prefs[stringPreferencesKey("default_screen")] ?: "Discord Web",
                        startupBehavior = prefs[stringPreferencesKey("startup_behavior")] ?: "Discord Web",
                        requireConfirmations = prefs[booleanPreferencesKey("require_confirmations")] ?: true,
                        animationLevel = level,
                        isCompactMode = prefs[booleanPreferencesKey("compact_mode")] ?: false,
                        isDynamicColors = prefs[booleanPreferencesKey("dynamic_colors")] ?: false,
                        autoEnableInstalledPlugins = prefs[booleanPreferencesKey("auto_enable_plugins")] ?: false,
                        checkPluginUpdates = prefs[booleanPreferencesKey("check_updates")] ?: true,
                        showExperimentalPlugins = prefs[booleanPreferencesKey("experimental_plugins")] ?: false,
                        isDeveloperModeEnabled = prefs[booleanPreferencesKey("developer_mode")] ?: false,
                        customBackgroundUri = prefs[stringPreferencesKey("bg_uri")],
                        backgroundBlur = prefs[floatPreferencesKey("bg_blur")] ?: 0f,
                        backgroundOpacity = prefs[floatPreferencesKey("bg_opacity")] ?: 0.85f,
                        backgroundDarkOverlay = prefs[floatPreferencesKey("bg_overlay")] ?: 0.35f,
                        backgroundScaleMode = prefs[stringPreferencesKey("bg_scale_mode")] ?: "Crop",
                        performanceMode = prefs[stringPreferencesKey("performance_mode")] ?: "Balanced",
                        hardwareAcceleration = prefs[booleanPreferencesKey("hardware_accel")] ?: true,
                        enableMessageFormattingPreview = prefs[booleanPreferencesKey("msg_preview")] ?: true,
                        timestampFormat = prefs[stringPreferencesKey("timestamp_format")] ?: "12-Hour (3:45 PM)",
                        quickEmojiBar = prefs[booleanPreferencesKey("quick_emoji")] ?: true,
                        enableInAppNotifications = prefs[booleanPreferencesKey("notif_enabled")] ?: true,
                        notificationSound = prefs[booleanPreferencesKey("notif_sound")] ?: true,
                        notificationVibration = prefs[booleanPreferencesKey("notif_vib")] ?: true,
                        notificationPriority = prefs[stringPreferencesKey("notif_priority")] ?: "High",
                        noiseSuppression = prefs[booleanPreferencesKey("noise_suppression")] ?: true,
                        preferredVideoQuality = prefs[stringPreferencesKey("video_quality")] ?: "720p HD",
                        autoStartCamera = prefs[booleanPreferencesKey("auto_start_camera")] ?: false,
                        keybinds = keybindsMap
                    )
                }
            }
        }
    }

    fun onVersionTapped(): String? {
        val currentTaps = _uiState.value.developerTapCount + 1
        if (!_uiState.value.isDeveloperModeEnabled) {
            val remaining = AppConfig.DEVELOPER_TAP_THRESHOLD - currentTaps
            if (remaining <= 0) {
                setDeveloperMode(true)
                _uiState.update { it.copy(developerTapCount = 0) }
                AppLogger.i("Settings", "Developer Mode unlocked by user.")
                EventBus.emit("Settings", "DEVELOPER_MODE_UNLOCKED", emptyMap())
                return "Developer Mode is now enabled!"
            } else if (remaining <= 4) {
                _uiState.update { it.copy(developerTapCount = currentTaps) }
                return "You are now $remaining steps away from being a developer."
            } else {
                _uiState.update { it.copy(developerTapCount = currentTaps) }
            }
        }
        return null
    }

    fun setLanguage(lang: String) = viewModelScope.launch { repository.updateLanguage(lang) }
    fun setDefaultScreen(screen: String) = viewModelScope.launch { repository.updateDefaultScreen(screen) }
    fun setStartupBehavior(behavior: String) = viewModelScope.launch { repository.updateStartupBehavior(behavior) }
    fun setConfirmations(enabled: Boolean) = viewModelScope.launch { repository.updateConfirmations(enabled) }
    fun setAnimationLevel(level: AnimationLevel) = viewModelScope.launch { repository.updateAnimationLevel(level) }
    fun setCompactMode(enabled: Boolean) = viewModelScope.launch { repository.updateCompactMode(enabled) }
    fun setDynamicColors(enabled: Boolean) = viewModelScope.launch { repository.updateDynamicColors(enabled) }
    fun setAutoEnablePlugins(enabled: Boolean) = viewModelScope.launch { repository.updateAutoEnablePlugins(enabled) }
    fun setCheckUpdates(enabled: Boolean) = viewModelScope.launch { repository.updateCheckUpdates(enabled) }
    fun setExperimentalPlugins(enabled: Boolean) = viewModelScope.launch { repository.updateExperimentalPlugins(enabled) }
    fun setDeveloperMode(enabled: Boolean) = viewModelScope.launch { repository.updateDeveloperMode(enabled) }

    fun setCustomBackground(uri: String?, blur: Float, opacity: Float, overlay: Float, scaleMode: String) = viewModelScope.launch {
        repository.updateCustomBackground(uri, blur, opacity, overlay, scaleMode)
        AppLogger.i("Settings", "Custom background updated (URI: $uri, Blur: $blur, Opacity: $opacity)")
        EventBus.emit("Settings", "BACKGROUND_UPDATED", mapOf("hasBackground" to (uri != null).toString()))
    }

    fun setPerformanceMode(mode: String) = viewModelScope.launch { repository.updatePerformanceMode(mode) }
    fun setHardwareAcceleration(enabled: Boolean) = viewModelScope.launch { repository.updateHardwareAcceleration(enabled) }

    fun setMessagePreferences(preview: Boolean, timestamp: String, quickEmoji: Boolean) = viewModelScope.launch {
        repository.updateMessagePreferences(preview, timestamp, quickEmoji)
    }

    fun setNotificationPreferences(enabled: Boolean, sound: Boolean, vib: Boolean, priority: String) = viewModelScope.launch {
        repository.updateNotificationPreferences(enabled, sound, vib, priority)
    }

    fun setVoiceVideoPreferences(noise: Boolean, quality: String, autoCamera: Boolean) = viewModelScope.launch {
        repository.updateVoiceVideo(noise, quality, autoCamera)
    }

    fun updateKeybind(action: String, shortcut: String) = viewModelScope.launch {
        val updated = _uiState.value.keybinds.toMutableMap()
        updated[action] = shortcut
        repository.updateKeybinds(updated)
    }

    fun deleteKeybind(action: String) = viewModelScope.launch {
        val updated = _uiState.value.keybinds.toMutableMap()
        updated.remove(action)
        repository.updateKeybinds(updated)
    }

    fun exportFullConfiguration(): String {
        val root = JSONObject()
        root.put("modux_version", AppConfig.APP_VERSION)
        root.put("timestamp", System.currentTimeMillis())

        val settingsObj = JSONObject().apply {
            put("language", _uiState.value.language)
            put("defaultScreen", _uiState.value.defaultScreen)
            put("startupBehavior", _uiState.value.startupBehavior)
            put("requireConfirmations", _uiState.value.requireConfirmations)
            put("animationLevel", _uiState.value.animationLevel.name)
            put("compactMode", _uiState.value.isCompactMode)
            put("dynamicColors", _uiState.value.isDynamicColors)
            put("developerMode", _uiState.value.isDeveloperModeEnabled)
            put("performanceMode", _uiState.value.performanceMode)
            put("hardwareAcceleration", _uiState.value.hardwareAcceleration)
            put("customBackgroundUri", _uiState.value.customBackgroundUri ?: "")
            put("backgroundBlur", _uiState.value.backgroundBlur)
            put("backgroundOpacity", _uiState.value.backgroundOpacity)
            put("backgroundDarkOverlay", _uiState.value.backgroundDarkOverlay)
            put("backgroundScaleMode", _uiState.value.backgroundScaleMode)
        }
        root.put("settings", settingsObj)
        root.put("theme_id", themeManager.currentTheme.value.id)
        root.put("accent_override", themeManager.customAccentOverride.value ?: "")
        root.put("plugins", JSONObject(pluginManager.exportPluginConfigs()))

        AppLogger.i("Settings", "Exported full app configuration snapshot.")
        return root.toString(2)
    }

    fun importFullConfiguration(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("settings")) {
                val s = root.getJSONObject("settings")
                if (s.has("language")) setLanguage(s.getString("language"))
                if (s.has("defaultScreen")) setDefaultScreen(s.getString("defaultScreen"))
                if (s.has("startupBehavior")) setStartupBehavior(s.getString("startupBehavior"))
                if (s.has("requireConfirmations")) setConfirmations(s.getBoolean("requireConfirmations"))
                if (s.has("animationLevel")) {
                    try { setAnimationLevel(AnimationLevel.valueOf(s.getString("animationLevel"))) } catch (_: Exception) {}
                }
                if (s.has("compactMode")) setCompactMode(s.getBoolean("compactMode"))
                if (s.has("dynamicColors")) setDynamicColors(s.getBoolean("dynamicColors"))
                if (s.has("developerMode")) setDeveloperMode(s.getBoolean("developerMode"))
                if (s.has("performanceMode")) setPerformanceMode(s.getString("performanceMode"))
                if (s.has("hardwareAcceleration")) setHardwareAcceleration(s.getBoolean("hardwareAcceleration"))
                if (s.has("customBackgroundUri")) {
                    val uri = s.optString("customBackgroundUri", "")
                    setCustomBackground(
                        uri = if (uri.isNotBlank()) uri else null,
                        blur = s.optDouble("backgroundBlur", 0.0).toFloat(),
                        opacity = s.optDouble("backgroundOpacity", 0.85).toFloat(),
                        overlay = s.optDouble("backgroundDarkOverlay", 0.35).toFloat(),
                        scaleMode = s.optString("backgroundScaleMode", "Crop")
                    )
                }
            }

            if (root.has("theme_id")) {
                themeManager.applyTheme(root.getString("theme_id"))
            }
            if (root.has("accent_override")) {
                val acc = root.getString("accent_override")
                themeManager.setAccentOverride(if (acc.isNotBlank()) acc else null)
            }
            if (root.has("plugins")) {
                pluginManager.importPluginConfigs(root.getString("plugins"))
            }

            AppLogger.i("Settings", "Imported and applied complete configuration backup.")
            true
        } catch (e: Exception) {
            AppLogger.e("Settings", "Failed to import configuration: ${e.message}")
            false
        }
    }

    fun resetAllSettings() {
        viewModelScope.launch {
            repository.clearAll()
            themeManager.applyTheme("default_dark")
            themeManager.setAccentOverride(null)
            AppLogger.i("Settings", "All settings reset to defaults.")
            EventBus.emit("Settings", "SETTINGS_RESET", emptyMap())
        }
    }
}
