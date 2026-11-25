package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.SummitRecords
import com.myhiking.log.domain.repository.SummitRecordRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ObserveSummitRecordsUseCase @Inject constructor(
    private val repository: SummitRecordRepository
) {
    operator fun invoke(): StateFlow<SummitRecords> = repository.records
}
