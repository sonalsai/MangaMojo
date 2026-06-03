package com.mangamojo.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.ErrorState
import com.mangamojo.app.ui.components.LoadingState
import com.mangamojo.app.ui.components.MangaCard
import com.mangamojo.app.ui.components.RailCard
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMangaClick: (String) -> Unit,
    onSeeFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val discover = state.discover
    val gridState = rememberLazyGridState()

    // Load the next page of the discovery feed as the grid nears the bottom.
    LaunchedEffect(gridState) {
        snapshotFlow {
            val layout = gridState.layoutInfo
            (layout.visibleItemsInfo.lastOrNull()?.index ?: 0) to layout.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (total > 0 && lastVisible >= total - 4) viewModel.loadMore()
            }
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("MangaMojo") },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(112.dp),
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ---- Library preview (side-scroll rail) ----
            if (state.favorites.isNotEmpty()) {
                fullSpan { RailHeader("Your library", actionLabel = "See all", onAction = onSeeFavorites) }
                fullSpan {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.favorites.take(12), key = { it.mangaId }) { favorite ->
                            RailCard(
                                title = favorite.title,
                                coverUrl = favorite.coverUrl,
                                onClick = { onMangaClick(favorite.mangaId) },
                            )
                        }
                    }
                }
            }

            // ---- Discovery tabs (Popular / Latest) as filter-chip pills ----
            fullSpan {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DiscoverTab.entries.forEach { tab ->
                        val selected = discover.tab == tab
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectTab(tab) },
                            label = { Text(tab.label) },
                            leadingIcon = if (selected) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(FilterChipDefaults.IconSize),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }

            // ---- Discovery feed (vertical grid, scrolls to bottom) ----
            when {
                discover.loading && discover.items.isEmpty() -> fullSpan {
                    Box(Modifier.fillMaxWidth().height(240.dp)) { LoadingState() }
                }

                discover.error != null && discover.items.isEmpty() -> fullSpan {
                    Box(Modifier.fillMaxWidth().height(240.dp)) {
                        ErrorState(discover.error!!.userMessage, onRetry = viewModel::retry)
                    }
                }

                discover.items.isEmpty() -> fullSpan {
                    Box(Modifier.fillMaxWidth().height(200.dp)) {
                        EmptyState("Nothing here", "No titles to show in this tab right now.")
                    }
                }

                else -> {
                    items(discover.items, key = { "${it.sourceId}:${it.id}" }) { manga ->
                        MangaCard(
                            title = manga.title,
                            coverUrl = manga.coverUrl,
                            onClick = { onMangaClick(manga.id) },
                        )
                    }
                    if (discover.loadingMore) {
                        fullSpan {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) { CircularProgressIndicator() }
                        }
                    }
                }
            }
        }
    }
}

/** Adds a full-width (all-columns) row to a [LazyVerticalGrid]. */
private fun LazyGridScope.fullSpan(content: @Composable LazyGridItemScope.() -> Unit) {
    item(span = { GridItemSpan(maxLineSpan) }, content = content)
}

@Composable
private fun RailHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
