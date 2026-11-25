package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.repository.SummitRecordRepository
import javax.inject.Inject

// signed in -> reload, signed out -> drop the session copy
class SyncSummitRecordsUseCase @Inject constructor(
    private val repository: SummitRecordRepository
) {
    suspend operator fun invoke(signedIn: Boolean) {
        if (signedIn) repository.refresh() else repository.clear()
    }
}
