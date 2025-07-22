package com.soltrak.di

import com.soltrak.data.database.ModuleDao
import com.soltrak.data.database.UidHistoryDao
import com.soltrak.data.repository.ModuleRepository
import com.soltrak.data.repository.UidHistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideModuleRepository(moduleDao: ModuleDao): ModuleRepository {
        return ModuleRepository(moduleDao)
    }

    @Singleton
    @Provides
    fun provideUidHistoryRepository(uidHistoryDao: UidHistoryDao): UidHistoryRepository {
        return UidHistoryRepository(uidHistoryDao)
    }
}