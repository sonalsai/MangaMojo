package com.mangamojo.app.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.domain.model.Favorite
import com.mangamojo.app.domain.usecase.ClearFavoritesUseCase
import com.mangamojo.app.domain.usecase.ObserveFavoritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavorites: ObserveFavoritesUseCase,
    private val clearFavorites: ClearFavoritesUseCase,
) : ViewModel() {

    val favorites: StateFlow<List<Favorite>> =
        observeFavorites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clearAll() {
        viewModelScope.launch { clearFavorites() }
    }
}
