package com.soltrak.ui.screen.settings

import android.app.Application
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.cipherlab.rfid.ClResult
import com.soltrak.utils.PreferencesManager
import com.soltrak.utils.RFIDHelper
import com.soltrak.utils.RFIDHelper.Companion.TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val prefsManager: PreferencesManager,
    private val rfidHelper: RFIDHelper,
) : AndroidViewModel(application) {

    private val _powerLevel = MutableStateFlow(prefsManager.powerLevel)
    val powerLevel: StateFlow<Int> = _powerLevel

    private val _settingE = MutableStateFlow(prefsManager.settingE)
    val settingE: StateFlow<Int> = _settingE

    private val _settingD = MutableStateFlow(prefsManager.settingD)
    val settingD: StateFlow<Int> = _settingD

    private val _scanTimeout = MutableStateFlow(prefsManager.scanTimeout)
    val scanTimeout: StateFlow<Int> = _scanTimeout

    // Added RSSI thresholds that allow negative values
    private val _rssiThresholdYellow = MutableStateFlow(prefsManager.rssiThresholdYellow)
    val rssiThresholdYellow: StateFlow<Int> = _rssiThresholdYellow

    private val _rssiThresholdGreen = MutableStateFlow(prefsManager.rssiThresholdGreen)
    val rssiThresholdGreen: StateFlow<Int> = _rssiThresholdGreen

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _showResetMessage = MutableStateFlow<String?>(null)
    val showResetMessage: StateFlow<String?> = _showResetMessage


    init {
        if (rfidHelper.isInitialized()) {
            val power = rfidHelper.txPower
            prefsManager.powerLevel = power
            Log.d("ModuleViewModel", "Tx Power stored in prefs: $power")
        }
    }

    fun resetFactoryData() {
      val message =  rfidHelper.reset()
        _showResetMessage.value = message
    }

    fun resetAppFactoryData() {
        _powerLevel.value = PreferencesManager.DEFAULT_POWER_LEVEL
        _settingE.value = PreferencesManager.DEFAULT_SETTING_E
        _settingD.value = PreferencesManager.DEFAULT_SETTING_D
        _rssiThresholdYellow.value = PreferencesManager.DEFAULT_RSSI_THRESHOLD_YELLOW
        _rssiThresholdGreen.value = PreferencesManager.DEFAULT_RSSI_THRESHOLD_GREEN
        _scanTimeout.value = PreferencesManager.DEFAULT_SCAN_TIMEOUT

        prefsManager.powerLevel = PreferencesManager.DEFAULT_POWER_LEVEL
        prefsManager.settingE = PreferencesManager.DEFAULT_SETTING_E
        prefsManager.settingD = PreferencesManager.DEFAULT_SETTING_D
        prefsManager.rssiThresholdYellow = PreferencesManager.DEFAULT_RSSI_THRESHOLD_YELLOW
        prefsManager.rssiThresholdGreen = PreferencesManager.DEFAULT_RSSI_THRESHOLD_GREEN
        prefsManager.scanTimeout = PreferencesManager.DEFAULT_SCAN_TIMEOUT

        _errorMessage.value = null
        _showResetMessage.value = "App data reset to defaults"
    }

    fun clearResetMessage() {
        _showResetMessage.value = null
    }

    fun updatePowerLevel(level: Int) {
        if (level in 5..26) {
            _powerLevel.value = level
            prefsManager.powerLevel = level
            rfidHelper.setTxPower(level)
        }
    }

    fun updateSettingE(value: Int) {
        _settingE.value = value
        prefsManager.settingE = value
        validate()
    }

    fun updateSettingD(value: Int) {
        _settingD.value = value
        prefsManager.settingD = value
        validate()
    }

    fun updateScanTimeOut(value: Int) {
        _scanTimeout.value = value
        prefsManager.scanTimeout = value
    }

    fun updateRssiThresholdYellow(value: Int) {
        _rssiThresholdYellow.value = value
        prefsManager.rssiThresholdYellow = value
    }

    fun updateRssiThresholdGreen(value: Int) {
        _rssiThresholdGreen.value = value
        prefsManager.rssiThresholdGreen = value
    }

    private fun validate() {
        _errorMessage.value = when {
            _settingD.value == 0 -> "Setting D cannot be 0"
            _settingD.value > _settingE.value -> "Setting D cannot be greater than Setting E"
            else -> null
        }
    }
}
