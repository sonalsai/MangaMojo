package com.mangamojo.app.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.ui.components.ConfirmDialog
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.MangaCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onMangaClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Library") },
            windowInsets = WindowInsets(0, 0, 0, 0),
            actions = {
                if (favorites.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Rounded.DeleteSweep, contentDescription = "Clear favorites")
                    }
                }
            },
        )

        if (favorites.isEmpty()) {
            EmptyState(
                title = "No favorites yet",
                message = "Tap the heart on any manga to save it to your library.",
                icon = Icons.Rounded.FavoriteBorder,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(112.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(favorites, key = { it.mangaId }) { favorite ->
                    MangaCard(
                        title = favorite.title,
                        coverUrl = favorite.coverUrl,
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
