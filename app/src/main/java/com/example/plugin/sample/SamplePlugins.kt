package com.example.plugin.sample

import com.example.plugin.ModuxPlugin
import com.example.plugin.PluginContext
import com.example.plugin.PluginManifest
import com.example.plugin.PluginPermission
import com.example.plugin.PluginSettingDefinition

/**
 * 1. Quick Settings Plugin
 * Adds customizable tiles, rapid toggle switches, and quick actions to the hub.
 */
class QuickSettingsPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.quicksettings",
        name = "Quick Settings",
        version = "1.3.0",
        author = "ModuX Core Team",
        description = "Provides fast-access customization tiles, quick switcher toggles, and instant layout tweaks.",
        iconName = "tune",
        category = "Utility",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.UI, PluginPermission.STORAGE),
        changelog = listOf(
            "v1.3.0: Added tile layout density controls",
            "v1.2.0: Introduced haptic feedback setting",
            "v1.0.0: Initial release with 4 core quick tiles"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.Toggle(
                settingKey = "show_tiles_on_home",
                settingTitle = "Show Quick Action Tiles on Home",
                settingDescription = "Display rapid customization buttons right in the Home dashboard",
                defaultValue = true,
                settingCategory = "Display"
            ),
            PluginSettingDefinition.Toggle(
                settingKey = "enable_haptic",
                settingTitle = "Haptic Feedback on Toggle",
                settingDescription = "Provide subtle tactile feedback when switching quick options",
                defaultValue = true,
                settingCategory = "Feedback"
            ),
            PluginSettingDefinition.Dropdown(
                settingKey = "tile_columns",
                settingTitle = "Tile Layout Grid",
                settingDescription = "Choose how many quick action tiles appear per row",
                options = listOf("2 Columns (Comfortable)", "3 Columns (Dense)", "4 Columns (Ultra-Compact)"),
                defaultOptionIndex = 0,
                settingCategory = "Layout"
            ),
            PluginSettingDefinition.ActionButton(
                settingKey = "reset_tiles_action",
                settingTitle = "Reset Tiles Layout",
                settingDescription = "Restore standard order for all quick switcher tiles",
                buttonLabel = "Reset Tiles",
                actionId = "reset_tiles",
                settingCategory = "Actions"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Quick Settings loaded successfully")
    }

    override fun onEnable() {
        context?.logger?.i("Quick Settings enabled: Quick Tiles active")
        context?.emitEvent("QUICK_SETTINGS_ENABLED", mapOf("active" to "true"))
    }

    override fun onDisable() {
        context?.logger?.i("Quick Settings disabled")
        context?.emitEvent("QUICK_SETTINGS_DISABLED", mapOf("active" to "false"))
    }

    override fun onUnload() {
        context?.logger?.i("Quick Settings unloaded")
        context = null
    }

    override fun executeAction(actionId: String): String {
        return if (actionId == "reset_tiles") {
            context?.emitEvent("QUICK_SETTINGS_RESET", mapOf("status" to "ok"))
            "Tile layout successfully restored to defaults."
        } else {
            "Unknown action $actionId"
        }
    }
}

/**
 * 2. Better Notifications Plugin
 * Customizes notification alert banners, mute rules, and preview modes.
 */
class BetterNotificationsPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.notifications",
        name = "Better Notifications",
        version = "2.0.1",
        author = "Aura Studio",
        description = "Advanced control over customization alerts, banner display styles, and priority channel filters.",
        iconName = "notifications",
        category = "Notifications",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.NOTIFICATIONS, PluginPermission.UI),
        changelog = listOf(
            "v2.0.1: Fixed banner dismiss animation",
            "v2.0.0: Redesigned pill-style status toasts",
            "v1.1.0: Added quiet hours scheduling filter"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.Toggle(
                settingKey = "floating_toasts",
                settingTitle = "Pill-style Banner Toasts",
                settingDescription = "Display sleek floating pills instead of generic snackbars",
                defaultValue = true,
                settingCategory = "Visuals"
            ),
            PluginSettingDefinition.Dropdown(
                settingKey = "notification_preview_mode",
                settingTitle = "Preview Verbosity",
                settingDescription = "Choose how detailed plugin state change notifications should be",
                options = listOf("Minimal (Icon Only)", "Standard (Title & Subtitle)", "Verbose (Full Telemetry)"),
                defaultOptionIndex = 1,
                settingCategory = "Display"
            ),
            PluginSettingDefinition.Slider(
                settingKey = "toast_duration_seconds",
                settingTitle = "Banner Duration",
                settingDescription = "Seconds to show floating notifications before auto-dismiss",
                minValue = 1f,
                maxValue = 10f,
                step = 1f,
                defaultValue = 3f,
                unit = "s",
                settingCategory = "Timing"
            ),
            PluginSettingDefinition.ActionButton(
                settingKey = "test_notification_btn",
                settingTitle = "Test Banner Alert",
                settingDescription = "Trigger a preview notification to check your banner settings",
                buttonLabel = "Send Test Alert",
                actionId = "test_alert",
                settingCategory = "Actions"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Better Notifications plugin loaded")
    }

    override fun onEnable() {
        context?.logger?.i("Better Notifications active")
        context?.emitEvent("NOTIFICATION_SYSTEM_UPDATED", mapOf("mode" to "enhanced"))
    }

    override fun onDisable() {
        context?.logger?.i("Better Notifications disabled")
        context?.emitEvent("NOTIFICATION_SYSTEM_UPDATED", mapOf("mode" to "default"))
    }

    override fun onUnload() {
        context = null
    }

    override fun executeAction(actionId: String): String {
        return if (actionId == "test_alert") {
            context?.emitEvent("NOTIFICATION_TEST_TRIGGERED", mapOf("message" to "ModuX banner alert test successful!"))
            "Sent test notification banner!"
        } else {
            "Action $actionId not found"
        }
    }
}

