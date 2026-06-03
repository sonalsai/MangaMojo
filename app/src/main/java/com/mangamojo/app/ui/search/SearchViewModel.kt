package com.mangamojo.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.core.AppError
import com.mangamojo.app.core.MangaDex
import com.mangamojo.app.core.toAppError
import com.mangamojo.app.domain.model.AppSettings
import com.mangamojo.app.domain.model.Manga
import com.mangamojo.app.domain.model.SearchQuery
import com.mangamojo.app.domain.model.SearchSort
import com.mangamojo.app.domain.usecase.ObserveSettingsUseCase
import com.mangamojo.app.domain.usecase.SearchMangaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val items: List<Manga> = emptyList(),
    val loading: Boolean = false,
    val loadingMore: Boolean = false,
    val error: AppError? = null,
    val hasMore: Boolean = false,
    val hasSearched: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchManga: SearchMangaUseCase,
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    private val settings: StateFlow<AppSettings> =
        observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private var searchJob: Job? = null
    private var offset = 0

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.update { SearchUiState(query = query) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(350) // debounce keystrokes
            runSearch(reset = true)
        }
    }

    fun retry() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch(reset = true) }
    }

    fun loadMore() {
        val current = _state.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        searchJob = viewModelScope.launch { runSearch(reset = false) }
    }

    private suspend fun runSearch(reset: Boolean) {
        val query = _state.value.query.trim()
        if (query.isBlank()) return
        if (reset) {
            offset = 0
            _state.update { it.copy(loading = true, error = null, hasSearched = true) }
        } else {
            _state.update { it.copy(loadingMore = true) }
        }
        try {
            val ratings = settings.value.contentRatings.toList()
                .ifEmpty { MangaDex.DEFAULT_CONTENT_RATINGS }
            val result = searchManga(
                SearchQuery(
                    title = query,
                    offset = offset,
                    contentRatings = ratings,
                    sort = SearchSort.RELEVANCE,
                )
            )
            offset = result.nextOffset
            _state.update {
                it.copy(
                    items = if (reset) result.items else it.items + result.items,
                    loading = false,
                    loadingMore = false,
                    hasMore = result.hasMore,
                    error = null,
                )
            }
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, loadingMore = false, error = e.toAppError()) }
        }
    }
}
