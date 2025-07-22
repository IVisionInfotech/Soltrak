package com.soltrak.data.repository

import com.soltrak.data.database.UidHistoryDao
import com.soltrak.data.database.UidHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UidHistoryRepository @Inject constructor(
    private val uidHistoryDao: UidHistoryDao
) {

    suspend fun insertUidHistory(history: UidHistoryEntity) {
        uidHistoryDao.insertUidHistory(history)
    }

    suspend fun getAllHistory(): List<UidHistoryEntity> {
        return uidHistoryDao.getAllHistory()
    }

    suspend fun deleteAll() {
        uidHistoryDao.deleteAllHistory()
    }
}
