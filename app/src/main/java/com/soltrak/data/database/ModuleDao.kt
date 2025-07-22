package com.soltrak.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface ModuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModule(module: ModuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllModules(modules: List<ModuleEntity>)

    @Update
    suspend fun updateModule(module: ModuleEntity)

    @Query("UPDATE modules SET uid = :uid WHERE id = :id")
    suspend fun updateUidById(id: String, uid: String)

    @Delete
    suspend fun deleteModule(module: ModuleEntity)

    @Query("SELECT * FROM modules")
    fun getAllModules(): Flow<List<ModuleEntity>>

    @Query("SELECT * FROM modules WHERE id = :id")
    suspend fun getModuleById(id: String): ModuleEntity?

    @Query("SELECT * FROM modules WHERE serialNo = :serialNo COLLATE NOCASE")
    suspend fun getModuleBySerialNo(serialNo: String): ModuleEntity?


    @Query("SELECT * FROM modules WHERE serialNo = :serialNo AND uid = :uid")
    suspend fun getModuleBySerialNoAndUid(serialNo: String,uid: String): ModuleEntity?

    @Query("DELETE FROM modules")
    suspend fun deleteAllModules()

    @Query("SELECT COUNT(*) FROM modules")
    suspend fun getRowCount(): Int

}