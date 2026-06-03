package com.mangamojo.app.domain.model

/**
 * Lightweight, source-agnostic manga summary used in grids, rails and search
 * results. [sourceId] identifies the originating provider (always "mangadex"
 * in Phase 1) so the same model can hold results from multiple sources later.
 */
data class Manga(
    val id: String,
    val sourceId: String,
    val title: String,
    val coverUrl: String?,
    val status: MangaStatus,
    val contentRating: String,
    val year: Int?,
)
