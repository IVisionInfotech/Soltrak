package com.soltrak.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "uid_history",
    indices = [Index(value = ["uid"], unique = true)]
)
data class UidHistoryEntity(
    @PrimaryKey
    val id: String,
    val uid: String,
    val timestamp: String
)
