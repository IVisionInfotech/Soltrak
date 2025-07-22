package com.soltrak.ui.screen.rfid.writeTag

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cipherlab.rfid.DeviceResponse
import com.cipherlab.rfid.GeneralString
import com.cipherlab.rfid.RFIDMemoryBank
import com.cipherlab.rfid.RFIDMode
import com.cipherlab.rfid.ScanMode
import com.google.gson.Gson
import com.soltrak.utils.RFIDHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RfidViewModel @Inject constructor(
    application: Application,
    private val rfidHelper: RFIDHelper,
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private var receiver: BroadcastReceiver? = null

    private val _epcValue = MutableStateFlow<String?>(null)
    val epcValue: StateFlow<String?> = _epcValue

    private val _tidValue = MutableStateFlow<String?>(null)
    val tidValue: StateFlow<String?> = _tidValue

    val _passwordValue = MutableStateFlow<String?>("00000000")
    val passwordValue: StateFlow<String?> = _passwordValue

    val _writeDataValue = MutableStateFlow<String?>(null)
    val writeDataValue: StateFlow<String?> = _writeDataValue

    private val _isShowChooseBank = MutableStateFlow<Boolean?>(false)
    val showChooseBank: StateFlow<Boolean?> = _isShowChooseBank

    private val _isShowPasswordLayout = MutableStateFlow<Boolean?>(false)
    val showPasswordLayout: StateFlow<Boolean?> = _isShowPasswordLayout

    private val _isShowWriteLayout = MutableStateFlow<Boolean?>(false)
    val showWriteLayout: StateFlow<Boolean?> = _isShowWriteLayout

    val selectedBank: MutableState<String> = mutableStateOf("")

    private var currentReadingBank: RFIDMemoryBank? = null

    fun registerReceiver() {
        if (receiver != null) return

        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                intent ?: return
                val action = intent.action ?: return

                when (action) {
                    GeneralString.Intent_RFIDSERVICE_CONNECTED -> {
                        rfidHelper.setScanMode(ScanMode.Single)
                        rfidHelper.setRFIDMode(RFIDMode.Inventory_EPC_TID)

                        viewModelScope.launch {
                            delay(500)
                            if (rfidHelper.configureRfid()) {
                                Log.d("RFIDview", "RFID configuration successful")
                            } else {
                                Log.e("RFIDview", "RFID configuration failed")
                            }
                        }
                    }

                    GeneralString.Intent_RFIDSERVICE_TAG_DATA -> {
                        val type = intent.getIntExtra(GeneralString.EXTRA_DATA_TYPE, -1)
                        val response = intent.getIntExtra(GeneralString.EXTRA_RESPONSE, -1)

                        Log.d("RFID_DEBUG", "Received TAG_DATA, type: $type, response: $response")

                        when (response) {
                            0 -> {
                                val epc = intent.getStringExtra(GeneralString.EXTRA_EPC) ?: ""
                                val tid = intent.getStringExtra(GeneralString.EXTRA_TID) ?: ""

                                _epcValue.value = epc
                                _tidValue.value = tid
                                _isShowChooseBank.value = true
                                selectedBank.value = ""
                                _isShowPasswordLayout.value = false
                                _isShowWriteLayout.value = false
                            }

                            6 -> showToast("Password error, please enter correct password")
                            1 -> Log.d("RFID_DEBUG", "Operation in progress")
                            else -> {
                                Log.e("RFID_DEBUG", "Operation failed, response code: $response")
                                showToast("Operation failed, error code: $response")
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
    }

    fun unregisterReceiver() {
        receiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                Log.w("RFID_DEBUG", "Receiver already unregistered")
            }
            receiver = null
        }
    }

    fun onReadClicked() {
        _epcValue.value = ""
        _tidValue.value = ""
        _passwordValue.value = "00000000"
        _writeDataValue.value = ""
        _isShowChooseBank.value = false
        _isShowPasswordLayout.value = false
        _isShowWriteLayout.value = false
        selectedBank.value = ""
        registerReceiver()
        startSingleScan()
    }

    private fun startSingleScan() {
        try {
            if (rfidHelper.startSingleScan()) {
                showToast("Starting scan")
            } else {
                showToast("Failed to start scan")
            }
        } catch (e: Exception) {
            showToast("Scan start error: ${e.message}")
            Log.e("RFID_DEBUG", "Scan start error", e)
        }
    }

    fun onClearClicked() {
        _epcValue.value = ""
        _tidValue.value = ""
        _passwordValue.value = "00000000"
        _writeDataValue.value = ""
        _isShowChooseBank.value = false
        _isShowPasswordLayout.value = false
        _isShowWriteLayout.value = false
        selectedBank.value = ""

        unregisterReceiver()
    }

    fun onBankSelected(selected: String) {
        selectedBank.value = selected
        when (selected) {
            "Reserved", "USER" -> {
                _isShowPasswordLayout.value = true
                _isShowWriteLayout.value = false
            }

            "EPC" -> {
                _isShowPasswordLayout.value = false
                _isShowWriteLayout.value = true
            }

            else -> {
                _isShowPasswordLayout.value = false
                _isShowWriteLayout.value = false
            }
        }
    }

    fun onWriteClicked() {
        val epc = _epcValue.value
        if (epc.isNullOrEmpty()) {
            showToast("Please scan tag to get EPC first")
            return
        }

        val writeDataInput = _writeDataValue.value
        if (!RFIDHelper.isValidHexString(writeDataInput)) {
            showToast("Please enter valid hexadecimal data")
            return
        }

        val bankString = selectedBank.value
        if (bankString.isEmpty()) {
            showToast("Please select a valid memory bank")
            return
        }

        val bank = when (bankString) {
            "Reserved" -> RFIDMemoryBank.Reserved
            "EPC" -> RFIDMemoryBank.EPC
            "USER" -> RFIDMemoryBank.User
            else -> null
        }

        if (bank == null) {
            showToast("Please select a valid memory bank")
            return
        }

        val passwordInput = _passwordValue.value
        val passwordBytes = RFIDHelper.getPasswordBytes(passwordInput)
        val offset = getOffsetForSelectedBank(bank)

        val response = rfidHelper.writeTagMemory(epc, bank, offset, writeDataInput, passwordBytes)
        Log.e("gdhf", "onWriteClicked: "+Gson().toJson(response))
//        showToast(RFIDHelper.handleWriteResponse(response, bank))

        if (response == DeviceResponse.OperationSuccess && !_tidValue.value.isNullOrEmpty()) {
            Handler(Looper.getMainLooper()).postDelayed({
                rfidHelper.readTagMemoryByTID(_tidValue.value, bank, offset, 8, passwordBytes)
                currentReadingBank = bank
            }, 1000)
        }
    }

    fun updatePassword(value: String) {
        _passwordValue.value = value
    }

    fun updateWriteData(value: String) {
        _writeDataValue.value = value
    }

    fun onPasswordClicked() {
        val epc = _epcValue.value
        if (epc.isNullOrEmpty()) {
            showToast("Please scan tag first or select Reserved area")
            return
        }

        val bank = if (selectedBank.value == "Reserved") {
            RFIDMemoryBank.Reserved
        } else {
            RFIDMemoryBank.User
        }

        val passwordInput = _passwordValue.value
        val password = RFIDHelper.getPasswordBytes(passwordInput)

        val success = rfidHelper.readTagMemory(epc, bank, 0, 8, password)
        if (!success) {
            showToast("Read failed, check password or tag position")
        }
        currentReadingBank = bank
    }

    private fun getOffsetForSelectedBank(bank: RFIDMemoryBank): Int {
        return when (bank) {
            RFIDMemoryBank.Reserved -> 0
            RFIDMemoryBank.EPC -> 2
            RFIDMemoryBank.User -> 0
            RFIDMemoryBank.TID -> TODO()
            RFIDMemoryBank.Err -> TODO()
        }
    }

    private fun showToast(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
