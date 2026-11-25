package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.repository.SummitRecordRepository
import javax.inject.Inject

class DeleteSummitRecordUseCase @Inject constructor(
    private val repository: SummitRecordRepository
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
