package com.example.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.core.AppLogger
import com.example.core.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data definition for a ModuX Theme.
 */
data class ThemeDefinition(
    val id: String,
    val name: String,
    val author: String,
    val version: String = "1.0.0",
    val description: String,
    val isDark: Boolean = true,
    val isCustom: Boolean = false,
    val backgroundHex: String,
    val surfaceHex: String,
    val cardHex: String,
    val textPrimaryHex: String,
    val textSecondaryHex: String,
    val accentHex: String,
    val borderHex: String
) {
    fun toBackgroundColor(): Color = parseColor(backgroundHex, Color(0xFF0F172A))
    fun toSurfaceColor(): Color = parseColor(surfaceHex, Color(0xFF1E293B))
    fun toCardColor(): Color = parseColor(cardHex, Color(0xFF243247))
    fun toTextPrimaryColor(): Color = parseColor(textPrimaryHex, Color(0xFFF8FAFC))
    fun toTextSecondaryColor(): Color = parseColor(textSecondaryHex, Color(0xFF94A3B8))
    fun toAccentColor(): Color = parseColor(accentHex, Color(0xFF6366F1))
    fun toBorderColor(): Color = parseColor(borderHex, Color(0x33FFFFFF))

    private fun parseColor(hex: String, fallback: Color): Color {
        return try {
            val clean = hex.removePrefix("#")
            val colorInt = when (clean.length) {
                6 -> (0xFF000000 or clean.toLong(16)).toInt()
                8 -> clean.toLong(16).toInt()
                else -> return fallback
            }
            Color(colorInt)
        } catch (_: Exception) {
            fallback
        }
    }
}

/**
 * Repository containing built-in and user-created custom themes.
 * Core built-in themes: Dark, AMOLED, Midnight, Light, Ocean.
 */
class ThemeRepository(private val context: Context) {
    private val sharedPrefs = context.getSharedPreferences("modux_theme_prefs", Context.MODE_PRIVATE)

    val builtInThemes = listOf(
        ThemeDefinition(
            id = "dark",
            name = "Dark",
            author = "ModuX Core",
            description = "Standard balanced Discord dark palette with vibrant indigo accents.",
            isDark = true,
            backgroundHex = "#313338",
            surfaceHex = "#2B2D31",
            cardHex = "#1E1F22",
            textPrimaryHex = "#F2F3F5",
            textSecondaryHex = "#949BA4",
            accentHex = "#5865F2",
            borderHex = "#3F4147"
        ),
        ThemeDefinition(
            id = "amoled",
            name = "AMOLED",
            author = "ModuX Core",
            description = "Pure pitch black background optimized for maximum battery savings on OLED displays.",
            isDark = true,
            backgroundHex = "#000000",
            surfaceHex = "#080808",
            cardHex = "#111111",
            textPrimaryHex = "#FFFFFF",
            textSecondaryHex = "#8E9297",
            accentHex = "#5865F2",
            borderHex = "#202225"
        ),
        ThemeDefinition(
            id = "midnight",
            name = "Midnight",
            author = "ModuX Core",
            description = "Deep twilight midnight sapphire tones with crisp radiant cyan highlights.",
            isDark = true,
            backgroundHex = "#0B0E14",
            surfaceHex = "#111827",
            cardHex = "#1F2937",
            textPrimaryHex = "#F9FAFB",
            textSecondaryHex = "#9CA3AF",
            accentHex = "#38BDF8",
            borderHex = "#374151"
        ),
        ThemeDefinition(
            id = "light",
            name = "Light",
            author = "ModuX Core",
            description = "High-contrast airy light theme with clean typography and slate borders.",
            isDark = false,
            backgroundHex = "#FFFFFF",
            surfaceHex = "#F2F3F5",
            cardHex = "#E3E5E8",
            textPrimaryHex = "#060607",
            textSecondaryHex = "#4E5058",
            accentHex = "#5865F2",
            borderHex = "#D1D5DB"
        ),
        ThemeDefinition(
            id = "ocean",
            name = "Ocean",
            author = "ModuX Core",
            description = "Deep oceanic abyssal teal palette with vibrant aquatic highlights.",
            isDark = true,
            backgroundHex = "#0A192F",
            surfaceHex = "#112240",
            cardHex = "#233554",
            textPrimaryHex = "#E6F1FF",
            textSecondaryHex = "#8892B0",
            accentHex = "#64FFDA",
            borderHex = "#233554"
        )
    )

