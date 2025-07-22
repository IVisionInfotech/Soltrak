package com.soltrak.ui.screen.barcodeScanScreen

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cipherlab.barcode.GeneralString
import com.cipherlab.rfid.GeneralString as RFIDString
import com.soltrak.data.database.ModuleEntity
import com.soltrak.data.database.UidHistoryEntity
import com.soltrak.data.repository.ModuleRepository
import com.soltrak.data.repository.UidHistoryRepository
import com.soltrak.utils.PreferencesManager
import com.soltrak.utils.RFIDHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class BarcodeScanViewModel @Inject constructor(
    application: Application,
    private val moduleRepository: ModuleRepository,
    private val prefsManager: PreferencesManager,
    private val uidHistoryRepository: UidHistoryRepository,
    private val rfidHelper: RFIDHelper
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private var receiver: BroadcastReceiver? = null

    val settingE = prefsManager.settingE
    val settingD = prefsManager.settingD

    private val _barcodeValue = MutableStateFlow("")
    val barcodeValue: StateFlow<String> = _barcodeValue

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isBarcodeValid = MutableStateFlow<Boolean?>(null)
    val isBarcodeValid: StateFlow<Boolean?> = _isBarcodeValid

    private val _epcValue = MutableStateFlow<String?>(null)
    val epcValue: StateFlow<String?> = _epcValue

    private val _tidValue = MutableStateFlow<String?>(null)
    val tidValue: StateFlow<String?> = _tidValue

    private val _uniqueEpcCount = MutableStateFlow<String?>(null)
    val uniqueEpcCount: StateFlow<String?> = _uniqueEpcCount

    private val _currentModuleEntity = MutableStateFlow<ModuleEntity?>(null)
    val currentModuleEntity: StateFlow<ModuleEntity?> = _currentModuleEntity

    private val _showMismatchDialog = MutableStateFlow(false)
    val showMismatchDialog: StateFlow<Boolean> = _showMismatchDialog

    private val _isReportLoading = MutableStateFlow(false)
    val isReportLoading: StateFlow<Boolean> = _isReportLoading

    val rfidScanInProgress = MutableStateFlow(false)

    private var scanCounter = 0
    private val scannedEpcs = mutableListOf<String>()

    init {
        if (rfidHelper.isInitialized()) {
            prefsManager.powerLevel = rfidHelper.txPower
        }
    }

    fun registerReceiver() {
        if (receiver != null) return
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                when (intent.action) {
                    GeneralString.Intent_SOFTTRIGGER_DATA,
                    GeneralString.Intent_PASS_TO_APP -> {
                        intent.getStringExtra(GeneralString.BcReaderData)?.let {
                            _barcodeValue.value = extractBarcodeData(it)
                            _isBarcodeValid.value = null
                            checkDatabase()
                        }
                    }

                    RFIDString.Intent_RFIDSERVICE_TAG_DATA -> {
                        val epc = intent.getStringExtra(RFIDString.EXTRA_EPC)
                        val tid = intent.getStringExtra(RFIDString.EXTRA_TID)
                        val response = intent.getIntExtra(RFIDString.EXTRA_RESPONSE, -1)

                        if (response == 0 && epc != null && tid != null) {
                            scanCounter++
                            scannedEpcs.add(epc)
                            _uniqueEpcCount.value = scannedEpcs.toSet().size.toString()

                            if (scanCounter == settingE) {
                                val epcCountMap = scannedEpcs.groupingBy { it }.eachCount()
                                val mostFrequent = epcCountMap.maxByOrNull { it.value }

                                if (mostFrequent != null && mostFrequent.value >= settingD) {
                                    _epcValue.value = mostFrequent.key
                                    _tidValue.value = tid
                                } else {
                                    _epcValue.value = ""
                                    _tidValue.value = ""
                                    _showMismatchDialog.value = true
                                }

                                stopRfidScan()
                                scanCounter = 0
                                scannedEpcs.clear()
                            }
                        } else if (response == 6) {
                            Toast.makeText(context, "Password error", Toast.LENGTH_SHORT).show()
                            stopRfidScan()
                        } else if (response != 1) {
                            Toast.makeText(context, "Scan failed ($response)", Toast.LENGTH_SHORT)
                                .show()
                            stopRfidScan()
                        }
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter().apply {
                addAction(GeneralString.Intent_SOFTTRIGGER_DATA)
                addAction(GeneralString.Intent_PASS_TO_APP)
                addAction(RFIDString.Intent_RFIDSERVICE_TAG_DATA)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun unregisterReceiver() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
        }
    }

    fun onBarcodeChange(newValue: String) {
        _barcodeValue.value = newValue
    }

    fun checkDatabase() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(300)
            _isBarcodeValid.value = checkBarcodeInDatabase(_barcodeValue.value)
            _isLoading.value = false
        }
    }

    private suspend fun checkBarcodeInDatabase(barcode: String): Boolean {
        val module = moduleRepository.getModuleBySerialNo(barcode)
        _currentModuleEntity.value = module
        return module != null
    }

    fun onDismissMismatchDialog() {
        _showMismatchDialog.value = false
    }

    fun onRescan() {
        _barcodeValue.value = ""
        _isBarcodeValid.value = null
        _epcValue.value = null
        _tidValue.value = null
        rfidScanInProgress.value = false
    }

    fun startRfidScan(onTimeout: () -> Unit) {
        rfidScanInProgress.value = true
        scannedEpcs.clear()
        scanCounter = 0
        rfidHelper.softScanTrigger(true)

        val timeoutSeconds = prefsManager.scanTimeout
        if (timeoutSeconds > 0) {
            viewModelScope.launch {
                delay(timeoutSeconds * 1000L)
                if (rfidScanInProgress.value) {
                    onTimeout()
                }
            }
        }
    }

    fun autoStopAndProcess(onNavigateToReport: (ModuleEntity) -> Unit) {
        stopRfidScan()
        getReportData(onNavigateToReport)
    }

    fun stopRfidScan() {
        rfidScanInProgress.value = false
        rfidHelper.softScanTrigger(false)
    }

    fun getReportData(onNavigateToReport: (ModuleEntity) -> Unit) {
        viewModelScope.launch {
            _isReportLoading.value = true
            val module = _currentModuleEntity.value
            val uid = _tidValue.value

            if (module != null && uid != null) {
                if (module.uid == uid) {
                    delay(300)
                    onNavigateToReport(module)
                    onRescan()
                } else {
                    val updatedModule = moduleRepository.updateUidAndGetModule(module.id, uid)
                    if (updatedModule != null) {
                        uidHistoryRepository.insertUidHistory(
                            UidHistoryEntity(
                                id = updatedModule.id,
                                uid = uid,
                                timestamp = SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss",
                                    Locale.getDefault()
                                ).format(Date())
                            )
                        )
                        delay(300)
                        onNavigateToReport(updatedModule)
                        onRescan()
                    }
                }
            } else if (module != null) {
                delay(300)
                onNavigateToReport(module)
                onRescan()
            }

            _isReportLoading.value = false
        }
    }

    private fun extractBarcodeData(rawInput: String): String {
        val parts = rawInput.split(":")
        return if (parts.size >= 3) parts.drop(1).dropLast(1).joinToString(":").trim()
        else if (parts.size == 2) parts[1].trim()
        else rawInput.trim()
    }
}
