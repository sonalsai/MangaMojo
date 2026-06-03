package com.mangamojo.app.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mangamojo.app.domain.repository.LibraryRepository
import com.mangamojo.app.domain.repository.MangaRepository
import com.mangamojo.app.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Refreshes cached details + chapter lists for the user's favorites so newly
 * released chapters are ready (and available offline) next time they open the
 * app. Best-effort: a failure on one title doesn't abort the others.
 */
@HiltWorker
class LibraryRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val mangaRepository: MangaRepository,
    private val libraryRepository: LibraryRepository,
    private val settingsRepository: SettingsRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = try {
        val language = settingsRepository.settings.first().translatedLanguage
        val favorites = libraryRepository.observeFavorites().first()
        for (favorite in favorites) {
            runCatching {
                mangaRepository.getMangaDetails(favorite.mangaId, forceRefresh = true)
                mangaRepository.getChapters(favorite.mangaId, listOf(language), forceRefresh = true)
            }
        }
        Result.success()
    } catch (e: Exception) {
        Result.retry()
    }
}
