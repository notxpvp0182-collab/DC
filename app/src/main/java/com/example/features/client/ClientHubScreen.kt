package com.example.features.client

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.core.AppConfig
import com.example.core.LogLevel
import com.example.developer.DeveloperViewModel
import com.example.features.plugins.PluginDetailDialog
import com.example.features.plugins.PluginSettingsDialog
import com.example.media.ScreenShareManager
import com.example.permissions.PermissionManager
import com.example.permissions.PermissionStatus
import com.example.plugin.PluginManager
import com.example.plugin.PluginRuntimeInfo
import com.example.settings.AnimationLevel
import com.example.settings.SettingsViewModel
import com.example.theme.ThemeDefinition
import com.example.theme.ThemeManager
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSearchBar
import com.example.ui.components.ModuxSectionHeader

enum class ClientCategory(val title: String, val icon: ImageVector) {
    GENERAL("General", Icons.Default.Tune),
    PLUGINS("Plugins", Icons.Default.Extension),
    THEMES("Themes", Icons.Default.Palette),
    BACKGROUND("Background", Icons.Default.Image),
    APPEARANCE("Appearance", Icons.Default.FormatPaint),
    MESSAGES("Message Tools", Icons.Default.TextFields),
    NOTIFICATIONS("Notifications", Icons.Default.Notifications),
    VOICE("Voice", Icons.Default.Mic),
    VIDEO("Video", Icons.Default.Videocam),
    SCREEN_SHARE("Screen Share", Icons.Default.ScreenShare),
    PERMISSIONS("Permissions", Icons.Default.Security),
    KEYBINDS("Keybinds", Icons.Default.Keyboard),
    PERFORMANCE("Performance", Icons.Default.Speed),
    DEVELOPER("Developer", Icons.Default.BugReport),
    BACKUP_RESTORE("Backup & Restore", Icons.Default.CloudUpload),
    ABOUT("About", Icons.Default.Info)
}

@Composable
fun ClientHubScreen(
    pluginManager: PluginManager,
    themeManager: ThemeManager,
    settingsViewModel: SettingsViewModel,
    developerViewModel: DeveloperViewModel,
    permissionManager: PermissionManager,
    screenShareManager: ScreenShareManager,
    onClose: () -> Unit,
    initialCategory: ClientCategory = ClientCategory.GENERAL,
    onReloadDiscordWeb: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Discord"
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Client Settings",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                ModuxBadge(
                                    text = "Active",
                                    containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                    contentColor = Color(0xFF10B981)
                                )
                            }
                            Text(
                                text = "ModuX Customization Engine",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }

            // Category Horizontal Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedCategory.ordinal,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) }
            ) {
                ClientCategory.entries.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(cat.title, fontSize = 13.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }

            // Category Body
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedCategory) {
                    ClientCategory.GENERAL -> ClientGeneralSection(settingsViewModel, onReloadDiscordWeb)
                    ClientCategory.PLUGINS -> ClientPluginsSection(pluginManager)
                    ClientCategory.THEMES -> ClientThemesSection(themeManager)
                    ClientCategory.BACKGROUND -> ClientBackgroundSection(settingsViewModel)
                    ClientCategory.APPEARANCE -> ClientAppearanceSection(themeManager, settingsViewModel)
                    ClientCategory.MESSAGES -> ClientMessagesSection(settingsViewModel)
                    ClientCategory.NOTIFICATIONS -> ClientNotificationsSection(settingsViewModel, permissionManager)
                    ClientCategory.VOICE -> ClientVoiceSection(permissionManager)
                    ClientCategory.VIDEO -> ClientVideoSection(permissionManager, settingsViewModel)
                    ClientCategory.SCREEN_SHARE -> ClientScreenShareSection(screenShareManager)
                    ClientCategory.PERMISSIONS -> ClientPermissionsSection(permissionManager)
                    ClientCategory.KEYBINDS -> ClientKeybindsSection(settingsViewModel)
                    ClientCategory.PERFORMANCE -> ClientPerformanceSection(settingsViewModel, onReloadDiscordWeb)
                    ClientCategory.DEVELOPER -> ClientDeveloperSection(developerViewModel)
                    ClientCategory.BACKUP_RESTORE -> ClientBackupRestoreSection(settingsViewModel, themeManager, pluginManager, onReloadDiscordWeb)
                    ClientCategory.ABOUT -> ClientAboutSection(settingsViewModel)
                }
            }
        }
    }
}

