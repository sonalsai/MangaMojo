package com.mangamojo.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.core.UiState
import com.mangamojo.app.core.toAppError
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.domain.model.HistoryEntry
import com.mangamojo.app.domain.model.Manga
import com.mangamojo.app.domain.usecase.GetPopularMangaUseCase
import com.mangamojo.app.domain.usecase.ObserveContinueReadingUseCase
import com.mangamojo.app.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val continueReading: List<HistoryEntry> = emptyList(),
    val favorites: List<Favorite> = emptyList(),
    val popular: UiState<List<Manga>> = UiState.Loading,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeContinueReading: ObserveContinueReadingUseCase,
    observeFavorites: ObserveFavoritesUseCase,
    private val getPopular: GetPopularMangaUseCase,
) : ViewModel() {

    private val popular = MutableStateFlow<UiState<List<Manga>>>(UiState.Loading)

    val uiState: StateFlow<HomeUiState> = combine(
        observeContinueReading(),
        observeFavorites(),
        popular,
    ) { continueReading, favorites, popular ->
        HomeUiState(continueReading = continueReading, favorites = favorites, popular = popular)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        loadPopular()
    }

    fun loadPopular() {
        viewModelScope.launch {
            popular.value = UiState.Loading
            popular.value = try {
                UiState.Success(getPopular(offset = 0, limit = 24).items)
            } catch (e: Exception) {
                UiState.Error(e.toAppError())
            }
        }
    }
}
