package com.mangamojo.app.domain.model

/** Furthest-read position within a single chapter. */
data class ReadingProgress(
    val mangaId: String,
    val chapterId: String,
    val page: Int,
    val total: Int,
    val completed: Boolean,
    val updatedAt: Long,
)
