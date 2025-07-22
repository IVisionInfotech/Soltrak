package com.soltrak.ui.screen.rfid.findTag

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FindTagViewModel @Inject constructor(
    application: Application,
    private val prefsManager: PreferencesManager,
    private val rfidHelper: RFIDHelper,
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val _tagId = MutableStateFlow("")
    val tagId: StateFlow<String> = _tagId

    val yellowThreshold = prefsManager.rssiThresholdYellow
    val greenThreshold = prefsManager.rssiThresholdGreen

    private val _mode = MutableStateFlow("EPC")
    val mode: StateFlow<String> = _mode

    private val _isFinding = MutableStateFlow(false)
    val isFinding: StateFlow<Boolean> = _isFinding

    private val _rssi = MutableStateFlow(-0f)
    val rssi: StateFlow<Float> = _rssi

    private val _toastMessages = Channel<String>()
    val toastMessages = _toastMessages.receiveAsFlow()

    private var receiver: BroadcastReceiver? = null

    init {
        if (rfidHelper.isInitialized()) {
            prefsManager.powerLevel = rfidHelper.txPower
        }
    }


    fun registerReceiver() {
        if (receiver != null) return

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action ?: return

                when (action) {
                    GeneralString.Intent_RFIDSERVICE_CONNECTED -> {
                        Log.d("FindTagVM", "RFID connected")
                        rfidHelper.setScanMode(ScanMode.Continuous)
                        rfidHelper.setRFIDMode(RFIDMode.Inventory_EPC_TID)
                        viewModelScope.launch {
                            delay(500)
                            if (rfidHelper.configureRfid()) {
                                Log.d("FindTagVM", "RFID configuration successful")
                            } else {
                                Log.e("FindTagVM", "RFID configuration failed")
                            }
                        }
                    }

                    GeneralString.Intent_RFIDSERVICE_TAG_DATA -> {
                        val epc = intent.getStringExtra(GeneralString.EXTRA_EPC)
                        val tid = intent.getStringExtra(GeneralString.EXTRA_TID)
                        val signal = intent.getDoubleExtra(GeneralString.EXTRA_DATA_RSSI, -100.0).toFloat()

                        if (_isFinding.value) {
                            val match = when (_mode.value) {
                                "EPC" -> epc == _tagId.value
                                else -> tid == _tagId.value
                            }
                            if (match) {
                                _rssi.value = signal + 8
                            }
                        } else {
                            if (_mode.value == "EPC" && !epc.isNullOrEmpty()) {
                                _tagId.value = epc
                            } else if (_mode.value == "TID" && !tid.isNullOrEmpty()) {
                                _tagId.value = tid
                            }
                        }
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
        Log.d("FindTagVM", "Receiver registered FindTagVM")
    }

    fun unregisterReceiver() {
        try {
            receiver?.let {
                context.unregisterReceiver(it)
                receiver = null
                Log.d("FindTagVM", "Receiver unregistered")
            }
        } catch (e: Exception) {
            Log.e("FindTagVM", "Receiver unregister error: ${e.message}")
        }
    }

    fun onTagIdChanged(value: String) {
        _tagId.value = value
    }

    fun setMode(mode: String) {
        _mode.value = mode
    }

    fun startFinding() {
        viewModelScope.launch {
            if (_tagId.value.isEmpty()) {
                _toastMessages.send("Tag ID is empty")
                return@launch
            }
            if (!RFIDHelper.isValidHexString(_tagId.value)) {
                _toastMessages.send("Invalid Hex format!")
                return@launch
            }

            if (_mode.value == "EPC") {
                if (_tagId.value.length % 4 != 0) {
                    _toastMessages.send("EPC should be multiple of 4!")
                    return@launch
                }
                if (rfidHelper.findEPCTag(_tagId.value)) {
                    _isFinding.value = true
                    _toastMessages.send("Started finding EPC")
                } else {
                    _toastMessages.send("Failed to start EPC")
                }
            } else {
                if (_tagId.value.length < 8) {
                    _toastMessages.send("TID too short")
                    return@launch
                }
                if (rfidHelper.findTIDTag(_tagId.value)) {
                    _isFinding.value = true
                    _toastMessages.send("Started finding TID")
                } else {
                    _toastMessages.send("Failed to start TID")
                }
            }
        }
    }

    fun stopFinding() {
        viewModelScope.launch {
            if (rfidHelper.stopFindingTag()) {
                _isFinding.value = false
                _toastMessages.send("Stopped")
            } else {
                _toastMessages.send("Failed to stop")
            }
        }
    }

    override fun onCleared() {
        try {
            unregisterReceiver()
        } catch (e: Exception) {
            Log.e("FindTagVM", "onCleared: ${e.message}")
        }
        super.onCleared()
    }

}
