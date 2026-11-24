package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.model.UserProfile
import com.myhiking.log.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveCurrentUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<UserProfile?> =
        repository.currentUser
}