// ----------------------------------------------------
// 1. CLIENT GENERAL
// ----------------------------------------------------
@Composable
fun ClientGeneralSection(
    settingsViewModel: SettingsViewModel,
    onReloadDiscordWeb: () -> Unit
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Client Status", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("ModuX customization layer active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Startup Experience", fontSize = 14.sp)
                        Text(state.startupBehavior, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "Core Customizations")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Compact Mode", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Reduces padding and channel spacing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.isCompactMode,
                            onCheckedChange = { settingsViewModel.setCompactMode(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Require Confirmations", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Show prompt before destructive resets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.requireConfirmations,
                            onCheckedChange = { settingsViewModel.setConfirmations(it) }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Animation Level", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Control client motion smoothness", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        FilterChip(
                            selected = true,
                            onClick = {
                                val next = when (state.animationLevel) {
                                    AnimationLevel.FULL -> AnimationLevel.REDUCED
                                    AnimationLevel.REDUCED -> AnimationLevel.OFF
                                    AnimationLevel.OFF -> AnimationLevel.FULL
                                }
                                settingsViewModel.setAnimationLevel(next)
                            },
                            label = { Text(state.animationLevel.name) }
                        )
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "Quick Client Actions")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onReloadDiscordWeb,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reload Web")
                }
                OutlinedButton(
                    onClick = {
                        val json = settingsViewModel.exportFullConfiguration()
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ModuX Config", json))
                        Toast.makeText(context, "Exported config copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Config")
                }
            }
        }
    }
}

