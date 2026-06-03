package com.mangamojo.app.ui.details

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.core.UiState
import com.mangamojo.app.domain.model.Chapter
import com.mangamojo.app.domain.model.MangaDetails
import com.mangamojo.app.ui.components.CoverImage
import com.mangamojo.app.ui.components.EmptyState
import com.mangamojo.app.ui.components.ErrorState
import com.mangamojo.app.ui.components.LoadingState
import com.mangamojo.app.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    onChapterClick: (chapterId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val details = state.details

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = (details as? UiState.Success)?.data?.title ?: "Details",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (details is UiState.Success) {
                    IconButton(onClick = viewModel::onToggleFavorite) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Rounded.Favorite
                            else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (state.isFavorite) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        when (details) {
            is UiState.Loading -> LoadingState()
            is UiState.Error -> ErrorState(details.error.userMessage, onRetry = viewModel::load)
            is UiState.Success -> DetailsContent(
                details = details.data,
                chapters = state.chapters,
                readChapterIds = state.readChapterIds,
                onChapterClick = onChapterClick,
                onOpenExternal = { uriHandler.openUri(it) },
                onRetryChapters = viewModel::load,
            )
        }
    }
}

@Composable
private fun DetailsContent(
    details: MangaDetails,
    chapters: UiState<List<Chapter>>,
    readChapterIds: Set<String>,
    onChapterClick: (String) -> Unit,
    onOpenExternal: (String) -> Unit,
    onRetryChapters: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item { HeaderSection(details) }

        item {
            val count = (chapters as? UiState.Success)?.data?.size
            SectionHeader(if (count != null) "Chapters ($count)" else "Chapters")
        }

        when (chapters) {
            is UiState.Loading -> item {
                Box(Modifier.fillMaxWidth().height(160.dp)) { LoadingState() }
            }
            is UiState.Error -> item {
                Box(Modifier.fillMaxWidth().height(160.dp)) {
                    ErrorState(chapters.error.userMessage, onRetry = onRetryChapters)
                }
            }
            is UiState.Success -> {
                if (chapters.data.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(160.dp)) {
                            EmptyState("No chapters", "There are no readable chapters in your language yet.")
                        }
                    }
                } else {
                    items(chapters.data, key = { it.id }) { chapter ->
                        ChapterRow(
                            chapter = chapter,
                            isRead = chapter.id in readChapterIds,
                            onClick = {
                                if (chapter.isExternal) onOpenExternal(chapter.externalUrl!!)
                                else onChapterClick(chapter.id)
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(details: MangaDetails) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CoverImage(
                url = details.coverUrl,
                contentDescription = details.title,
                modifier = Modifier.width(120.dp).height(180.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(details.title, style = MaterialTheme.typography.titleLarge, maxLines = 3, overflow = TextOverflow.Ellipsis)
                val author = (details.authors + details.artists).distinct().joinToString(", ").ifBlank { "Unknown author" }
                Text(author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(details.status.label) })
                    details.year?.let { Text(it.toString(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }

        if (details.tags.isNotEmpty()) {
            Text(
                text = details.tags.take(6).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        if (details.description.isNotBlank()) {
            Text(
                text = details.description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun ChapterRow(
    chapter: Chapter,
    isRead: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = chapter.label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isRead) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = chapter.scanlationGroup,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (chapter.isExternal) {
            Icon(
                Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = "Opens externally",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else if (isRead) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = "Read",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
