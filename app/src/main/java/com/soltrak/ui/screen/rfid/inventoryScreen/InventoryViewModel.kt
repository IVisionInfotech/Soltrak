package com.soltrak.ui.screen.rfid.inventoryScreen

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import kotlinx.coroutines.delay
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.RFIDMode
import com.cipherlab.rfid.ScanMode
import com.soltrak.utils.PreferencesManager
import com.soltrak.utils.RFIDHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    application: Application,
    private val prefsManager: PreferencesManager,
    private val rfidHelper: RFIDHelper
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    private val _tagList = MutableStateFlow<List<RfidTag>>(emptyList())
    val tagList: StateFlow<List<RfidTag>> = _tagList

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    private var receiver: BroadcastReceiver? = null

    init {
        if (rfidHelper.isInitialized()) {
            prefsManager.powerLevel = rfidHelper.txPower
        }
    }

    fun toggleScan() {
        _isScanning.value = !_isScanning.value
        if (_isScanning.value) {
            rfidHelper.softScanTrigger(true)
        } else {
            rfidHelper.softScanTrigger(false)
        }
    }

    fun registerReceiver() {
        if (receiver != null) return

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    GeneralString.Intent_RFIDSERVICE_CONNECTED -> {
                        rfidHelper.setScanMode(ScanMode.Continuous)
                        rfidHelper.setRFIDMode(RFIDMode.Inventory_EPC_TID)
                        viewModelScope.launch {
                            delay(500)
                            if (rfidHelper.configureRfid()) {
                                Log.d("InventoryVM", "RFID configuration successful")
                            } else {
                                Log.e("InventoryVM", "RFID configuration failed")
                            }
                        }
                    }

                    GeneralString.Intent_RFIDSERVICE_TAG_DATA -> {
                        var epc = intent.getStringExtra(GeneralString.EXTRA_EPC)
                        var tid = intent.getStringExtra(GeneralString.EXTRA_TID)
                        Log.e("eq", "onReceive: $epc + $tid" )

                        epc = if (epc.isNullOrEmpty()) "" else epc
                        tid = if (tid.isNullOrEmpty()) "" else tid

//                        if (!epc.isNullOrEmpty() && !tid.isNullOrEmpty()) {
                            updateTagList(epc, tid)
//                        }
                    }
                }
            }
        }

        ContextCompat.registerReceiver(
            context,
            receiver!!,
            IntentFilter().apply {
                addAction(GeneralString.Intent_RFIDSERVICE_CONNECTED)
                addAction(GeneralString.Intent_RFIDSERVICE_TAG_DATA)
            },
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        Log.d("InventoryVM", "Receiver registered InventoryVM")
    }

    fun unregisterReceiver() {
        receiver?.let {
            context.unregisterReceiver(it)
            receiver = null
            Log.d("InventoryVM", "Receiver unregistered")
        }
    }

    private fun updateTagList(epc: String, tid: String) {
        val currentList = _tagList.value.toMutableList()
        val index = currentList.indexOfFirst { it.epc == epc }

        if (index >= 0) {
            val updatedTag = currentList[index].copy(count = currentList[index].count + 1)
            currentList[index] = updatedTag
        } else {
            currentList.add(RfidTag(epc = epc, tid = tid, count = 1))
        }

        _tagList.value = currentList
    }

    fun resetTagList() {
        _tagList.value = emptyList()
    }

    override fun onCleared() {
        unregisterReceiver()
        super.onCleared()
    }
}