// ----------------------------------------------------
// 2. APPEARANCE
// ----------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientAppearanceSection(
    themeManager: ThemeManager,
    settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.uiState.collectAsState()
    val customAccent by themeManager.customAccentOverride.collectAsState()

    val accentPresets = listOf(
        "#6366F1" to "Indigo",
        "#38BDF8" to "Cyan",
        "#10B981" to "Emerald",
        "#F43F5E" to "Rose",
        "#8B5CF6" to "Purple",
        "#F59E0B" to "Amber"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Accent Color Override")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select a global accent color to highlight buttons, badges, and sliders across Discord and ModuX:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accentPresets.forEach { (hex, name) ->
                            val isSelected = customAccent == hex
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable {
                                        themeManager.setAccentOverride(if (isSelected) null else hex)
                                    },
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.5.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(
                                                try {
                                                    Color(android.graphics.Color.parseColor(hex))
                                                } catch (_: Exception) {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(name, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    if (customAccent != null) {
                        TextButton(
                            onClick = { themeManager.setAccentOverride(null) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Reset Accent Override")
                        }
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "UI Densities & Theming")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dynamic Colors (Android 12+)", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Sync palette with your wallpaper system colors", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.isDynamicColors,
                            onCheckedChange = { settingsViewModel.setDynamicColors(it) }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 3. THEMES
// ----------------------------------------------------
@Composable
fun ClientThemesSection(
    themeManager: ThemeManager
) {
    val allThemes by themeManager.allThemes.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModuxSectionHeader(title = "Built-in & Custom Themes")
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Theme")
                }
            }
        }

        items(allThemes) { theme ->
            val isSelected = currentTheme.id == theme.id
            ModuxCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { themeManager.applyTheme(theme.id) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(theme.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                if (theme.isCustom) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ModuxBadge(
                                        text = "Custom",
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                            Text(theme.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (isSelected) {
                            ModuxBadge(
                                text = "Active",
                                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                contentColor = Color(0xFF10B981)
                            )
                        } else {
                            Button(
                                onClick = { themeManager.applyTheme(theme.id) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Color Preview Palette Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                    ) {
                        Box(modifier = Modifier.weight(2f).fillMaxHeight().background(theme.toBackgroundColor()))
                        Box(modifier = Modifier.weight(2f).fillMaxHeight().background(theme.toSurfaceColor()))
                        Box(modifier = Modifier.weight(2f).fillMaxHeight().background(theme.toCardColor()))
                        Box(modifier = Modifier.weight(1.5f).fillMaxHeight().background(theme.toAccentColor()))
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateThemeDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { newTheme ->
                themeManager.createAndApplyCustomTheme(newTheme)
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CreateThemeDialog(
    onDismiss: () -> Unit,
    onSave: (ThemeDefinition) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var bgHex by remember { mutableStateOf("#0B0E14") }
    var surfaceHex by remember { mutableStateOf("#151B26") }
    var cardHex by remember { mutableStateOf("#1F2937") }
    var accentHex by remember { mutableStateOf("#6366F1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Theme") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Theme Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = bgHex,
                    onValueChange = { bgHex = it },
                    label = { Text("Background Hex") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = accentHex,
                    onValueChange = { accentHex = it },
                    label = { Text("Accent Hex") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val t = ThemeDefinition(
                            id = "custom_${System.currentTimeMillis()}",
                            name = name,
                            author = "User",
                            description = desc.ifBlank { "Custom ModuX theme" },
                            backgroundHex = bgHex,
                            surfaceHex = surfaceHex,
                            cardHex = cardHex,
                            textPrimaryHex = "#F9FAFB",
                            textSecondaryHex = "#9CA3AF",
                            accentHex = accentHex,
                            borderHex = "#374151",
                            isCustom = true
                        )
                        onSave(t)
                    }
                }
            ) {
                Text("Save & Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ----------------------------------------------------
// 4. BACKGROUND
// ----------------------------------------------------
@Composable
fun ClientBackgroundSection(
    settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            settingsViewModel.setCustomBackground(
                uri = uri.toString(),
                blur = state.backgroundBlur,
                opacity = state.backgroundOpacity,
                overlay = state.backgroundDarkOverlay,
                scaleMode = state.backgroundScaleMode
            )
            Toast.makeText(context, "Custom wallpaper selected!", Toast.LENGTH_SHORT).show()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Custom Wallpaper")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Layer a custom wallpaper behind or across Discord with adjustable opacity, dark overlay, and blur filters.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Image Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.customBackgroundUri != null) {
                            AsyncImage(
                                model = state.customBackgroundUri,
                                contentDescription = "Custom Wallpaper",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(state.backgroundBlur.dp),
                                contentScale = ContentScale.Crop,
                                alpha = state.backgroundOpacity
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = state.backgroundDarkOverlay))
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No Custom Wallpaper Active", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { photoPickerLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Choose Image")
                        }

                        if (state.customBackgroundUri != null) {
                            OutlinedButton(
                                onClick = {
                                    settingsViewModel.setCustomBackground(
                                        uri = null,
                                        blur = 0f,
                                        opacity = 0.85f,
                                        overlay = 0.35f,
                                        scaleMode = "Crop"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Remove")
                            }
                        }
                    }
                }
            }
        }

        if (state.customBackgroundUri != null) {
            item {
                ModuxSectionHeader(title = "Wallpaper Filters & Adjustments")
                ModuxCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Opacity
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Opacity", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${(state.backgroundOpacity * 100).toInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = state.backgroundOpacity,
                                onValueChange = {
                                    settingsViewModel.setCustomBackground(
                                        uri = state.customBackgroundUri,
                                        blur = state.backgroundBlur,
                                        opacity = it,
                                        overlay = state.backgroundDarkOverlay,
                                        scaleMode = state.backgroundScaleMode
                                    )
                                },
                                valueRange = 0.1f..1.0f
                            )
                        }

                        // Blur
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Blur Radius", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${state.backgroundBlur.toInt()} dp", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = state.backgroundBlur,
                                onValueChange = {
                                    settingsViewModel.setCustomBackground(
                                        uri = state.customBackgroundUri,
                                        blur = it,
                                        opacity = state.backgroundOpacity,
                                        overlay = state.backgroundDarkOverlay,
                                        scaleMode = state.backgroundScaleMode
                                    )
                                },
                                valueRange = 0f..25f
                            )
                        }

                        // Dark Overlay
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Dark Overlay Tint", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                Text("${(state.backgroundDarkOverlay * 100).toInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = state.backgroundDarkOverlay,
                                onValueChange = {
                                    settingsViewModel.setCustomBackground(
                                        uri = state.customBackgroundUri,
                                        blur = state.backgroundBlur,
                                        opacity = state.backgroundOpacity,
                                        overlay = it,
                                        scaleMode = state.backgroundScaleMode
                                    )
                                },
                                valueRange = 0.0f..0.9f
                            )
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 5. PLUGINS
// ----------------------------------------------------
@Composable
fun ClientPluginsSection(
    pluginManager: PluginManager
) {
    val states by pluginManager.pluginStates.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Installed, 1: Available, 2: Updates
    var activeSettingsPlugin by remember { mutableStateOf<PluginRuntimeInfo?>(null) }
    var activeDetailPlugin by remember { mutableStateOf<PluginRuntimeInfo?>(null) }

    val installedList = states.values.filter { it.isInstalled }
    val availableList = states.values.filter { !it.isInstalled }

    val filteredList = when (selectedTab) {
        0 -> installedList
        1 -> availableList
        else -> installedList.filter { it.hasUpdate }
    }.filter {
        it.manifest.name.contains(searchQuery, ignoreCase = true) ||
                it.manifest.description.contains(searchQuery, ignoreCase = true) ||
                it.manifest.category.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            ModuxSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search plugins...",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Installed (${installedList.size})", "Catalog (${availableList.size})", "Updates").forEachIndexed { index, label ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(label, fontSize = 12.sp) }
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredList) { info ->
                ModuxCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(info.manifest.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    ModuxBadge(
                                        text = info.manifest.category,
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text("v${info.manifest.version} by ${info.manifest.author}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            if (info.isInstalled) {
                                Switch(
                                    checked = info.isEnabled,
                                    onCheckedChange = { pluginManager.togglePlugin(info.manifest.id, it) }
                                )
                            } else {
                                Button(
                                    onClick = { pluginManager.installPlugin(info.manifest) },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Install")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(info.manifest.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                        if (info.isInstalled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { activeDetailPlugin = info }) {
                                    Text("Details")
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                if (info.manifest.settingsSchema.isNotEmpty()) {
                                    OutlinedButton(
                                        onClick = { activeSettingsPlugin = info },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Settings")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (activeSettingsPlugin != null) {
        PluginSettingsDialog(
            pluginInfo = activeSettingsPlugin!!,
            pluginManager = pluginManager,
            onDismiss = { activeSettingsPlugin = null }
        )
    }

    if (activeDetailPlugin != null) {
        PluginDetailDialog(
            pluginInfo = activeDetailPlugin!!,
            pluginManager = pluginManager,
            onConfigureClick = {
                val p = activeDetailPlugin
                activeDetailPlugin = null
                activeSettingsPlugin = p
            },
            onDismiss = { activeDetailPlugin = null }
        )
    }
}

// ----------------------------------------------------
// 6. MESSAGES
// ----------------------------------------------------
@Composable
fun ClientMessagesSection(
    settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Message Formatting Tools")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Markdown Preview Bar", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Assists with standard Discord Markdown (*bold*, _italics_, `code`)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.enableMessageFormattingPreview,
                            onCheckedChange = {
                                settingsViewModel.setMessagePreferences(it, state.timestampFormat, state.quickEmojiBar)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Quick Reaction Emoji Bar", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Display convenient quick reaction shortcuts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.quickEmojiBar,
                            onCheckedChange = {
                                settingsViewModel.setMessagePreferences(state.enableMessageFormattingPreview, state.timestampFormat, it)
                            }
                        )
                    }
                }
            }
        }

        item {
            ModuxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Message tools operate purely on standard client-side input helpers. Token harvesting and unauthorized scraping are strictly blocked by ModuX safety protocols.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 7. NOTIFICATIONS
// ----------------------------------------------------
@Composable
fun ClientNotificationsSection(
    settingsViewModel: SettingsViewModel,
    permissionManager: PermissionManager
) {
    val state by settingsViewModel.uiState.collectAsState()
    val permState by permissionManager.state.collectAsState()

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionManager.refreshPermissions()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Notification Preferences")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("In-App Notification Alerts", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Show banner overlays for incoming alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.enableInAppNotifications,
                            onCheckedChange = {
                                settingsViewModel.setNotificationPreferences(it, state.notificationSound, state.notificationVibration, state.notificationPriority)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notification Sounds", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Play chime for direct mentions", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.notificationSound,
                            onCheckedChange = {
                                settingsViewModel.setNotificationPreferences(state.enableInAppNotifications, it, state.notificationVibration, state.notificationPriority)
                            }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Vibration", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Haptic feedback on alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.notificationVibration,
                            onCheckedChange = {
                                settingsViewModel.setNotificationPreferences(state.enableInAppNotifications, state.notificationSound, it, state.notificationPriority)
                            }
                        )
                    }
                }
            }
        }

        item {
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("System Notification Permission", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(
                            text = if (permState.notificationStatus == PermissionStatus.GRANTED) "Granted" else "Permission Required",
                            fontSize = 12.sp,
                            color = if (permState.notificationStatus == PermissionStatus.GRANTED) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                        )
                    }
                    if (permState.notificationStatus != PermissionStatus.GRANTED) {
                        Button(
                            onClick = {
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    permissionManager.openAppSettings()
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 8. VOICE
// ----------------------------------------------------
@Composable
fun ClientVoiceSection(
    permissionManager: PermissionManager
) {
    val permState by permissionManager.state.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionManager.refreshPermissions()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Voice Input & Microphone")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Microphone Access", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Required for Discord Web voice channels", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (permState.microphoneStatus == PermissionStatus.GRANTED) {
                        ModuxBadge(
                            text = "Granted",
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            contentColor = Color(0xFF10B981)
                        )
                    } else {
                        Button(
                            onClick = { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 9. VIDEO
// ----------------------------------------------------
@Composable
fun ClientVideoSection(
    permissionManager: PermissionManager,
    settingsViewModel: SettingsViewModel
) {
    val permState by permissionManager.state.collectAsState()
    val state by settingsViewModel.uiState.collectAsState()

    val camLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionManager.refreshPermissions()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Video & Camera")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Camera Access", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Required for video calls and streams", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (permState.cameraStatus == PermissionStatus.GRANTED) {
                            ModuxBadge(
                                text = "Granted",
                                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                contentColor = Color(0xFF10B981)
                            )
                        } else {
                            Button(
                                onClick = { camLauncher.launch(android.Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Preferred Video Profile", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("Default WebRTC resolution constraint", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(state.preferredVideoQuality, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 10. SCREEN SHARE
// ----------------------------------------------------
@Composable
fun ClientScreenShareSection(
    screenShareManager: ScreenShareManager
) {
    val info by screenShareManager.info.collectAsState()
    val context = LocalContext.current

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        screenShareManager.onCapturePermissionResult(result.resultCode, result.data)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Native Screen Sharing")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Screen Capture Engine", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Android MediaProjection Architecture", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        if (info.isSharing) {
                            ModuxBadge(
                                text = "Capturing",
                                containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                                contentColor = Color(0xFF10B981)
                            )
                        } else {
                            ModuxBadge(
                                text = "Idle",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = info.statusMessage,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!info.isSharing) {
                            Button(
                                onClick = {
                                    val intent = screenShareManager.createScreenCaptureIntent()
                                    if (intent != null) {
                                        screenCaptureLauncher.launch(intent)
                                    } else {
                                        Toast.makeText(context, "MediaProjection not supported on this device", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ScreenShare, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Screen Share Session")
                            }
                        } else {
                            Button(
                                onClick = { screenShareManager.stopScreenSharing() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Stop Screen Share")
                            }
                        }
                    }
                }
            }
        }

        item {
            ModuxCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column {
                    Text("Architecture Notice", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = info.webrtcNotice,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// 11. PERMISSION CENTER
// ----------------------------------------------------
@Composable
fun ClientPermissionsSection(
    permissionManager: PermissionManager
) {
    val state by permissionManager.state.collectAsState()

    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionManager.refreshPermissions() }
    val camLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionManager.refreshPermissions() }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permissionManager.refreshPermissions() }

    val permissionsList = listOf(
        Triple("Microphone", "Voice channels & voice calls", state.microphoneStatus to { micLauncher.launch(android.Manifest.permission.RECORD_AUDIO) }),
        Triple("Camera", "Video calls & live avatar video", state.cameraStatus to { camLauncher.launch(android.Manifest.permission.CAMERA) }),
        Triple("Notifications", "Direct mentions & chat alerts", state.notificationStatus to {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                notifLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                permissionManager.openAppSettings()
            }
        }),
        Triple("Photos & Media", "Custom wallpaper upload & image sharing", state.mediaPhotosStatus to { permissionManager.openAppSettings() })
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "System Permission Center")
        }

        items(permissionsList) { (title, subtitle, pair) ->
            val (status, onRequest) = pair
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (status == PermissionStatus.GRANTED) {
                        ModuxBadge(
                            text = "Granted",
                            containerColor = Color(0xFF10B981).copy(alpha = 0.2f),
                            contentColor = Color(0xFF10B981)
                        )
                    } else {
                        Button(
                            onClick = onRequest,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Grant")
                        }
                    }
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { permissionManager.openAppSettings() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open Android App Settings")
            }
        }
    }
}

// ----------------------------------------------------
// 12. KEYBINDS
// ----------------------------------------------------
@Composable
fun ClientKeybindsSection(
    settingsViewModel: SettingsViewModel
) {
    val state by settingsViewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModuxSectionHeader(title = "Configured Keyboard Shortcuts")
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Shortcut")
                }
            }
        }

        items(state.keybinds.toList()) { (action, shortcut) ->
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(action, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Triggers $action", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = shortcut,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var actionText by remember { mutableStateOf("") }
        var shortcutText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Custom Keybind") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = actionText,
                        onValueChange = { actionText = it },
                        label = { Text("Action Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shortcutText,
                        onValueChange = { shortcutText = it },
                        label = { Text("Key Combination (e.g. Ctrl + Shift + P)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (actionText.isNotBlank() && shortcutText.isNotBlank()) {
                            settingsViewModel.updateKeybind(actionText, shortcutText)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ----------------------------------------------------
// 13. PERFORMANCE
// ----------------------------------------------------
@Composable
fun ClientPerformanceSection(
    settingsViewModel: SettingsViewModel,
    onReloadDiscordWeb: () -> Unit
) {
    val state by settingsViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val modes = listOf("Balanced", "Performance", "Battery Saver")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Performance Tuning")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Select how ModuX balances memory caching and graphical rendering speed:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        modes.forEach { mode ->
                            val isSelected = state.performanceMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { settingsViewModel.setPerformanceMode(mode) },
                                label = { Text(mode, fontSize = 12.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hardware Acceleration", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("Use GPU for WebView rendering and CSS compositing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = state.hardwareAcceleration,
                            onCheckedChange = { settingsViewModel.setHardwareAcceleration(it) }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    System.gc()
                    Toast.makeText(context, "Trimmed memory and ran garbage collection", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Trim Memory & Run Garbage Collection")
            }
        }
    }
}

// ----------------------------------------------------
// 14. BACKUP & RESTORE
// ----------------------------------------------------
@Composable
fun ClientBackupRestoreSection(
    settingsViewModel: SettingsViewModel,
    themeManager: ThemeManager,
    pluginManager: PluginManager,
    onReloadDiscordWeb: () -> Unit
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var importJsonText by remember { mutableStateOf("") }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var importStatus by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Export Configuration")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Generate a versioned JSON backup of your current themes, plugin configurations, keybinds, and display settings.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            val pluginConfigs = pluginManager.exportPluginConfigs()
                            val backupObj = org.json.JSONObject().apply {
                                put("version", "1.0.0")
                                put("app", "ModuX")
                                put("timestamp", System.currentTimeMillis())
                                put("plugins", org.json.JSONObject(pluginConfigs))
                                put("theme", themeManager.currentTheme.value.id)
                                put("accent", themeManager.customAccentOverride.value ?: "")
                            }
                            val jsonString = backupObj.toString(2)
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("ModuX Backup", jsonString)
                            clipboard.setPrimaryClip(clip)
                            exportStatus = "Configuration JSON copied to clipboard (${jsonString.length} bytes)"
                            Toast.makeText(context, "Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export to Clipboard (JSON)")
                    }

                    if (exportStatus != null) {
                        Text(
                            text = exportStatus ?: "",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "Import Configuration")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Paste a ModuX JSON configuration block below to restore settings.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        label = { Text("Backup JSON String") },
                        placeholder = { Text("{\"version\": \"1.0.0\", ...}") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 6,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (importJsonText.isBlank()) {
                                    importStatus = "Please enter or paste JSON content."
                                    return@Button
                                }
                                try {
                                    val obj = org.json.JSONObject(importJsonText)
                                    val version = obj.optString("version", "unknown")
                                    if (obj.has("plugins")) {
                                        pluginManager.importPluginConfigs(obj.getJSONObject("plugins").toString())
                                    }
                                    if (obj.has("theme")) {
                                        themeManager.applyTheme(obj.getString("theme"))
                                    }
                                    if (obj.has("accent") && obj.getString("accent").isNotBlank()) {
                                        themeManager.setAccentOverride(obj.getString("accent"))
                                    }
                                    importStatus = "Successfully imported configuration (Schema v$version)!"
                                    Toast.makeText(context, "Settings imported!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    importStatus = "Invalid JSON format: ${e.message}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Validate & Restore")
                        }

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                                val clip = clipboard?.primaryClip?.getItemAt(0)?.text?.toString()
                                if (!clip.isNullOrBlank()) {
                                    importJsonText = clip
                                    Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Paste")
                        }
                    }

                    if (importStatus != null) {
                        Text(
                            text = importStatus ?: "",
                            fontSize = 12.sp,
                            color = if (importStatus?.startsWith("Success") == true) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "WebView Maintenance & Cache")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onReloadDiscordWeb,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Force Reload Discord Web")
                    }

                    OutlinedButton(
                        onClick = {
                            android.webkit.WebStorage.getInstance().deleteAllData()
                            Toast.makeText(context, "WebView temporary cache cleared", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear WebView Cache")
                    }
                }
            }
        }

        item {
            ModuxSectionHeader(title = "Danger Zone")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Reset all custom themes, plugin states, and keybinds to defaults:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(
                        onClick = { showResetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Reset All Customizations")
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Customizations?") },
            text = { Text("Are you sure you want to reset all themes, plugins, and custom background settings to default values?") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetAllSettings()
                        showResetDialog = false
                        Toast.makeText(context, "All customizations have been reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ----------------------------------------------------
// 15. DEVELOPER
// ----------------------------------------------------
@Composable
fun ClientDeveloperSection(
    developerViewModel: DeveloperViewModel
) {
    val devUiState by developerViewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxSectionHeader(title = "Runtime Diagnostics")
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("JVM Heap Usage", fontSize = 13.sp)
                        Text("${devUiState.telemetry.usedMemoryMb} MB / ${devUiState.telemetry.maxMemoryMb} MB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { devUiState.telemetry.usedMemoryMb.toFloat() / maxOf(1L, devUiState.telemetry.maxMemoryMb).toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModuxSectionHeader(title = "Live Terminal Logs")
                IconButton(onClick = { developerViewModel.clearLogs() }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Logs")
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(10.dp)),
                color = Color(0xFF0D1117),
                border = BorderStroke(1.dp, Color(0xFF30363D))
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(devUiState.logs) { entry ->
                        Text(
                            text = "[${entry.tag}] ${entry.message}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = when (entry.level) {
                                LogLevel.ERROR -> Color(0xFFF87171)
                                LogLevel.WARNING -> Color(0xFFFBBF24)
                                LogLevel.INFO -> Color(0xFF60A5FA)
                                LogLevel.DEBUG -> Color(0xFF9CA3AF)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// 16. ABOUT
// ----------------------------------------------------
@Composable
fun ClientAboutSection(
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModuxCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("ModuX Discord Customization Hub", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Version ${AppConfig.APP_VERSION}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A lightweight, modular customization client layer for Discord Web featuring sandboxed plugin SDK, real-time theme generator, custom wallpaper filters, and native Android permission & screen capture routing.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            ModuxCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val msg = settingsViewModel.onVersionTapped()
                        if (msg != null) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
            ) {
                Column {
                    Text("Build Architecture", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Kotlin • Jetpack Compose • Android Gradle Plugin • DataStore", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