    fun getCustomThemes(): List<ThemeDefinition> {
        val jsonStr = sharedPrefs.getString("custom_themes", null) ?: return emptyList()
        val list = mutableListOf<ThemeDefinition>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ThemeDefinition(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        author = obj.optString("author", "User"),
                        version = obj.optString("version", "1.0.0"),
                        description = obj.optString("description", "Custom created theme"),
                        isDark = obj.optBoolean("isDark", true),
                        isCustom = true,
                        backgroundHex = obj.getString("backgroundHex"),
                        surfaceHex = obj.getString("surfaceHex"),
                        cardHex = obj.getString("cardHex"),
                        textPrimaryHex = obj.getString("textPrimaryHex"),
                        textSecondaryHex = obj.getString("textSecondaryHex"),
                        accentHex = obj.getString("accentHex"),
                        borderHex = obj.getString("borderHex")
                    )
                )
            }
        } catch (e: Exception) {
            AppLogger.e("ThemeRepository", "Failed to parse custom themes: ${e.message}")
        }
        return list
    }

    fun saveCustomTheme(theme: ThemeDefinition) {
        val current = getCustomThemes().filter { it.id != theme.id }.toMutableList()
        current.add(theme.copy(isCustom = true))

        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("author", t.author)
                put("version", t.version)
                put("description", t.description)
                put("isDark", t.isDark)
                put("backgroundHex", t.backgroundHex)
                put("surfaceHex", t.surfaceHex)
                put("cardHex", t.cardHex)
                put("textPrimaryHex", t.textPrimaryHex)
                put("textSecondaryHex", t.textSecondaryHex)
                put("accentHex", t.accentHex)
                put("borderHex", t.borderHex)
            }
            array.put(obj)
        }
        sharedPrefs.edit().putString("custom_themes", array.toString()).apply()
        AppLogger.i("ThemeRepository", "Custom theme '${theme.name}' saved.")
    }

    fun deleteCustomTheme(id: String) {
        val current = getCustomThemes().filter { it.id != id }
        val array = JSONArray()
        for (t in current) {
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("author", t.author)
                put("version", t.version)
                put("description", t.description)
                put("isDark", t.isDark)
                put("backgroundHex", t.backgroundHex)
                put("surfaceHex", t.surfaceHex)
                put("cardHex", t.cardHex)
                put("textPrimaryHex", t.textPrimaryHex)
                put("textSecondaryHex", t.textSecondaryHex)
                put("accentHex", t.accentHex)
                put("borderHex", t.borderHex)
            }
            array.put(obj)
        }
        sharedPrefs.edit().putString("custom_themes", array.toString()).apply()
        AppLogger.i("ThemeRepository", "Custom theme $id deleted.")
    }
}

/**
 * Manager responsible for active theme selection, dynamic color override, and accent customization.
 */
class ThemeManager(
    private val context: Context,
    private val repository: ThemeRepository = ThemeRepository(context)
) {
    private val sharedPrefs = context.getSharedPreferences("modux_theme_prefs", Context.MODE_PRIVATE)

    private val _allThemes = MutableStateFlow<List<ThemeDefinition>>(emptyList())
    val allThemes: StateFlow<List<ThemeDefinition>> = _allThemes.asStateFlow()

    private val _currentTheme = MutableStateFlow<ThemeDefinition>(repository.builtInThemes.first())
    val currentTheme: StateFlow<ThemeDefinition> = _currentTheme.asStateFlow()

    private val _customAccentOverride = MutableStateFlow<String?>(null)
    val customAccentOverride: StateFlow<String?> = _customAccentOverride.asStateFlow()

    init {
        refreshThemes()
        loadSavedTheme()
    }

    fun refreshThemes() {
        val builtIn = repository.builtInThemes
        val custom = repository.getCustomThemes()
        _allThemes.value = builtIn + custom
    }

    private fun loadSavedTheme() {
        val savedId = sharedPrefs.getString("selected_theme_id", "dark") ?: "dark"
        val savedAccent = sharedPrefs.getString("accent_override", null)
        _customAccentOverride.value = savedAccent

        val theme = _allThemes.value.find { it.id == savedId } ?: repository.builtInThemes.first()
        _currentTheme.value = theme
    }

    fun applyTheme(themeId: String) {
        val target = _allThemes.value.find { it.id == themeId }
        if (target != null) {
            _currentTheme.value = target
            sharedPrefs.edit().putString("selected_theme_id", themeId).apply()
            AppLogger.i("ThemeManager", "Applied theme '${target.name}'")
            EventBus.emit("ThemeManager", "THEME_APPLIED", mapOf("themeId" to themeId, "themeName" to target.name))
        }
    }

    fun setAccentOverride(hex: String?) {
        _customAccentOverride.value = hex
        sharedPrefs.edit().putString("accent_override", hex).apply()
        AppLogger.i("ThemeManager", "Accent override updated: $hex")
        EventBus.emit("ThemeManager", "ACCENT_OVERRIDE_CHANGED", mapOf("accent" to (hex ?: "none")))
    }

    fun createAndApplyCustomTheme(theme: ThemeDefinition) {
        repository.saveCustomTheme(theme)
        refreshThemes()
        applyTheme(theme.id)
    }

    fun deleteCustomTheme(id: String) {
        if (_currentTheme.value.id == id) {
            applyTheme("dark")
        }
        repository.deleteCustomTheme(id)
        refreshThemes()
    }

    fun reloadThemes() {
        refreshThemes()
        loadSavedTheme()
        EventBus.emit("ThemeManager", "THEMES_RELOADED", emptyMap())
    }
}
