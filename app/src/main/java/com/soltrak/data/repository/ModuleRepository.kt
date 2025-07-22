package com.soltrak.data.repository

import com.soltrak.data.database.ModuleDao
import com.soltrak.data.database.ModuleEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModuleRepository @Inject constructor(private val moduleDao: ModuleDao) {

    val allModules: Flow<List<ModuleEntity>> = moduleDao.getAllModules()

    suspend fun insert(module: ModuleEntity) {
        moduleDao.insertModule(module)
    }

    suspend fun insertAll(modules: List<ModuleEntity>) {
        moduleDao.insertAllModules(modules)
    }

    suspend fun update(module: ModuleEntity) {
        moduleDao.updateModule(module)
    }

    suspend fun updateUidAndGetModule(id: String, uid: String): ModuleEntity? {
        moduleDao.updateUidById(id, uid)
        return moduleDao.getModuleById(id)
    }

    suspend fun delete(module: ModuleEntity) {
        moduleDao.deleteModule(module)
    }

    suspend fun getModuleById(id: String): ModuleEntity? {
        return moduleDao.getModuleById(id)
    }

    suspend fun deleteAll() {
        moduleDao.deleteAllModules()
    }

    suspend fun getModuleBySerialNo(barcode: String): ModuleEntity? {
        return moduleDao.getModuleBySerialNo(barcode)
    }

    suspend fun getModuleBySerialNoAndUID(barcode: String, uid: String): ModuleEntity? {
        return moduleDao.getModuleBySerialNoAndUid(barcode, uid)
    }

    suspend fun getLocalRowCount(): Int {
        return moduleDao.getRowCount()
    }
}