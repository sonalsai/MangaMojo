package com.mangamojo.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.core.AppError
import com.mangamojo.app.core.MangaDex
import com.mangamojo.app.core.toAppError
import com.mangamojo.app.domain.model.AppSettings
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.domain.model.Manga
import com.mangamojo.app.domain.model.SearchQuery
import com.mangamojo.app.domain.model.SearchSort
import com.mangamojo.app.domain.usecase.ObserveFavoritesUseCase
import com.mangamojo.app.domain.usecase.ObserveSettingsUseCase
import com.mangamojo.app.domain.usecase.SearchMangaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Discovery feed tabs on the Home screen. */
enum class DiscoverTab(val label: String, val sort: SearchSort) {
    POPULAR("Popular", SearchSort.POPULAR),
    LATEST("Latest", SearchSort.LATEST),
}

/** Paginated state for the selected discovery tab. */
data class DiscoverState(
    val tab: DiscoverTab = DiscoverTab.POPULAR,
    val items: List<Manga> = emptyList(),
    val loading: Boolean = true,
    val loadingMore: Boolean = false,
    val error: AppError? = null,
    val hasMore: Boolean = false,
)

data class HomeUiState(
    val favorites: List<Favorite> = emptyList(),
    val discover: DiscoverState = DiscoverState(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val searchManga: SearchMangaUseCase,
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    private val settings: StateFlow<AppSettings> =
        observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val discover = MutableStateFlow(DiscoverState())

    val uiState: StateFlow<HomeUiState> = combine(
        observeFavorites(),
        discover,
    ) { favorites, discoverState ->
        HomeUiState(favorites, discoverState)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private var offset = 0
    private var loadJob: Job? = null

    init {
        loadDiscover(reset = true)
    }

    fun selectTab(tab: DiscoverTab) {
        if (discover.value.tab == tab) return
        discover.update { it.copy(tab = tab) }
        loadDiscover(reset = true)
    }

    fun retry() = loadDiscover(reset = true)

    fun loadMore() {
        val current = discover.value
        if (current.loading || current.loadingMore || !current.hasMore) return
        loadDiscover(reset = false)
    }

    private fun loadDiscover(reset: Boolean) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (reset) {
                offset = 0
                discover.update { it.copy(loading = true, error = null) }
            } else {
                discover.update { it.copy(loadingMore = true) }
            }
            try {
                val ratings = settings.value.contentRatings.toList()
                    .ifEmpty { MangaDex.DEFAULT_CONTENT_RATINGS }
                val result = searchManga(
                    SearchQuery(
                        title = null,
                        offset = offset,
                        sort = discover.value.tab.sort,
                        contentRatings = ratings,
                    )
                )
                offset = result.nextOffset
                discover.update {
                    it.copy(
                        items = if (reset) result.items else it.items + result.items,
                        loading = false,
                        loadingMore = false,
                        hasMore = result.hasMore,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                discover.update {
                    it.copy(loading = false, loadingMore = false, error = e.toAppError())
                }
            }
        }
    }
}