/**
 * 3. Custom Accent Plugin
 * Allows fine-grained color palette injection and dynamic accent tints.
 */
class CustomAccentPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.customaccent",
        name = "Custom Accent Styler",
        version = "1.4.2",
        author = "Chromatica",
        description = "Inject bespoke brand tint colors, vibrant glowing highlights, and gradient header accents.",
        iconName = "palette",
        category = "Appearance",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.UI, PluginPermission.LOCAL_DATA),
        changelog = listOf(
            "v1.4.2: Added 6 new neon color presets",
            "v1.3.0: Live palette preview canvas support"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.ColorPicker(
                settingKey = "primary_accent_color",
                settingTitle = "Primary Accent Tint",
                settingDescription = "Override the default primary accent used in buttons, switches, and badges",
                defaultColorHex = "#6366F1",
                palette = listOf("#6366F1", "#8B5CF6", "#EC4899", "#F43F5E", "#10B981", "#06B6D4", "#F59E0B", "#EAB308"),
                settingCategory = "Colors"
            ),
            PluginSettingDefinition.Toggle(
                settingKey = "enable_card_glow",
                settingTitle = "Subtle Accent Borders",
                settingDescription = "Tint active card borders with the selected accent color",
                defaultValue = true,
                settingCategory = "Effects"
            ),
            PluginSettingDefinition.Slider(
                settingKey = "accent_glow_opacity",
                settingTitle = "Glow Intensity",
                settingDescription = "Border opacity intensity for highlighted elements",
                minValue = 10f,
                maxValue = 100f,
                step = 5f,
                defaultValue = 40f,
                unit = "%",
                settingCategory = "Effects"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Custom Accent Styler loaded")
    }

    override fun onEnable() {
        val color = context?.getSettingString("primary_accent_color", "#6366F1") ?: "#6366F1"
        context?.logger?.i("Custom Accent enabled with tint: $color")
        context?.emitEvent("ACCENT_OVERRIDE_ENABLED", mapOf("color" to color))
    }

    override fun onDisable() {
        context?.logger?.i("Custom Accent disabled, reverted to theme base")
        context?.emitEvent("ACCENT_OVERRIDE_DISABLED", emptyMap())
    }

    override fun onUnload() {
        context = null
    }
}

/**
 * 4. Compact UI Plugin
 * Minimizes padding, shrinks list cards, and increases information density.
 */
class CompactUiPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.compactui",
        name = "Compact UI Density",
        version = "1.1.0",
        author = "ModuX Core Team",
        description = "Reduces vertical padding and card sizes across all screens for maximum information density.",
        iconName = "view_compact",
        category = "Appearance",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.UI),
        changelog = listOf(
            "v1.1.0: Added density slider (80% to 50%)",
            "v1.0.0: Initial release"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.Slider(
                settingKey = "density_scale",
                settingTitle = "Padding Density Scale",
                settingDescription = "Scale factor applied to spacing and card margins",
                minValue = 50f,
                maxValue = 90f,
                step = 5f,
                defaultValue = 75f,
                unit = "%",
                settingCategory = "Density"
            ),
            PluginSettingDefinition.Toggle(
                settingKey = "hide_card_subtitles",
                settingTitle = "Minimalist Card View",
                settingDescription = "Omit secondary description text on plugin cards for ultra-dense view",
                defaultValue = false,
                settingCategory = "Density"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Compact UI density plugin loaded")
    }

    override fun onEnable() {
        context?.logger?.i("Compact UI enabled")
        context?.emitEvent("COMPACT_MODE_CHANGED", mapOf("enabled" to "true"))
    }

    override fun onDisable() {
        context?.logger?.i("Compact UI disabled")
        context?.emitEvent("COMPACT_MODE_CHANGED", mapOf("enabled" to "false"))
    }

    override fun onUnload() {
        context = null
    }
}

