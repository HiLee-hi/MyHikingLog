package com.myhiking.log.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myhiking.log.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// session state. TODO step 3: sync records on login
@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = observeCurrentUserUseCase()
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
