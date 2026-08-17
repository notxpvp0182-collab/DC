package com.example.plugin

import com.example.core.AppLogger
import com.example.core.EventBus

/**
 * Declared permissions that plugins may request.
 * Users can inspect permissions before enabling/installing plugins.
 */
enum class PluginPermission(val title: String, val description: String, val level: SafetyLevel) {
    STORAGE("Local Storage", "Store custom plugin preferences and cached assets locally", SafetyLevel.SAFE),
    NOTIFICATIONS("Notifications", "Display banner alerts and system custom toasts", SafetyLevel.MODERATE),
    NETWORK("Network (External)", "Fetch updates and public metadata from remote endpoints", SafetyLevel.MODERATE),
    UI("UI Customization", "Inject custom cards, buttons, badges, and layout overrides", SafetyLevel.SAFE),
    LOCAL_DATA("Local Data", "Access non-sensitive device stats and customization configs", SafetyLevel.SAFE),
    SYSTEM_STATS("System Diagnostics", "Inspect heap memory, active threads, and rendering latency", SafetyLevel.SAFE);

    enum class SafetyLevel {
        SAFE, MODERATE, ADVANCED
    }
}

/**
 * Supported types of interactive settings a plugin can declare.
 */
sealed class PluginSettingDefinition(
    val key: String,
    val title: String,
    val description: String = "",
    val category: String = "General"
) {
    data class Toggle(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val defaultValue: Boolean = false,
        val settingCategory: String = "General"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class Slider(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val minValue: Float = 0f,
        val maxValue: Float = 100f,
        val step: Float = 1f,
        val defaultValue: Float = 50f,
        val unit: String = "",
        val settingCategory: String = "General"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class TextField(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val defaultValue: String = "",
        val placeholder: String = "",
        val settingCategory: String = "General"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class NumberField(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val defaultValue: Int = 0,
        val minValue: Int = 0,
        val maxValue: Int = 1000,
        val settingCategory: String = "General"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class Dropdown(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val options: List<String>,
        val defaultOptionIndex: Int = 0,
        val settingCategory: String = "General"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class ColorPicker(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val defaultColorHex: String = "#6366F1",
        val palette: List<String> = listOf("#6366F1", "#EC4899", "#10B981", "#F59E0B", "#3B82F6", "#8B5CF6", "#14B8A6"),
        val settingCategory: String = "Appearance"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)

    data class ActionButton(
        val settingKey: String,
        val settingTitle: String,
        val settingDescription: String = "",
        val buttonLabel: String = "Execute",
        val actionId: String,
        val settingCategory: String = "Actions"
    ) : PluginSettingDefinition(settingKey, settingTitle, settingDescription, settingCategory)
}

/**
 * Manifest describing metadata, compatibility, permissions, and settings schema.
 */
data class PluginManifest(
    val id: String,
    val name: String,
    val version: String,
    val author: String,
    val description: String,
    val iconName: String = "extension",
    val category: String = "Utility",
    val minAppVersion: String = "1.0.0",
    val permissions: List<PluginPermission> = emptyList(),
    val dependencies: List<String> = emptyList(),
    val changelog: List<String> = emptyList(),
    val settingsSchema: List<PluginSettingDefinition> = emptyList()
)

/**
 * Sandboxed API context provided to a plugin at runtime.
 */
interface PluginContext {
    val pluginId: String
    val logger: PluginLogger
    fun getSettingBoolean(key: String, default: Boolean = false): Boolean
    fun getSettingFloat(key: String, default: Float = 0f): Float
    fun getSettingString(key: String, default: String = ""): String
    fun getSettingInt(key: String, default: Int = 0): Int
    fun setSetting(key: String, value: Any)
    fun emitEvent(eventType: String, payload: Map<String, String> = emptyMap())
    fun isPermissionGranted(permission: PluginPermission): Boolean
}

class PluginLogger(private val pluginId: String) {
    fun d(message: String) = AppLogger.d("Plugin:$pluginId", message)
    fun i(message: String) = AppLogger.i("Plugin:$pluginId", message)
    fun w(message: String) = AppLogger.w("Plugin:$pluginId", message)
    fun e(message: String) = AppLogger.e("Plugin:$pluginId", message)
}

/**
 * Core Plugin interface for ModuX.
 */
interface ModuxPlugin {
    val manifest: PluginManifest

    /** Called when the plugin is initially instantiated and registered */
    fun onLoad(context: PluginContext)

    /** Called when the user or system enables the plugin */
    fun onEnable()

    /** Called when the plugin is disabled. Must clean up any resources */
    fun onDisable()

    /** Called when the plugin is unloaded/uninstalled */
    fun onUnload()

    /** Called when an interactive action button in settings is triggered */
    fun executeAction(actionId: String): String = "Action executed"
}

enum class PluginLifecycleState {
    UNINSTALLED,
    INSTALLED,
    LOADED,
    ENABLED,
    DISABLED,
    ERROR
}

data class PluginRuntimeInfo(
    val manifest: PluginManifest,
    val state: PluginLifecycleState = PluginLifecycleState.DISABLED,
    val isInstalled: Boolean = true,
    val isEnabled: Boolean = false,
    val hasUpdate: Boolean = false,
    val availableUpdateVersion: String? = null,
    val lastErrorMessage: String? = null,
    val settingsValues: Map<String, Any> = emptyMap()
)
