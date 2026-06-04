package com.mangamojo.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.core.MangaDex
import com.mangamojo.app.domain.model.ReadingDirection
import com.mangamojo.app.domain.model.ThemeMode
import com.mangamojo.app.domain.model.ThemePalette
import com.mangamojo.app.ui.components.ConfirmDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val cachedCount by viewModel.cachedCount.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<ConfirmTarget?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            SettingsSection("Appearance")
            SettingRow(title = "Theme mode", subtitle = "Use system, light, or dark display mode.") {
                SingleChoiceSegmentedButtonRow {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = settings.themeMode == mode,
                            onClick = { viewModel.onThemeModeChange(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index, ThemeMode.entries.size),
                        ) { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
            }
            SettingRow(title = "Color theme", subtitle = "Default is Shonen Crimson.") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemePalette.entries.forEach { palette ->
                        FilterChip(
                            selected = settings.themePalette == palette,
                            onClick = { viewModel.onThemePaletteChange(palette) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(palette.swatchColor),
                                )
                            },
                            label = { Text(palette.label) },
                        )
                    }
                }
            }

            HorizontalDivider()
            SettingsSection("Reader")
            SettingRow(
                title = "Reading direction",
                subtitle = "Vertical (webtoon) is used in this version; other modes are coming soon.",
            ) {
                SingleChoiceSegmentedButtonRow {
                    ReadingDirection.entries.forEachIndexed { index, dir ->
                        SegmentedButton(
                            selected = settings.readingDirection == dir,
                            onClick = { viewModel.onReadingDirectionChange(dir) },
                            shape = SegmentedButtonDefaults.itemShape(index, ReadingDirection.entries.size),
                        ) { Text(dir.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
            }
            ToggleRow(
                title = "Data saver",
                subtitle = "Load lower-resolution pages to save bandwidth.",
                checked = settings.dataSaver,
                onCheckedChange = viewModel::onDataSaverChange,
            )

            HorizontalDivider()
            SettingsSection("Content")
            SettingRow(
                title = "Content ratings",
                subtitle = "Which ratings to include in search and browse.",
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MangaDex.ALL_CONTENT_RATINGS.forEach { rating ->
                        FilterChip(
                            selected = rating in settings.contentRatings,
                            onClick = { viewModel.onToggleContentRating(rating) },
                            label = { Text(rating.replaceFirstChar { it.uppercase() }) },
                        )
                    }
                }
            }

            HorizontalDivider()
            SettingsSection("Storage")
            ClickableRow(
                title = "Clear cache",
                subtitle = "$cachedCount cached titles. Favorites and history are kept.",
                onClick = { dialog = ConfirmTarget.CACHE },
            )
            ClickableRow(
                title = "Clear history",
                subtitle = "Remove all reading history and progress.",
                onClick = { dialog = ConfirmTarget.HISTORY },
            )
            ClickableRow(
                title = "Clear favorites",
                subtitle = "Remove every title from your library.",
                onClick = { dialog = ConfirmTarget.FAVORITES },
            )

            HorizontalDivider()
            Text(
                text = "MangaMojo - Phase 1 - Source: MangaDex",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
            )
        }
    }

    dialog?.let { target ->
        ConfirmDialog(
            title = target.title,
            message = target.message,
            confirmLabel = "Clear",
            onConfirm = {
                when (target) {
                    ConfirmTarget.CACHE -> viewModel.onClearCache()
                    ConfirmTarget.HISTORY -> viewModel.onClearHistory()
                    ConfirmTarget.FAVORITES -> viewModel.onClearFavorites()
                }
            },
            onDismiss = { dialog = null },
        )
    }
}

private enum class ConfirmTarget(val title: String, val message: String) {
    CACHE("Clear cache?", "Cached metadata will be removed. Favorites and history are kept."),
    HISTORY("Clear history?", "All reading history and progress will be removed."),
    FAVORITES("Clear favorites?", "All titles will be removed from your library."),
}

private val ThemePalette.label: String
    get() = when (this) {
        ThemePalette.SHONEN_CRIMSON -> "Shonen"
        ThemePalette.NEON_CYBERPUNK -> "Cyberpunk"
        ThemePalette.RETRO_SHONEN -> "Retro"
        ThemePalette.MYSTICAL_DARK_SAGE -> "Sage"
    }

private val ThemePalette.swatchColor: Color
    get() = when (this) {
        ThemePalette.SHONEN_CRIMSON -> Color(0xFFE50914)
        ThemePalette.NEON_CYBERPUNK -> Color(0xFFA855F7)
        ThemePalette.RETRO_SHONEN -> Color(0xFFFF6B00)
        ThemePalette.MYSTICAL_DARK_SAGE -> Color(0xFF10B981)
    }

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingRow(title: String, subtitle: String, control: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Column(modifier = Modifier.padding(top = 8.dp)) { control() }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun ClickableRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onClick) { Text("Clear") }
    }
}
