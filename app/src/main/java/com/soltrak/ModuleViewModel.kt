package com.soltrak

import android.os.Environment
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken

import com.google.gson.stream.JsonReader
import com.soltrak.data.database.ModuleEntity
import com.soltrak.data.models.IVPoint
import com.soltrak.data.models.ModuleData
import com.soltrak.data.repository.ModuleRepository
import com.soltrak.data.repository.UidHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

@HiltViewModel
class ModuleViewModel @Inject constructor(
    private val repository: ModuleRepository,
    private val uidHistoryRepository: UidHistoryRepository
) : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    val isLoading = mutableStateOf(false)
    val progressPercentage = mutableStateOf(0)
    val importStatusText = mutableStateOf("")


    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
        uidHistoryRepository.deleteAll()
    }

    suspend fun importModulesFromJson(inputStream: InputStream) {
        try {
            val gson = Gson()
            inputStream.use { stream ->
                InputStreamReader(stream).use { readerStream ->
                    val reader = JsonReader(readerStream)
                    reader.isLenient = true

                    val modulesToInsert = mutableListOf<ModuleEntity>()

                    reader.beginObject()
                    while (reader.hasNext()) {
                        val name = reader.nextName()
                        if (name.startsWith("Data")) {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                val moduleJson: JsonObject = gson.fromJson(reader, object : TypeToken<JsonObject>() {}.type)
                                val moduleData = gson.fromJson(moduleJson, ModuleData::class.java)

                                val ivPoints = mutableListOf<IVPoint>()
                                var maxIVIndex = 0

                                moduleJson.entrySet().forEach { entry ->
                                    val match = Regex("^[IV](\\d+)$").find(entry.key)
                                    match?.groupValues?.get(1)?.toIntOrNull()?.let { index ->
                                        if (index > maxIVIndex) maxIVIndex = index
                                    }
                                }


                                for (i in 1..maxIVIndex) {
                                    val iVal = moduleJson["I%02d".format(i)]?.asString.orEmpty()
                                    val vVal = moduleJson["V%02d".format(i)]?.asString.orEmpty()
                                    if (iVal.isNotEmpty() && vVal.isNotEmpty()) {
                                        ivPoints.add(IVPoint(i, iVal, vVal))
                                    }
                                }

                                val moduleEntity = ModuleEntity(
                                    id = moduleData.id ?: "",
                                    serialNo = moduleData.serialNo,
                                    moduleNo = moduleData.moduleNo,
                                    modType = moduleData.modType,
                                    pvMfgr = moduleData.pvMfgr,
                                    cellMfgr = moduleData.cellMfgr,
                                    iecLab = moduleData.iecLab,
                                    pvMfgMonYear = moduleData.pvMfgMonYear,
                                    cellMfgMonYear = moduleData.cellMfgMonYear,
                                    voc = moduleData.voc,
                                    isc = moduleData.isc,
                                    vMax = moduleData.vMax,
                                    iMax = moduleData.iMax,
                                    pMax = moduleData.pMax,
                                    ff = moduleData.ff,
                                    capVoltage = moduleData.capVoltage,
                                    insol = moduleData.insol,
                                    tempTest = moduleData.tempTest,
                                    tempCorr = moduleData.tempCorr,
                                    cellEff = moduleData.cellEff,
                                    moduleEff = moduleData.moduleEff,
                                    rSeries = moduleData.rSeries,
                                    rShunt = moduleData.rShunt,
                                    status = moduleData.status,
                                    createdTS = moduleData.createdTS,
                                    importFlag = moduleData.importFlag,
                                    createdTSWT = moduleData.createdTSWT,
                                    uid = moduleData.uid,
                                    ivPointsJson = gson.toJson(ivPoints)
                                )

                                modulesToInsert.add(moduleEntity)
                            }
                            reader.endArray()
                        } else {
                            reader.skipValue()
                        }
                    }
                    reader.endObject()

                    if (modulesToInsert.isNotEmpty()) {
                        repository.insertAll(modulesToInsert)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                _toastMessage.value = "Import failed: ${e.message}"
            }
        }
    }

    suspend fun isLocalDbEmpty(): Boolean {
        return repository.getLocalRowCount() == 0
    }


    fun exportUidHistoryToZip(
        onExportComplete: (String) -> Unit,
        onExportFailed: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uidHistoryList = uidHistoryRepository.getAllHistory()

                if (uidHistoryList.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onExportFailed()
                    }
                    return@launch
                }


                val gson = Gson()
                val uidArray = JsonArray().apply {
                    uidHistoryList.forEach {
                        add(JsonObject().apply {
                            addProperty("id", it.id)
                            addProperty("uid", it.uid)
                            addProperty("timestamp", it.timestamp)
                        })
                    }
                }

                val exportDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "Soltrak"
                )

                val jsonFile = getUniqueFile(
                    folder = exportDir,
                    baseName = "DatabaseFromHHD_${getCurrentTimestamp()}",
                    extension = "dat"
                ).apply {
                    parentFile?.mkdirs()
                    writeText(gson.toJson(JsonObject().apply { add("Data", uidArray) }))
                }

                val zipFile = getUniqueFile(exportDir, jsonFile.nameWithoutExtension, "zip")
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    zos.putNextEntry(ZipEntry(jsonFile.name))
                    FileInputStream(jsonFile).copyTo(zos)
                    zos.closeEntry()
                }

                jsonFile.delete()

                withContext(Dispatchers.Main) {
                    onExportComplete(zipFile.absolutePath)
                }

            } catch (e: Exception) {
                Log.e("EXPORT_ZIP", "Export failed", e)
                withContext(Dispatchers.Main) {
                    onExportFailed()
                }
            }
        }
    }

    private fun getUniqueFile(folder: File, baseName: String, extension: String): File {
        var file = File(folder, "$baseName.$extension")
        var i = 1
        while (file.exists()) {
            file = File(folder, "$baseName($i).$extension")
            i++
        }
        return file
    }

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault()).format(Date())
    }
}