/**
 * 5. Developer Tools Plugin
 * Injects telemetry monitors, runtime event counters, and diagnostics helpers.
 */
class DeveloperToolsPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.devtools",
        name = "Developer Diagnostics SDK",
        version = "2.1.0",
        author = "DevLab",
        description = "Provides live heap memory inspection, event stream telemetry, and performance profiling tools.",
        iconName = "bug_report",
        category = "Developer",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.SYSTEM_STATS, PluginPermission.LOCAL_DATA, PluginPermission.UI),
        changelog = listOf(
            "v2.1.0: Real-time event counter ticker",
            "v2.0.0: Memory gauge integration"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.Toggle(
                settingKey = "show_fps_meter",
                settingTitle = "Show Performance Ticker",
                settingDescription = "Display current frame latency and memory usage on the home dashboard",
                defaultValue = true,
                settingCategory = "Telemetry"
            ),
            PluginSettingDefinition.Toggle(
                settingKey = "verbose_event_logs",
                settingTitle = "Verbose Event Bus Logging",
                settingDescription = "Record detailed parameter maps for every broadcast event",
                defaultValue = true,
                settingCategory = "Logs"
            ),
            PluginSettingDefinition.ActionButton(
                settingKey = "gc_action_btn",
                settingTitle = "Run Garbage Collector",
                settingDescription = "Request JVM garbage collection and log free heap delta",
                buttonLabel = "Request GC",
                actionId = "request_gc",
                settingCategory = "Actions"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Developer Diagnostics loaded")
    }

    override fun onEnable() {
        context?.logger?.i("Developer Diagnostics active")
        context?.emitEvent("DEVTOOLS_STATE_CHANGED", mapOf("active" to "true"))
    }

    override fun onDisable() {
        context?.logger?.i("Developer Diagnostics disabled")
        context?.emitEvent("DEVTOOLS_STATE_CHANGED", mapOf("active" to "false"))
    }

    override fun onUnload() {
        context = null
    }

    override fun executeAction(actionId: String): String {
        return if (actionId == "request_gc") {
            val runtime = Runtime.getRuntime()
            val before = runtime.freeMemory() / (1024 * 1024)
            System.gc()
            val after = runtime.freeMemory() / (1024 * 1024)
            context?.logger?.i("Garbage collection executed. Free memory: ${before}MB -> ${after}MB")
            "GC executed. Free memory: ${after}MB (Δ ${after - before}MB)"
        } else {
            "Action $actionId not supported"
        }
    }
}

/**
 * 6. Welcome Hub Plugin
 * Injects welcome message card, daily customization tip, and spotlight shortcuts.
 */
class WelcomeHubPlugin : ModuxPlugin {
    private var context: PluginContext? = null

    override val manifest = PluginManifest(
        id = "modux.welcome",
        name = "Welcome Spotlight",
        version = "1.0.2",
        author = "ModuX Core Team",
        description = "Displays daily customization recommendations, onboarding tips, and quick start guides.",
        iconName = "auto_awesome",
        category = "Utility",
        minAppVersion = "1.0.0",
        permissions = listOf(PluginPermission.UI),
        changelog = listOf(
            "v1.0.2: Added rotating daily tip carousel",
            "v1.0.0: Initial release"
        ),
        settingsSchema = listOf(
            PluginSettingDefinition.TextField(
                settingKey = "custom_greeting_name",
                settingTitle = "Personalized Greeting Name",
                settingDescription = "Customize the greeting display on the Home dashboard",
                defaultValue = "Customizer",
                placeholder = "Enter your nickname",
                settingCategory = "General"
            ),
            PluginSettingDefinition.Toggle(
                settingKey = "show_daily_tips",
                settingTitle = "Show Daily Customization Tips",
                settingDescription = "Provide helpful suggestions for themes and workflow setups",
                defaultValue = true,
                settingCategory = "General"
            )
        )
    )

    override fun onLoad(context: PluginContext) {
        this.context = context
        context.logger.i("Welcome Spotlight loaded")
    }

    override fun onEnable() {
        context?.logger?.i("Welcome Spotlight enabled")
        context?.emitEvent("WELCOME_SPOTLIGHT_ENABLED", emptyMap())
    }

    override fun onDisable() {
        context?.logger?.i("Welcome Spotlight disabled")
        context?.emitEvent("WELCOME_SPOTLIGHT_DISABLED", emptyMap())
    }

    override fun onUnload() {
        context = null
    }
}
