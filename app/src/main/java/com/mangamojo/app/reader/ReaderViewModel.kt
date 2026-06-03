package com.mangamojo.app.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.core.AppError
import com.mangamojo.app.core.toAppError
import com.mangamojo.app.domain.model.AppSettings
import com.mangamojo.app.domain.model.Chapter
import com.mangamojo.app.domain.model.MangaDetails
import com.mangamojo.app.domain.model.Page
import com.mangamojo.app.domain.usecase.GetChapterPagesUseCase
import com.mangamojo.app.domain.usecase.GetChapterProgressUseCase
import com.mangamojo.app.domain.usecase.GetChaptersUseCase
import com.mangamojo.app.domain.usecase.GetMangaDetailsUseCase
import com.mangamojo.app.domain.usecase.ObserveSettingsUseCase
import com.mangamojo.app.domain.usecase.SaveReadingProgressUseCase
import com.mangamojo.app.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReaderUiState(
    val loading: Boolean = true,
    val error: AppError? = null,
    val mangaTitle: String = "",
    val chapterId: String = "",
    val chapterLabel: String = "",
    val pages: List<Page> = emptyList(),
    val initialPage: Int = 0,
    val isExternal: Boolean = false,
    val externalUrl: String? = null,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
)

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMangaDetails: GetMangaDetailsUseCase,
    private val getChapters: GetChaptersUseCase,
    private val getChapterPages: GetChapterPagesUseCase,
    private val saveProgress: SaveReadingProgressUseCase,
    private val getChapterProgress: GetChapterProgressUseCase,
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    private val mangaId: String = checkNotNull(savedStateHandle[Routes.ARG_MANGA_ID])
    private val initialChapterId: String = checkNotNull(savedStateHandle[Routes.ARG_CHAPTER_ID])

    private val settings: StateFlow<AppSettings> =
        observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _state = MutableStateFlow(ReaderUiState())
    val state: StateFlow<ReaderUiState> = _state.asStateFlow()

    private var details: MangaDetails? = null
    private var chapters: List<Chapter> = emptyList()
    // Chapter feed is ordered newest-first, so a lower index is a *later*
    // chapter in story order. "Next" therefore decrements the index.
    private var currentIndex: Int = -1

    init {
        start()
    }

    fun retry() = start()

    private fun start() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                details = getMangaDetails(mangaId)
                val language = settings.value.translatedLanguage
                chapters = getChapters(mangaId, listOf(language))
                currentIndex = chapters.indexOfFirst { it.id == initialChapterId }.coerceAtLeast(0)
                loadCurrentChapter()
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.toAppError()) }
            }
        }
    }

    fun goToNextChapter() {
        if (currentIndex > 0) {
            currentIndex--
            viewModelScope.launch { loadCurrentChapter() }
        }
    }

    fun goToPreviousChapter() {
        if (currentIndex < chapters.lastIndex) {
            currentIndex++
            viewModelScope.launch { loadCurrentChapter() }
        }
    }

    private suspend fun loadCurrentChapter() {
        val chapter = chapters.getOrNull(currentIndex)
        if (chapter == null) {
            _state.update { it.copy(loading = false, error = AppError.NotFound) }
            return
        }
        _state.update { it.copy(loading = true, error = null) }

        if (chapter.isExternal) {
            _state.update {
                it.copy(
                    loading = false,
                    isExternal = true,
                    externalUrl = chapter.externalUrl,
                    mangaTitle = details?.title.orEmpty(),
                    chapterId = chapter.id,
                    chapterLabel = chapter.label,
                    pages = emptyList(),
                    hasPrevious = hasPrevious(),
                    hasNext = hasNext(),
                )
            }
            return
        }

        try {
            val pages = getChapterPages(chapter.id, settings.value.dataSaver)
            val resumePage = getChapterProgress(chapter.id)?.page ?: 0
            _state.update {
                it.copy(
                    loading = false,
                    error = null,
                    isExternal = false,
                    externalUrl = null,
                    mangaTitle = details?.title.orEmpty(),
                    chapterId = chapter.id,
                    chapterLabel = chapter.label,
                    pages = pages,
                    initialPage = resumePage.coerceIn(0, (pages.size - 1).coerceAtLeast(0)),
                    hasPrevious = hasPrevious(),
                    hasNext = hasNext(),
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = e.toAppError()) }
        }
    }

    /** Persist the furthest-read page; called as the reader scrolls. */
    fun onPageChanged(page: Int) {
        val manga = details ?: return
        val chapter = chapters.getOrNull(currentIndex) ?: return
        val total = _state.value.pages.size
        if (total == 0) return
        viewModelScope.launch {
            saveProgress(manga, chapter, page.coerceIn(0, total - 1), total)
        }
    }

    private fun hasNext(): Boolean = currentIndex > 0
    private fun hasPrevious(): Boolean = currentIndex < chapters.lastIndex
}
