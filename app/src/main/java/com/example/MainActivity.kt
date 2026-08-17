package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.core.AppLogger
import com.example.developer.DeveloperViewModel
import com.example.features.discord.DiscordMainScreen
import com.example.media.ScreenShareManager
import com.example.permissions.PermissionManager
import com.example.plugin.PluginManager
import com.example.plugin.sample.BetterNotificationsPlugin
import com.example.plugin.sample.CompactUiPlugin
import com.example.plugin.sample.CustomAccentPlugin
import com.example.plugin.sample.DeveloperToolsPlugin
import com.example.plugin.sample.QuickSettingsPlugin
import com.example.plugin.sample.WelcomeHubPlugin
import com.example.settings.SettingsRepository
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeManager
import com.example.ui.theme.ModuxTheme

import java.io.File

class MainActivity : ComponentActivity() {

    private lateinit var pluginManager: PluginManager
    private lateinit var themeManager: ThemeManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var developerViewModel: DeveloperViewModel
    private lateinit var permissionManager: PermissionManager
    private lateinit var screenShareManager: ScreenShareManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure WebView cache directories exist and are writable
        try {
            val webViewCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            webViewCache.mkdirs()
            File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm").mkdirs()
        } catch (_: Exception) {
            // Ignore if directory creation handles automatically
        }

        AppLogger.i("MainActivity", "ModuX Discord Customization Client initializing...")

        // 1. Initialize Managers & Repositories
        pluginManager = PluginManager(applicationContext)
        themeManager = ThemeManager(applicationContext)
        settingsRepository = SettingsRepository(applicationContext)
        settingsViewModel = SettingsViewModel(settingsRepository, pluginManager, themeManager)
        developerViewModel = DeveloperViewModel(pluginManager, themeManager)
        permissionManager = PermissionManager(applicationContext)
        screenShareManager = ScreenShareManager(applicationContext)

        setContent {
            val currentTheme by themeManager.currentTheme.collectAsState()
            val customAccent by themeManager.customAccentOverride.collectAsState()
            val settingsState by settingsViewModel.uiState.collectAsState()

            ModuxTheme(
                themeDefinition = currentTheme,
                accentOverride = customAccent,
                dynamicColor = settingsState.isDynamicColors
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DiscordMainScreen(
                        pluginManager = pluginManager,
                        themeManager = themeManager,
                        settingsViewModel = settingsViewModel,
                        developerViewModel = developerViewModel,
                        permissionManager = permissionManager,
                        screenShareManager = screenShareManager
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissionManager.refreshPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        pluginManager.unloadAll()
        screenShareManager.stopScreenSharing()
    }
}
