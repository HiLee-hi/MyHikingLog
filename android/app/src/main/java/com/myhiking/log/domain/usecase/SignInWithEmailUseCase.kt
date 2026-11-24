package com.myhiking.log.domain.usecase

import com.myhiking.log.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithEmailUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String) =
        repository.signInWithEmail(email, password)
}
