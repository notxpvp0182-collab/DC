package com.example.plugin

import android.content.Context
import com.example.core.AppConfig
import com.example.core.AppLogger
import com.example.core.EventBus
import com.example.plugin.sample.BetterNotificationsPlugin
import com.example.plugin.sample.CompactUiPlugin
import com.example.plugin.sample.CustomAccentPlugin
import com.example.plugin.sample.DeveloperToolsPlugin
import com.example.plugin.sample.QuickSettingsPlugin
import com.example.plugin.sample.WelcomeHubPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Validation engine for verifying plugin safety, compatibility, and dependencies.
 */
object PluginValidator {
    sealed class ValidationResult {
        data object Valid : ValidationResult()
        data class Invalid(val reason: String) : ValidationResult()
    }

    fun validate(manifest: PluginManifest, installedPluginIds: Set<String>): ValidationResult {
        if (manifest.id.isBlank()) return ValidationResult.Invalid("Plugin ID cannot be empty.")
        if (manifest.name.isBlank()) return ValidationResult.Invalid("Plugin Name cannot be empty.")
        if (manifest.version.isBlank()) return ValidationResult.Invalid("Plugin Version cannot be empty.")

        // App version check
        if (isVersionOlder(AppConfig.APP_VERSION, manifest.minAppVersion)) {
            return ValidationResult.Invalid(
                "App version ${AppConfig.APP_VERSION} is older than required min version ${manifest.minAppVersion}."
            )
        }

        // Dependency check
        for (dep in manifest.dependencies) {
            if (!installedPluginIds.contains(dep)) {
                return ValidationResult.Invalid("Missing required dependency plugin: '$dep'.")
            }
        }

        return ValidationResult.Valid
    }

