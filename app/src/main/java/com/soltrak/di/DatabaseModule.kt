package com.soltrak.di

import android.content.Context
import androidx.room.Room
import com.soltrak.data.database.AppDatabase
import com.soltrak.data.database.ModuleDao
import com.soltrak.data.database.UidHistoryDao
import com.soltrak.utils.RFIDHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "module_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun provideModuleDao(database: AppDatabase): ModuleDao {
        return database.moduleDao()
    }

    @Singleton
    @Provides
    fun provideUidHistoryDao(database: AppDatabase): UidHistoryDao {
        return database.uidHistoryDao()
    }

    @Singleton
    @Provides
    fun provideRfidHelper(@ApplicationContext context: Context): RFIDHelper {
        return RFIDHelper(context)
    }

}

