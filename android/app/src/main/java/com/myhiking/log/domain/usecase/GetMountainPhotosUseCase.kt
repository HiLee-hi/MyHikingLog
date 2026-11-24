package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.Mountain
import com.myhiking.log.domain.repository.MountainRepository
import javax.inject.Inject

class GetMountainPhotosUseCase @Inject constructor(
    private val repository: MountainRepository
) {
    suspend operator fun invoke(mountain: Mountain): List<String> = repository.photoUrls(mountain)
}