    private fun isVersionOlder(current: String, required: String): Boolean {
        return try {
            val currentClean = current.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
            val requiredClean = required.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
            for (i in 0 until maxOf(currentClean.size, requiredClean.size)) {
                val c = currentClean.getOrElse(i) { 0 }
                val r = requiredClean.getOrElse(i) { 0 }
                if (c < r) return true
                if (c > r) return false
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Repository providing built-in plugins, available catalog plugins, and update definitions.
 */
class PluginRepository {
    private val availableCatalog = listOf(
        PluginManifest(
            id = "modux.soundpack",
            name = "Acoustic Theme Pack",
            version = "1.0.0",
            author = "EchoAudio",
            description = "Subtle auditory cues, click feedback, and soft soundscapes for UI interactions.",
            iconName = "volume_up",
            category = "Audio",
            minAppVersion = "1.0.0",
            permissions = listOf(PluginPermission.LOCAL_DATA, PluginPermission.UI),
            changelog = listOf("v1.0.0: Initial sound pack with 5 subtle clicks")
        ),
        PluginManifest(
            id = "modux.fontstyler",
            name = "Typography Master",
            version = "1.2.0",
            author = "TypeCraft",
            description = "Adjust text line spacing, custom letter tracking, and font weight hierarchies.",
            iconName = "font_download",
            category = "Appearance",
            minAppVersion = "1.0.0",
            permissions = listOf(PluginPermission.UI),
            changelog = listOf("v1.2.0: Dynamic font scale support", "v1.0.0: First release")
        ),
        PluginManifest(
            id = "modux.gesturenav",
            name = "Motion Gestures",
            version = "1.0.4",
            author = "Kinetic UI",
            description = "Quick swipe navigation between tabs, double-tap actions, and swipe to dismiss.",
            iconName = "touch_app",
            category = "Utility",
            minAppVersion = "1.0.0",
            permissions = listOf(PluginPermission.UI, PluginPermission.LOCAL_DATA),
            changelog = listOf("v1.0.4: Edge swipe sensitivity tuning")
        )
    )

    fun getCatalog(): List<PluginManifest> = availableCatalog
}

/**
 * Central manager orchestrating plugin life-cycles, persistence, sandboxing, and execution.
 */
class PluginManager(
    private val context: Context,
    private val repository: PluginRepository = PluginRepository()
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sharedPrefs = context.getSharedPreferences("modux_plugin_prefs", Context.MODE_PRIVATE)

    // Plugin registry: ID -> ModuxPlugin
    private val activePlugins = ConcurrentHashMap<String, ModuxPlugin>()
    // Plugin settings map: ID -> (Key -> Value)
    private val pluginSettingsCache = ConcurrentHashMap<String, ConcurrentHashMap<String, Any>>()

    private val _pluginStates = MutableStateFlow<Map<String, PluginRuntimeInfo>>(emptyMap())
    val pluginStates: StateFlow<Map<String, PluginRuntimeInfo>> = _pluginStates.asStateFlow()

    init {
        initializePlugins()
    }

    private fun initializePlugins() {
        // Built-in starter plugins
        val builtIn = listOf<ModuxPlugin>(
            QuickSettingsPlugin(),
            BetterNotificationsPlugin(),
            CustomAccentPlugin(),
            CompactUiPlugin(),
            DeveloperToolsPlugin(),
            WelcomeHubPlugin()
        )

        val states = mutableMapOf<String, PluginRuntimeInfo>()

        for (plugin in builtIn) {
            val id = plugin.manifest.id
            activePlugins[id] = plugin
            loadSettingsForPlugin(id, plugin.manifest.settingsSchema)

            // Check saved enable state (default QuickSettings, CustomAccent, WelcomeHub to true)
            val isDefaultOn = id in setOf("modux.quicksettings", "modux.customaccent", "modux.welcome")
            val isEnabled = sharedPrefs.getBoolean("enabled_$id", isDefaultOn)

            val runtimeInfo = PluginRuntimeInfo(
                manifest = plugin.manifest,
                state = if (isEnabled) PluginLifecycleState.ENABLED else PluginLifecycleState.DISABLED,
                isInstalled = true,
                isEnabled = isEnabled,
                hasUpdate = false,
                settingsValues = pluginSettingsCache[id]?.toMap() ?: emptyMap()
            )
            states[id] = runtimeInfo

            try {
                val ctx = createPluginContext(id)
                plugin.onLoad(ctx)
                if (isEnabled) {
                    plugin.onEnable()
                }
            } catch (e: Exception) {
                AppLogger.e("PluginManager", "Error initializing plugin $id: ${e.message}")
                states[id] = runtimeInfo.copy(
                    state = PluginLifecycleState.ERROR,
                    isEnabled = false,
                    lastErrorMessage = e.message ?: "Initialization failed"
                )
            }
        }

        // Add repository catalog plugins as uninstalled items
        for (catalogManifest in repository.getCatalog()) {
            if (!states.containsKey(catalogManifest.id)) {
                states[catalogManifest.id] = PluginRuntimeInfo(
                    manifest = catalogManifest,
                    state = PluginLifecycleState.UNINSTALLED,
                    isInstalled = false,
                    isEnabled = false
                )
            }
        }

        _pluginStates.value = states
        AppLogger.i("PluginManager", "Initialized ${activePlugins.size} installed plugins.")
    }

    fun registerPlugin(plugin: ModuxPlugin) {
        val id = plugin.manifest.id
        activePlugins[id] = plugin
        loadSettingsForPlugin(id, plugin.manifest.settingsSchema)

        val isDefaultOn = id in setOf("modux.quicksettings", "modux.customaccent", "modux.welcome")
        val isEnabled = sharedPrefs.getBoolean("enabled_$id", isDefaultOn)

        val runtimeInfo = PluginRuntimeInfo(
            manifest = plugin.manifest,
            state = if (isEnabled) PluginLifecycleState.ENABLED else PluginLifecycleState.DISABLED,
            isInstalled = true,
            isEnabled = isEnabled,
            hasUpdate = false,
            settingsValues = pluginSettingsCache[id]?.toMap() ?: emptyMap()
        )

        _pluginStates.update { it + (id to runtimeInfo) }

        try {
            val ctx = createPluginContext(id)
            plugin.onLoad(ctx)
            if (isEnabled) {
                plugin.onEnable()
            }
        } catch (e: Exception) {
            AppLogger.e("PluginManager", "Error registering plugin $id: ${e.message}")
            _pluginStates.update { current ->
                current + (id to runtimeInfo.copy(
                    state = PluginLifecycleState.ERROR,
                    isEnabled = false,
                    lastErrorMessage = e.message ?: "Registration failed"
                ))
            }
        }
    }

    fun unloadAll() {
        for ((id, plugin) in activePlugins) {
            try {
                if (_pluginStates.value[id]?.isEnabled == true) {
                    plugin.onDisable()
                }
                plugin.onUnload()
            } catch (e: Exception) {
                AppLogger.e("PluginManager", "Error unloading $id: ${e.message}")
            }
        }
        activePlugins.clear()
    }

    fun getPluginState(id: String): PluginRuntimeInfo? = _pluginStates.value[id]

    fun togglePlugin(id: String, enable: Boolean): Boolean {
        val plugin = activePlugins[id]
        val currentInfo = _pluginStates.value[id] ?: return false

        if (enable) {
            // Validate before enabling
            val installedIds = _pluginStates.value.filter { it.value.isInstalled }.keys
            val validation = PluginValidator.validate(currentInfo.manifest, installedIds)
            if (validation is PluginValidator.ValidationResult.Invalid) {
                AppLogger.w("PluginManager", "Cannot enable plugin $id: ${validation.reason}")
                _pluginStates.update { current ->
                    current + (id to currentInfo.copy(lastErrorMessage = validation.reason))
                }
                return false
            }

            return try {
                plugin?.onEnable()
                sharedPrefs.edit().putBoolean("enabled_$id", true).apply()
                _pluginStates.update { current ->
                    current + (id to currentInfo.copy(
                        state = PluginLifecycleState.ENABLED,
                        isEnabled = true,
                        lastErrorMessage = null
                    ))
                }
                AppLogger.i("PluginManager", "Plugin $id enabled.")
                EventBus.emit("PluginManager", "PLUGIN_ENABLED", mapOf("pluginId" to id))
                true
            } catch (e: Exception) {
                AppLogger.e("PluginManager", "Failed to enable plugin $id: ${e.message}")
                _pluginStates.update { current ->
                    current + (id to currentInfo.copy(
                        state = PluginLifecycleState.ERROR,
                        isEnabled = false,
                        lastErrorMessage = "Error while enabling: ${e.message}"
                    ))
                }
                false
            }
        } else {
            return try {
                plugin?.onDisable()
                sharedPrefs.edit().putBoolean("enabled_$id", false).apply()
                _pluginStates.update { current ->
                    current + (id to currentInfo.copy(
                        state = PluginLifecycleState.DISABLED,
                        isEnabled = false,
                        lastErrorMessage = null
                    ))
                }
                AppLogger.i("PluginManager", "Plugin $id disabled.")
                EventBus.emit("PluginManager", "PLUGIN_DISABLED", mapOf("pluginId" to id))
                true
            } catch (e: Exception) {
                AppLogger.e("PluginManager", "Error while disabling plugin $id: ${e.message}")
                false
            }
        }
    }

    fun installPlugin(manifest: PluginManifest): Boolean {
        val id = manifest.id
        val validation = PluginValidator.validate(manifest, _pluginStates.value.filter { it.value.isInstalled }.keys)
        if (validation is PluginValidator.ValidationResult.Invalid) {
            AppLogger.w("PluginManager", "Cannot install plugin $id: ${validation.reason}")
            return false
        }

        // Create a dynamic stub plugin instance for store/catalog items
        val dynamicPlugin = object : ModuxPlugin {
            private var ctx: PluginContext? = null
            override val manifest: PluginManifest = manifest

            override fun onLoad(context: PluginContext) {
                this.ctx = context
                context.logger.i("Dynamic plugin $id loaded")
            }

            override fun onEnable() {
                ctx?.logger?.i("Dynamic plugin $id enabled")
                ctx?.emitEvent("DYNAMIC_PLUGIN_ENABLED", mapOf("id" to id))
            }

            override fun onDisable() {
                ctx?.logger?.i("Dynamic plugin $id disabled")
                ctx?.emitEvent("DYNAMIC_PLUGIN_DISABLED", mapOf("id" to id))
            }

            override fun onUnload() {
                ctx = null
            }
        }

        activePlugins[id] = dynamicPlugin
        val ctx = createPluginContext(id)
        dynamicPlugin.onLoad(ctx)

        sharedPrefs.edit().putBoolean("installed_$id", true).apply()
        _pluginStates.update { current ->
            current + (id to PluginRuntimeInfo(
                manifest = manifest,
                state = PluginLifecycleState.DISABLED,
                isInstalled = true,
                isEnabled = false
            ))
        }

        AppLogger.i("PluginManager", "Plugin ${manifest.name} installed successfully.")
        EventBus.emit("PluginManager", "PLUGIN_INSTALLED", mapOf("pluginId" to id))
        return true
    }

    fun uninstallPlugin(id: String): Boolean {
        val plugin = activePlugins[id]
        if (plugin != null) {
            try {
                if (_pluginStates.value[id]?.isEnabled == true) {
                    plugin.onDisable()
                }
                plugin.onUnload()
            } catch (e: Exception) {
                AppLogger.e("PluginManager", "Error unloading plugin $id: ${e.message}")
            }
        }

        sharedPrefs.edit()
            .remove("enabled_$id")
            .remove("installed_$id")
            .remove("settings_$id")
            .apply()

        activePlugins.remove(id)
        pluginSettingsCache.remove(id)

        val catalogItem = repository.getCatalog().find { it.id == id }
        _pluginStates.update { current ->
            if (catalogItem != null) {
                current + (id to PluginRuntimeInfo(
                    manifest = catalogItem,
                    state = PluginLifecycleState.UNINSTALLED,
                    isInstalled = false,
                    isEnabled = false
                ))
            } else {
                current - id
            }
        }

        AppLogger.i("PluginManager", "Plugin $id uninstalled.")
        EventBus.emit("PluginManager", "PLUGIN_UNINSTALLED", mapOf("pluginId" to id))
        return true
    }

    fun updatePluginSetting(pluginId: String, key: String, value: Any) {
        val map = pluginSettingsCache.getOrPut(pluginId) { ConcurrentHashMap() }
        map[key] = value

        saveSettingsForPlugin(pluginId)

        val currentInfo = _pluginStates.value[pluginId]
        if (currentInfo != null) {
            _pluginStates.update { current ->
                current + (pluginId to currentInfo.copy(settingsValues = map.toMap()))
            }
        }

        EventBus.emit("PluginManager", "PLUGIN_SETTING_CHANGED", mapOf("pluginId" to pluginId, "key" to key))
    }

    fun executePluginAction(pluginId: String, actionId: String): String {
        val plugin = activePlugins[pluginId] ?: return "Plugin not active"
        return try {
            val result = plugin.executeAction(actionId)
            AppLogger.i("PluginManager", "Executed action '$actionId' for plugin $pluginId: $result")
            result
        } catch (e: Exception) {
            AppLogger.e("PluginManager", "Failed action '$actionId' on $pluginId: ${e.message}")
            "Error: ${e.message}"
        }
    }

    fun reloadAllPlugins() {
        AppLogger.i("PluginManager", "Reloading all plugins...")
        val currentStates = _pluginStates.value
        for ((id, info) in currentStates) {
            if (info.isInstalled && info.isEnabled) {
                val p = activePlugins[id]
                try {
                    p?.onDisable()
                    p?.onLoad(createPluginContext(id))
                    p?.onEnable()
                } catch (e: Exception) {
                    AppLogger.e("PluginManager", "Failed to reload $id: ${e.message}")
                }
            }
        }
        EventBus.emit("PluginManager", "PLUGINS_RELOADED", emptyMap())
    }

    fun getActivePluginCount(): Int = _pluginStates.value.values.count { it.isEnabled }

    fun exportPluginConfigs(): String {
        val json = JSONObject()
        for ((id, settings) in pluginSettingsCache) {
            val pluginJson = JSONObject()
            for ((k, v) in settings) {
                pluginJson.put(k, v)
            }
            val isEnabled = _pluginStates.value[id]?.isEnabled ?: false
            pluginJson.put("__enabled", isEnabled)
            json.put(id, pluginJson)
        }
        return json.toString(2)
    }

    fun importPluginConfigs(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val keys = root.keys()
            while (keys.hasNext()) {
                val pluginId = keys.next()
                val obj = root.getJSONObject(pluginId)
                val isEnabled = obj.optBoolean("__enabled", false)

                val settings = pluginSettingsCache.getOrPut(pluginId) { ConcurrentHashMap() }
                val objKeys = obj.keys()
                while (objKeys.hasNext()) {
                    val k = objKeys.next()
                    if (k != "__enabled") {
                        settings[k] = obj.get(k)
                    }
                }
                saveSettingsForPlugin(pluginId)
                if (activePlugins.containsKey(pluginId)) {
                    togglePlugin(pluginId, isEnabled)
                }
            }
            AppLogger.i("PluginManager", "Imported plugin configurations successfully.")
            true
        } catch (e: Exception) {
            AppLogger.e("PluginManager", "Failed to import plugin configs: ${e.message}")
            false
        }
    }

    private fun createPluginContext(id: String): PluginContext {
        return object : PluginContext {
            override val pluginId: String = id
            override val logger: PluginLogger = PluginLogger(id)

            override fun getSettingBoolean(key: String, default: Boolean): Boolean {
                val v = pluginSettingsCache[id]?.get(key)
                return when (v) {
                    is Boolean -> v
                    is String -> v.toBooleanStrictOrNull() ?: default
                    else -> default
                }
            }

            override fun getSettingFloat(key: String, default: Float): Float {
                val v = pluginSettingsCache[id]?.get(key)
                return when (v) {
                    is Number -> v.toFloat()
                    is String -> v.toFloatOrNull() ?: default
                    else -> default
                }
            }

            override fun getSettingString(key: String, default: String): String {
                return pluginSettingsCache[id]?.get(key)?.toString() ?: default
            }

            override fun getSettingInt(key: String, default: Int): Int {
                val v = pluginSettingsCache[id]?.get(key)
                return when (v) {
                    is Number -> v.toInt()
                    is String -> v.toIntOrNull() ?: default
                    else -> default
                }
            }

            override fun setSetting(key: String, value: Any) {
                updatePluginSetting(id, key, value)
            }

            override fun emitEvent(eventType: String, payload: Map<String, String>) {
                EventBus.emit("Plugin:$id", eventType, payload)
            }

            override fun isPermissionGranted(permission: PluginPermission): Boolean {
                // In ModuX, permissions declared in manifest are granted upon install/enable
                val manifest = _pluginStates.value[id]?.manifest ?: return false
                return manifest.permissions.contains(permission)
            }
        }
    }

    private fun loadSettingsForPlugin(id: String, schema: List<PluginSettingDefinition>) {
        val map = ConcurrentHashMap<String, Any>()
        val savedJsonStr = sharedPrefs.getString("settings_$id", null)

        // Seed defaults first
        for (def in schema) {
            when (def) {
                is PluginSettingDefinition.Toggle -> map[def.settingKey] = def.defaultValue
                is PluginSettingDefinition.Slider -> map[def.settingKey] = def.defaultValue
                is PluginSettingDefinition.TextField -> map[def.settingKey] = def.defaultValue
                is PluginSettingDefinition.NumberField -> map[def.settingKey] = def.defaultValue
                is PluginSettingDefinition.Dropdown -> map[def.settingKey] = def.options.getOrElse(def.defaultOptionIndex) { "" }
                is PluginSettingDefinition.ColorPicker -> map[def.settingKey] = def.defaultColorHex
                is PluginSettingDefinition.ActionButton -> Unit
            }
        }

        // Overlay saved JSON if present
        if (savedJsonStr != null) {
            try {
                val json = JSONObject(savedJsonStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = json.get(k)
                }
            } catch (e: Exception) {
                AppLogger.w("PluginManager", "Failed to parse saved settings for $id")
            }
        }

        pluginSettingsCache[id] = map
    }

    private fun saveSettingsForPlugin(id: String) {
        val map = pluginSettingsCache[id] ?: return
        try {
            val json = JSONObject()
            for ((k, v) in map) {
                json.put(k, v)
            }
            sharedPrefs.edit().putString("settings_$id", json.toString()).apply()
        } catch (e: Exception) {
            AppLogger.e("PluginManager", "Failed to save settings for $id: ${e.message}")
        }
    }
}
