package com.mangamojo.app.domain.usecase

import com.mangamojo.app.core.CachePolicy
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.domain.model.HistoryEntry
import com.mangamojo.app.domain.model.MangaDetails
import com.mangamojo.app.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveFavoritesUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(): Flow<List<Favorite>> = repo.observeFavorites()
}

class ObserveIsFavoriteUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(mangaId: String): Flow<Boolean> = repo.observeIsFavorite(mangaId)
}

class ToggleFavoriteUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke(manga: MangaDetails) = repo.toggleFavorite(manga)
}

class ClearFavoritesUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke() = repo.clearFavorites()
}

class ObserveHistoryUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(limit: Int = CachePolicy.HISTORY_LIMIT): Flow<List<HistoryEntry>> =
        repo.observeHistory(limit)
}

class ObserveContinueReadingUseCase @Inject constructor(private val repo: LibraryRepository) {
    operator fun invoke(limit: Int = CachePolicy.CONTINUE_READING_LIMIT): Flow<List<HistoryEntry>> =
        repo.observeContinueReading(limit)
}

class RemoveFromHistoryUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke(mangaId: String) = repo.removeFromHistory(mangaId)
}

class ClearHistoryUseCase @Inject constructor(private val repo: LibraryRepository) {
    suspend operator fun invoke() = repo.clearHistory()
}
