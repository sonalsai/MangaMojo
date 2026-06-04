package com.mangamojo.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.domain.model.HistoryEntry
import com.mangamojo.app.domain.model.Manga
import com.mangamojo.app.ui.components.CoverImage
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.ErrorState
import com.mangamojo.app.ui.components.LoadingState
import com.mangamojo.app.ui.components.ProgressOverlayCard
import com.mangamojo.app.ui.components.RailCard
import com.mangamojo.app.ui.components.SectionHeader
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMangaClick: (String) -> Unit,
    onResume: (mangaId: String, chapterId: String) -> Unit,
    onSearch: () -> Unit,
    onSeeFavorites: () -> Unit,
    onSeeHistory: () -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val discover = state.discover
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            (layout.visibleItemsInfo.lastOrNull()?.index ?: 0) to layout.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (total > 0 && lastVisible >= total - 2) viewModel.loadMore()
            }
    }

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
                IconButton(onClick = onSearch) {
                    Icon(Icons.Rounded.Search, contentDescription = "Search")
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

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                val hero = discover.items.firstOrNull()
                if (hero != null) {
                    HeroSection(manga = hero, onReadNow = { onMangaClick(hero.id) })
                } else {
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        if (discover.loading) LoadingState()
                    }
                }
            }

            if (state.history.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Continue Reading",
                        actionLabel = "View All",
                        onAction = onSeeHistory,
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.history.take(12), key = { it.mangaId }) { entry ->
                            ProgressOverlayCard(
                                title = entry.title,
                                coverUrl = entry.coverUrl,
                                progress = entry.progressFraction,
                                subtitle = entry.progressLabel,
                                onClick = { onResume(entry.mangaId, entry.chapterId) },
                            )
                        }
                    }
                }
            }

            if (state.favorites.isNotEmpty()) {
                item {
                    LibraryRail(
                        favorites = state.favorites,
                        onMangaClick = onMangaClick,
                        onSeeFavorites = onSeeFavorites,
                    )
                }
            }

            item {
                DiscoverTabs(selected = discover.tab, onSelect = viewModel::selectTab)
            }

            item {
                DiscoverRail(
                    discover = discover,
                    onMangaClick = onMangaClick,
                    onRetry = viewModel::retry,
                )
            }

            if (discover.items.size > 10) {
                item {
                    SectionHeader(title = "More to Explore")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(discover.items.drop(10).take(16), key = { "${it.sourceId}:${it.id}" }) { manga ->
                            RailCard(
                                title = manga.title,
                                coverUrl = manga.coverUrl,
                                subtitle = manga.metadata,
                                onClick = { onMangaClick(manga.id) },
                            )
                        }
                        if (discover.loadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.size(width = 80.dp, height = 180.dp),
                                    contentAlignment = Alignment.Center,
                                ) { CircularProgressIndicator() }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(
    manga: Manga,
    onReadNow: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(420.dp),
    ) {
        CoverImage(
            url = manga.coverUrl,
            contentDescription = manga.title,
            cornerRadius = 0,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to MaterialTheme.colorScheme.background.copy(alpha = 0.06f),
                        0.52f to MaterialTheme.colorScheme.background.copy(alpha = 0.36f),
                        1f to MaterialTheme.colorScheme.background,
                    )
                ),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = manga.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroChip(manga.status.label)
                HeroChip(manga.contentRating.prettyLabel())
            }
            Text(
                text = "A featured MangaMojo pick with striking art, fast chapters, and a story ready for your next binge.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.92f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onReadNow,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Read Now")
                }
                OutlinedButton(
                    onClick = onReadNow,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("My List")
                }
            }
        }
    }
}

@Composable
private fun HeroChip(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun LibraryRail(
    favorites: List<Favorite>,
    onMangaClick: (String) -> Unit,
    onSeeFavorites: () -> Unit,
) {
    SectionHeader(title = "My Library", actionLabel = "View All", onAction = onSeeFavorites)
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(favorites.take(12), key = { it.mangaId }) { favorite ->
            RailCard(
                title = favorite.title,
                coverUrl = favorite.coverUrl,
                subtitle = favorite.status.label,
                onClick = { onMangaClick(favorite.mangaId) },
            )
        }
    }
}

@Composable
private fun DiscoverTabs(
    selected: DiscoverTab,
    onSelect: (DiscoverTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DiscoverTab.entries.forEach { tab ->
            FilterChip(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                label = { Text(if (tab == DiscoverTab.POPULAR) "Popular Manga" else "Latest Updates") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Composable
private fun DiscoverRail(
    discover: DiscoverState,
    onMangaClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    val title = if (discover.tab == DiscoverTab.POPULAR) "Popular Manga" else "Latest Updates"
    SectionHeader(title = title)
    when {
        discover.loading && discover.items.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp)) {
            LoadingState()
        }

        discover.error != null && discover.items.isEmpty() -> Box(Modifier.fillMaxWidth().height(220.dp)) {
            ErrorState(discover.error.userMessage, onRetry = onRetry)
        }

        discover.items.isEmpty() -> Box(Modifier.fillMaxWidth().height(180.dp)) {
            EmptyState("Nothing here", "No titles to show in this rail right now.")
        }

        else -> LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(discover.items.take(18), key = { "${it.sourceId}:${it.id}" }) { manga ->
                RailCard(
                    title = manga.title,
                    coverUrl = manga.coverUrl,
                    subtitle = manga.metadata,
                    badge = if (discover.tab == DiscoverTab.LATEST) "NEW" else null,
                    onClick = { onMangaClick(manga.id) },
                )
            }
        }
    }
}

private val Manga.metadata: String
    get() = listOf(status.label, contentRating.prettyLabel())
        .filter { it.isNotBlank() }
        .joinToString(" - ")

private val HistoryEntry.progressLabel: String
    get() {
        val remaining = (total - page - 1).coerceAtLeast(0)
        return if (total > 0) "$chapterLabel - $remaining pages left" else chapterLabel
    }

private fun String.prettyLabel(): String =
    replaceFirstChar { it.uppercase() }
