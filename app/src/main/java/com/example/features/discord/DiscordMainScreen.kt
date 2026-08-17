package com.example.features.discord

import android.webkit.PermissionRequest
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.developer.DeveloperViewModel
import com.example.features.client.ClientCategory
import com.example.features.client.ClientHubScreen
import com.example.media.ScreenShareManager
import com.example.permissions.PermissionManager
import com.example.plugin.PluginManager
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeManager
import com.example.web.DiscordWebContainer

@Composable
fun DiscordMainScreen(
    pluginManager: PluginManager,
    themeManager: ThemeManager,
    settingsViewModel: SettingsViewModel,
    developerViewModel: DeveloperViewModel,
    permissionManager: PermissionManager,
    screenShareManager: ScreenShareManager,
    modifier: Modifier = Modifier
) {
    val currentTheme by themeManager.currentTheme.collectAsState()
    val customAccent by themeManager.customAccentOverride.collectAsState()
    val settingsState by settingsViewModel.uiState.collectAsState()

    var isClientHubOpen by remember { mutableStateOf(false) }
    var targetClientCategory by remember { mutableStateOf(ClientCategory.GENERAL) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current

    BackHandler(enabled = true) {
        if (isClientHubOpen) {
            isClientHubOpen = false
        } else if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(currentTheme.toBackgroundColor())
    ) {
        // 1. Custom Wallpaper Background Layer
        if (settingsState.customBackgroundUri != null) {
            AsyncImage(
                model = settingsState.customBackgroundUri,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(settingsState.backgroundBlur.dp),
                contentScale = ContentScale.Crop,
                alpha = settingsState.backgroundOpacity
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = settingsState.backgroundDarkOverlay))
            )
        }

        // 2. Primary Discord Web Container
        DiscordWebContainer(
            theme = currentTheme,
            accentOverride = customAccent,
            isHardwareAccelerationEnabled = settingsState.hardwareAcceleration,
            isCompactMode = settingsState.isCompactMode,
            onOpenClientCategory = { categoryId ->
                val category = mapCategoryKeyToEnum(categoryId)
                targetClientCategory = category
                isClientHubOpen = true
            },
            onWebViewCreated = { webView ->
                webViewRef = webView
            },
            onPermissionRequested = { request ->
                Toast.makeText(context, "Discord requested ${request.resources.joinToString()}", Toast.LENGTH_SHORT).show()
                permissionManager.refreshPermissions()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 3. Client Customization Overlay Panel (Triggered seamlessly from Discord Settings)
        AnimatedVisibility(
            visible = isClientHubOpen,
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            ClientHubScreen(
                pluginManager = pluginManager,
                themeManager = themeManager,
                settingsViewModel = settingsViewModel,
                developerViewModel = developerViewModel,
                permissionManager = permissionManager,
                screenShareManager = screenShareManager,
                initialCategory = targetClientCategory,
                onClose = { isClientHubOpen = false },
                onReloadDiscordWeb = {
                    webViewRef?.reload()
                    isClientHubOpen = false
                }
            )
        }
    }
}

private fun mapCategoryKeyToEnum(key: String): ClientCategory {
    return when (key.lowercase().trim()) {
        "general", "client" -> ClientCategory.GENERAL
        "plugins" -> ClientCategory.PLUGINS
        "themes" -> ClientCategory.THEMES
        "background" -> ClientCategory.BACKGROUND
        "appearance" -> ClientCategory.APPEARANCE
        "messages" -> ClientCategory.MESSAGES
        "notifications" -> ClientCategory.NOTIFICATIONS
        "voice" -> ClientCategory.VOICE
        "video" -> ClientCategory.VIDEO
        "screenshare", "screen_share" -> ClientCategory.SCREEN_SHARE
        "permissions" -> ClientCategory.PERMISSIONS
        "keybinds" -> ClientCategory.KEYBINDS
        "performance" -> ClientCategory.PERFORMANCE
        "developer" -> ClientCategory.DEVELOPER
        "backup", "backup_restore", "restore" -> ClientCategory.BACKUP_RESTORE
        "about" -> ClientCategory.ABOUT
        else -> ClientCategory.GENERAL
    }
}
