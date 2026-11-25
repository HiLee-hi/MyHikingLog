package com.myhiking.log.di

import com.myhiking.log.data.repository.AuthRepositoryImpl
import com.myhiking.log.data.repository.MountainRepositoryImpl
import com.myhiking.log.data.repository.SummitRecordRepositoryImpl
import com.myhiking.log.domain.repository.AuthRepository
import com.myhiking.log.domain.repository.MountainRepository
import com.myhiking.log.domain.repository.SummitRecordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMountainRepository(impl: MountainRepositoryImpl): MountainRepository

    @Binds
    @Singleton
    abstract fun bindSummitRecordRepository(impl: SummitRecordRepositoryImpl): SummitRecordRepository
}
