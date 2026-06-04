package com.mangamojo.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mangamojo.app.domain.model.AppSettings
import com.mangamojo.app.domain.usecase.ObserveSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Exposes app settings so the activity can theme the whole app. */
@HiltViewModel
class MainViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {
    val settings: StateFlow<AppSettings> =
        observeSettings().stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
}
