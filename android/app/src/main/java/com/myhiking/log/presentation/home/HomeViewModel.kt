package com.myhiking.log.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myhiking.log.domain.model.SummitRecords
import com.myhiking.log.domain.usecase.ObserveSummitRecordsUseCase
import com.myhiking.log.domain.usecase.SyncSummitRecordsUseCase
import com.myhiking.log.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeSummitRecords: ObserveSummitRecordsUseCase,
    private val syncSummitRecords: SyncSummitRecordsUseCase
) : ViewModel() {

    val records: StateFlow<SummitRecords> = observeSummitRecords()

    fun retry() {
        viewModelScope.launch {
            runCatching { syncSummitRecords(signedIn = true) }
                .onFailure { AppLogger.w("VM_HOME", "Summit retry failed: ${it.message}") }
        }
    }
}
