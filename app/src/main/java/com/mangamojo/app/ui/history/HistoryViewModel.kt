package com.mangamojo.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.domain.model.HistoryEntry
import com.mangamojo.app.domain.usecase.ClearHistoryUseCase
import com.mangamojo.app.domain.usecase.ObserveHistoryUseCase
import com.mangamojo.app.domain.usecase.RemoveFromHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeHistory: ObserveHistoryUseCase,
    private val removeFromHistory: RemoveFromHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase,
) : ViewModel() {

    val history: StateFlow<List<HistoryEntry>> =
        observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun remove(mangaId: String) {
        viewModelScope.launch { removeFromHistory(mangaId) }
    }

    fun clearAll() {
        viewModelScope.launch { clearHistory() }
    }
}
