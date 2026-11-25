package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.repository.MountainRepository
import javax.inject.Inject

class FindMountainUseCase @Inject constructor(
    private val repository: MountainRepository
) {
    suspend operator fun invoke(id: String): Mountain? = repository.find(id)
}
