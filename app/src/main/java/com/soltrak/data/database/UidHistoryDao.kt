package com.soltrak.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UidHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUidHistory(history: UidHistoryEntity)

    @Query("SELECT * FROM uid_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<UidHistoryEntity>

    @Query("DELETE FROM uid_history")
    suspend fun deleteAllHistory()
}
