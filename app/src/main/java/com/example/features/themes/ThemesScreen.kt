package com.example.features.themes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.theme.ThemeDefinition
import com.example.theme.ThemeManager
import com.example.ui.components.ModuxBadge
import com.example.ui.components.ModuxCard
import com.example.ui.components.ModuxSearchBar
import com.example.ui.components.ModuxSectionHeader

enum class ThemeCategoryTab(val title: String) {
    ALL("All Themes"),
    DARK("Dark & AMOLED"),
    LIGHT("Light"),
    CUSTOM("Custom")
}

@Composable
fun ThemesScreen(
    themeManager: ThemeManager
) {
    val allThemes by themeManager.allThemes.collectAsState()
    val currentTheme by themeManager.currentTheme.collectAsState()
    val accentOverride by themeManager.customAccentOverride.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(ThemeCategoryTab.ALL) }
    var isCreateCustomThemeDialogOpen by remember { mutableStateOf(false) }

    val accentPalette = listOf(
        null, // Theme Default
        "#6366F1", // Indigo
        "#8B5CF6", // Purple
        "#EC4899", // Pink
        "#F43F5E", // Rose
        "#10B981", // Emerald
        "#06B6D4", // Cyan
        "#F59E0B"  // Amber
    )

    val filteredThemes = allThemes.filter { theme ->
        val matchesTab = when (selectedTab) {
            ThemeCategoryTab.ALL -> true
            ThemeCategoryTab.DARK -> theme.isDark && !theme.isCustom
            ThemeCategoryTab.LIGHT -> !theme.isDark && !theme.isCustom
            ThemeCategoryTab.CUSTOM -> theme.isCustom
        }
        val matchesSearch = searchQuery.isBlank() ||
                theme.name.contains(searchQuery, ignoreCase = true) ||
                theme.description.contains(searchQuery, ignoreCase = true) ||
                theme.author.contains(searchQuery, ignoreCase = true)

        matchesTab && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("themes_screen_scroll"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Search bar
                ModuxSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search themes by name or author..."
                )
            }

            // Quick Accent Color Picker Strip
            item {
                ModuxCard(
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Accent Color Override",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (accentOverride != null) {
                                Text(
                                    text = "Reset",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable { themeManager.setAccentOverride(null) }
                                        .padding(4.dp)
                                        .testTag("reset_accent_override_btn")
                                )
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(accentPalette) { hex ->
                                val isSelected = (hex == null && accentOverride == null) || (hex != null && hex.equals(accentOverride, ignoreCase = true))
                                val displayColor = if (hex != null) parseColorHex(hex) else currentTheme.toAccentColor()

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(displayColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { themeManager.setAccentOverride(hex) }
                                        .testTag(if (hex == null) "accent_default_btn" else "accent_${hex.removePrefix("#")}_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Navigation Row
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    ThemeCategoryTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("tab_theme_${tab.name.lowercase()}")
                        )
                    }
                }
            }

            // Create Custom Theme Action Button
            item {
                Button(
                    onClick = { isCreateCustomThemeDialogOpen = true },
                    modifier = Modifier.fillMaxWidth().testTag("create_custom_theme_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Design Custom Theme")
                }
            }

            // Themes List
            items(filteredThemes) { theme ->
                ThemeCardItem(
                    theme = theme,
                    isApplied = theme.id == currentTheme.id,
                    onApply = {
                        themeManager.applyTheme(theme.id)
                    },
                    onDelete = if (theme.isCustom) {
                        { themeManager.deleteCustomTheme(theme.id) }
                    } else null
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Create Custom Theme Dialog
        if (isCreateCustomThemeDialogOpen) {
            CreateCustomThemeDialog(
                onDismiss = { isCreateCustomThemeDialogOpen = false },
                onSave = { newTheme ->
                    themeManager.createAndApplyCustomTheme(newTheme)
                    isCreateCustomThemeDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun ThemeCardItem(
    theme: ThemeDefinition,
    isApplied: Boolean,
    onApply: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    ModuxCard(
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
        borderColor = if (isApplied) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Preview Canvas
            LiveThemePreviewCanvas(theme = theme)

            // Header Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (theme.isCustom) {
                            ModuxBadge(
                                text = "CUSTOM",
                                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Text(
                        text = "by ${theme.author} • v${theme.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onDelete != null) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_theme_${theme.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete custom theme",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Apply Button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().testTag("apply_theme_${theme.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isApplied) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                    contentColor = if (isApplied) MaterialTheme.colorScheme.primary else Color.White
                ),
                border = if (isApplied) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                if (isApplied) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Applied", fontWeight = FontWeight.Bold)
                } else {
                    Text("Apply Theme")
                }
            }
        }
    }
}

@Composable
fun LiveThemePreviewCanvas(theme: ThemeDefinition) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp)),
        color = theme.toBackgroundColor(),
        border = BorderStroke(1.dp, theme.toBorderColor())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Mini Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ModuX Canvas",
                    color = theme.toTextPrimaryColor(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(theme.toAccentColor())
                )
            }

            // Mini Mock Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = theme.toCardColor(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, theme.toBorderColor())
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sample Component",
                            color = theme.toTextPrimaryColor(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Color preview palette",
                            color = theme.toTextSecondaryColor(),
                            fontSize = 8.sp
                        )
                    }

                    Surface(
                        color = theme.toAccentColor(),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Active",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomThemeDialog(
    onDismiss: () -> Unit,
    onSave: (ThemeDefinition) -> Unit
) {
    var themeName by remember { mutableStateOf("My Custom Palette") }
    var authorName by remember { mutableStateOf("You") }
    var description by remember { mutableStateOf("Bespoke custom user theme.") }

    var backgroundHex by remember { mutableStateOf("#0C0F1D") }
    var surfaceHex by remember { mutableStateOf("#15192C") }
    var cardHex by remember { mutableStateOf("#1E243D") }
    var textPrimaryHex by remember { mutableStateOf("#F8FAFC") }
    var textSecondaryHex by remember { mutableStateOf("#94A3B8") }
    var accentHex by remember { mutableStateOf("#6366F1") }

    val previewTheme = ThemeDefinition(
        id = "custom_${System.currentTimeMillis()}",
        name = themeName.ifBlank { "Custom Theme" },
        author = authorName.ifBlank { "You" },
        description = description,
        isDark = true,
        isCustom = true,
        backgroundHex = backgroundHex,
        surfaceHex = surfaceHex,
        cardHex = cardHex,
        textPrimaryHex = textPrimaryHex,
        textSecondaryHex = textSecondaryHex,
        accentHex = accentHex,
        borderHex = "#334155"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("create_theme_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Design Custom Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live preview before applying",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Live Preview Canvas
                LiveThemePreviewCanvas(theme = previewTheme)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = themeName,
                            onValueChange = { themeName = it },
                            label = { Text("Theme Name") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_theme_name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    item {
                        ColorPickerPresetRow(
                            label = "Accent Color",
                            selectedHex = accentHex,
                            presets = listOf("#6366F1", "#8B5CF6", "#EC4899", "#F43F5E", "#10B981", "#06B6D4", "#F59E0B", "#EAB308"),
                            onSelect = { accentHex = it }
                        )
                    }

                    item {
                        ColorPickerPresetRow(
                            label = "Background Tint",
                            selectedHex = backgroundHex,
                            presets = listOf("#0C0F1D", "#000000", "#0D1117", "#051A14", "#120A20", "#1C1917", "#F8FAFC"),
                            onSelect = { backgroundHex = it }
                        )
                    }

                    item {
                        ColorPickerPresetRow(
                            label = "Card & Surface Tint",
                            selectedHex = cardHex,
                            presets = listOf("#1E243D", "#161616", "#21262D", "#143328", "#24173D", "#292524", "#F1F5F9"),
                            onSelect = {
                                cardHex = it
                                surfaceHex = it
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                Button(
                    onClick = { onSave(previewTheme) },
                    modifier = Modifier.fillMaxWidth().testTag("save_custom_theme_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save & Apply Theme")
                }
            }
        }
    }
}

@Composable
fun ColorPickerPresetRow(
    label: String,
    selectedHex: String,
    presets: List<String>,
    onSelect: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = selectedHex,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(presets) { hex ->
                val isSelected = hex.equals(selectedHex, ignoreCase = true)
                val color = parseColorHex(hex)
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelect(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun parseColorHex(hex: String): Color {
    return try {
        val clean = hex.removePrefix("#")
        Color((0xFF000000 or clean.toLong(16)).toInt())
    } catch (_: Exception) {
        Color(0xFF6366F1)
    }
}
