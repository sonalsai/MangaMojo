package com.mangamojo.app.ui.history

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mangamojo.app.domain.model.HistoryEntry
import com.mangamojo.app.ui.components.ConfirmDialog
import com.mangamojo.app.ui.components.CoverImage
import com.mangamojo.app.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onResume: (mangaId: String, chapterId: String) -> Unit,
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            ),
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        if (history.isEmpty()) {
            EmptyState(
                title = "No history yet",
                message = "Chapters you read will show up here so you can pick up where you left off.",
                icon = Icons.Rounded.History,
            )
        } else {
            val groups = history.groupBy { it.historyBucket }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    HistoryHeader(onClear = { showClearDialog = true })
                }

                HistoryBucket.entries.forEach { bucket ->
                    val entries = groups[bucket].orEmpty()
                    if (entries.isNotEmpty()) {
                        item { BucketHeader(bucket.label) }
                        items(entries, key = { it.mangaId }) { entry ->
                            HistoryCard(
                                entry = entry,
                                onClick = { onResume(entry.mangaId, entry.chapterId) },
                                onRemove = { viewModel.remove(entry.mangaId) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearDialog) {
        ConfirmDialog(
            title = "Clear history?",
            message = "This removes all reading history and progress markers.",
            confirmLabel = "Clear",
            onConfirm = viewModel::clearAll,
            onDismiss = { showClearDialog = false },
        )
    }
}

@Composable
private fun HistoryHeader(onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Reading History",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Pick up right where you left off in your journey.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(Icons.Rounded.DeleteSweep, contentDescription = null)
            Text(
                text = "  Clear History",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BucketHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CoverImage(
                url = entry.coverUrl,
                contentDescription = entry.title,
                modifier = Modifier.size(width = 88.dp, height = 120.dp),
                cornerRadius = 8,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = entry.relativeTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                Text(
                    text = "Last read: ${entry.chapterLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${entry.percentComplete}% Complete",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (entry.isCompleted) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${(entry.page + 1).coerceAtMost(entry.total)}/${entry.total.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { entry.progressFraction },
                    modifier = Modifier.fillMaxWidth().height(5.dp),
                    color = if (entry.isCompleted) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f),
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Remove from history")
            }
        }
    }
}

private enum class HistoryBucket(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    EARLIER("Earlier"),
}

private val HistoryEntry.historyBucket: HistoryBucket
    get() {
        val age = System.currentTimeMillis() - readAt
        val dayMillis = 24L * 60L * 60L * 1000L
        return when {
            age < dayMillis -> HistoryBucket.TODAY
            age < dayMillis * 2 -> HistoryBucket.YESTERDAY
            else -> HistoryBucket.EARLIER
        }
    }

private val HistoryEntry.percentComplete: Int
    get() = (progressFraction.coerceIn(0f, 1f) * 100).toInt()

private val HistoryEntry.relativeTime: String
    get() {
        val age = (System.currentTimeMillis() - readAt).coerceAtLeast(0)
        val minute = 60L * 1000L
        val hour = 60L * minute
        val day = 24L * hour
        return when {
            age < hour -> "${(age / minute).coerceAtLeast(1)}m ago"
            age < day -> "${age / hour}h ago"
            age < day * 2 -> "Yesterday"
            else -> "${age / day}d ago"
        }
    }
