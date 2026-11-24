package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() =
        repository.signInWithGoogle()
}
