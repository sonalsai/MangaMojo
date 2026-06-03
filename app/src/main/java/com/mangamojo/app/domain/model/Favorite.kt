package com.mangamojo.app.domain.model

/** A manga the user has saved to their library. */
data class Favorite(
    val mangaId: String,
    val sourceId: String,
    val title: String,
    val coverUrl: String?,
    val status: MangaStatus,
    val addedAt: Long,
)
