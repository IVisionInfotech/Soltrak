package com.soltrak.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ModuleEntity::class, UidHistoryEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moduleDao(): ModuleDao
    abstract fun uidHistoryDao(): UidHistoryDao
}
