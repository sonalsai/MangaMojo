package com.mangamojo.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.core.UiState
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.ErrorState
import com.mangamojo.app.ui.components.LoadingState
import com.mangamojo.app.ui.components.ProgressOverlayCard
import com.mangamojo.app.ui.components.RailCard
import com.mangamojo.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMangaClick: (String) -> Unit,
    onContinueClick: (mangaId: String, chapterId: String) -> Unit,
    onSeeFavorites: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("MangaMojo") },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (state.continueReading.isNotEmpty()) {
                item { SectionHeader("Continue reading") }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(state.continueReading, key = { it.chapterId }) { entry ->
                            ProgressOverlayCard(
                                title = entry.title,
                                coverUrl = entry.coverUrl,
                                progress = entry.progressFraction,
                                subtitle = entry.chapterLabel,
                                onClick = { onContinueClick(entry.mangaId, entry.chapterId) },
                            )
                        }
                    }
                }
            }

            if (state.favorites.isNotEmpty()) {
                item {
                    SectionHeader("Your library", actionLabel = "See all", onAction = onSeeFavorites)
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
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

            item { SectionHeader("Popular on MangaDex") }
            when (val popular = state.popular) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(240.dp)) { LoadingState() }
                }
                is UiState.Error -> item {
                    Box(Modifier.fillMaxWidth().height(240.dp)) {
                        ErrorState(popular.error.userMessage, onRetry = viewModel::loadPopular)
                    }
                }
                is UiState.Success -> {
                    if (popular.data.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp)) {
                                EmptyState("Nothing here", "Couldn't find popular titles right now.")
                            }
                        }
                    } else {
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(popular.data, key = { it.id }) { manga ->
                                    RailCard(
                                        title = manga.title,
                                        coverUrl = manga.coverUrl,
                                        onClick = { onMangaClick(manga.id) },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
