package com.mangamojo.app.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.ui.components.ConfirmDialog
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.MangaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onMangaClick: (String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(LibraryTab.FAVORITES) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "MANGAMOJO",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                )
            },
            navigationIcon = {
                IconButton(onClick = onOpenDrawer) {
                    Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                }
            },
            actions = {
                if (favorites.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear favorites")
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LibraryHeader(selectedTab = selectedTab, onSelect = { selectedTab = it })
            }

            if (selectedTab != LibraryTab.FAVORITES) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
                        EmptyState(
                            title = "${selectedTab.label} is empty",
                            message = "This category will fill in as MangaMojo tracks more library state.",
                            icon = Icons.Rounded.FavoriteBorder,
                        )
                    }
                }
            } else if (favorites.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp)) {
                        EmptyState(
                            title = "No favorites yet",
                            message = "Tap the heart on any manga to save it to your library.",
                            icon = Icons.Rounded.FavoriteBorder,
                        )
                    }
                }
            } else {
                items(favorites, key = { it.mangaId }) { favorite ->
                    MangaCard(
                        title = favorite.title,
                        coverUrl = favorite.coverUrl,
                        subtitle = favorite.status.label,
                        badge = if (favorite.isRecent) "NEW" else null,
                        onClick = { onMangaClick(favorite.mangaId) },
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Clear library?",
            message = "This removes all favorites. Your reading history is kept.",
            confirmLabel = "Clear",
            onConfirm = viewModel::clearAll,
            onDismiss = { showClearDialog = false },
        )
    }
}

@Composable
private fun LibraryHeader(
    selectedTab: LibraryTab,
    onSelect: (LibraryTab) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text(
            text = "My Library",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LibraryPill(icon = Icons.AutoMirrored.Rounded.Sort, label = "Latest")
            LibraryPill(icon = Icons.Rounded.FilterList, label = "Filter")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            LibraryTab.entries.forEach { tab ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(tab) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == tab) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .width(1.dp)
                            .background(
                                if (selectedTab == tab) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surface,
                                RoundedCornerShape(99.dp),
                            )
                            .padding(vertical = 1.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null)
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

private enum class LibraryTab(val label: String) {
    FAVORITES("Favorites"),
    READING("Reading"),
    COMPLETED("Completed"),
}

private val Favorite.isRecent: Boolean
    get() = System.currentTimeMillis() - addedAt < 7L * 24L * 60L * 60L * 1000L
