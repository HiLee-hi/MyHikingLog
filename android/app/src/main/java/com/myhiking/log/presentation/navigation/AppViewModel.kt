package com.myhiking.log.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myhiking.log.domain.usecase.ObserveCurrentUserUseCase
import com.myhiking.log.domain.usecase.SyncSummitRecordsUseCase
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// session state + summit records follow the signed-in user
@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val syncSummitRecords: SyncSummitRecordsUseCase
) : ViewModel() {

    private val userId = observeCurrentUserUseCase().map { it?.id }.distinctUntilChanged()

    val isLoggedIn: StateFlow<Boolean> = userId
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        // account switch -> collectLatest drops the old user's load
        viewModelScope.launch {
            userId.collectLatest { id ->
                runCatching { syncSummitRecords(signedIn = id != null) }
                    // status is FAILED in the repository, home shows retry
                    .onFailure { AppLogger.w("VM_APP", "Summit sync failed: ${it.message}") }
            }
        }
    }
}
