package com.soltrak.utils

import android.content.Context
import android.util.Log
import com.cipherlab.rfid.ClResult
import com.cipherlab.rfid.DeviceResponse
import com.cipherlab.rfid.DeviceVoltageInfo
import com.cipherlab.rfid.InventoryType
import com.cipherlab.rfid.LockTarget
import com.cipherlab.rfid.ModuleTemperature
import com.cipherlab.rfid.RFIDMemoryBank
import com.cipherlab.rfid.RFIDMode
import com.cipherlab.rfid.RfidEpcFilter
import com.cipherlab.rfid.ScanMode
import com.cipherlab.rfid.SwitchMode
import com.cipherlab.rfid.WorkMode
import com.cipherlab.rfidapi.RfidManager
import com.google.gson.Gson

class RFIDHelper(context: Context?) {
        private var rfidManager: RfidManager? = null
        private var initialized = false

    init {
        if (!initialized) {
            try {
                this.rfidManager = RfidManager.InitInstance(context)
                if (rfidManager != null) {
                    initialized = true
                    Log.e(TAG, "RFID initialized")
                }
            } catch (e: Exception) {
                Log.e(TAG, "RFID initialization failed: ${e.message}")
                initialized = false
            }
        }
    }

    fun reset() : String {
//        val re: Int = rfidManager!!.ResetToDefault()
//        if (re != ClResult.S_OK.ordinal) {
//            val last: String = rfidManager!!.GetLastError()
//            Log.e(TAG, "GetLastError = $last")
//            return "Something went wrong"
//        }else{
//            Log.e(TAG, "ResetToDefault = $re \n ${isInitialized()}")
//            return  "Settings reset successfully"
//        }
        return  "Settings reset successfully"
    }

    fun isInitialized(): Boolean {
        return initialized && rfidManager != null
    }


    fun configureRfid(): Boolean {
        try {
            var success = true

            // Check if RFID service is bound
            if (!rfidManager!!.GetConnectionStatus()) {
                Log.e(TAG, "RFID service not bound, cannot configure")
                return false
            }

            var result = rfidManager!!.SetTriggerSwitchMode(true)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set trigger switch mode: " + rfidManager!!.GetLastError())
                success = false
            }

            result = rfidManager!!.SetSwitchModeTriggerCounts(3)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "無法設置切換模式按鍵次數: " + rfidManager!!.GetLastError())
            }

            val modes = ArrayList<SwitchMode>()
            modes.add(SwitchMode.UHFRFIDReader)
            modes.add(SwitchMode.BarcodeReader)

            result = rfidManager!!.SetSwitchModeByCounts(modes)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "無法設置切換模式循環: " + rfidManager!!.GetLastError())
            }

            result = rfidManager!!.SetWorkMode(WorkMode.MultiTagMode)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set work mode: " + rfidManager!!.GetLastError())
                success = false
            }

            result = rfidManager!!.SetScanMode(ScanMode.Continuous)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set scan mode: " + rfidManager!!.GetLastError())
                success = false
            }

            result = rfidManager!!.SetRFIDMode(RFIDMode.Inventory_EPC_TID)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set RFID mode: " + rfidManager!!.GetLastError())
                success = false
            }

            return success
        } catch (e: Exception) {
            Log.e(TAG, "Exception during RFID configuration: " + e.message, e)
            return false
        }
    }

    fun setScanMode(mode: ScanMode?): Int {
        return rfidManager!!.SetScanMode(mode)
    }

    fun setRFIDMode(mode: RFIDMode?): Int {
        return rfidManager!!.SetRFIDMode(mode)
    }

    fun softScanTrigger(Status: Boolean): Int {
        return rfidManager!!.SoftScanTrigger(Status)
    }

    fun startSingleScan(): Boolean {
        try {
            val clearResult = rfidManager!!.ClearFilterDuplicate()
            if (clearResult != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to clear duplicate filter: " + rfidManager!!.GetLastError())
            }

            val result = rfidManager!!.RFIDDirectStartInventoryRound(InventoryType.EPC_AND_TID, 1)
            Log.e(TAG, "startSingleScan: "+Gson().toJson(result) )
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to start scanning: " + rfidManager!!.GetLastError())
                return false
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception during scan start: " + e.message, e)
            return false
        }
    }

    fun cancelScan(): Boolean {
        try {
            val result = rfidManager!!.RFIDDirectCancelInventoryRound()
            return result == ClResult.S_OK.ordinal
        } catch (e: Exception) {
            Log.e(TAG, "Exception during scan cancellation: " + e.message, e)
            return false
        }
    }

    fun readTagMemory(
        epc: String?,
        bank: RFIDMemoryBank,
        offsetWords: Int,
        lengthWords: Int,
        password: ByteArray?
    ): Boolean {
        var password = password
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot read: EPC is empty")
            return false
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)

            if (password == null) {
                password = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
            }

            val offsetBytes = offsetWords * 2
            val lengthBytes = lengthWords * 2

            Log.d(
                TAG,
                "Reading " + bank.name + " area, offset: " + offsetWords + " words, length: " + lengthWords + " words"
            )

            val result = rfidManager!!.RFIDDirectReadTagByEPC(
                password,
                epcBytes,
                bank,
                offsetBytes,
                lengthBytes,
                3
            )

            if (result != ClResult.S_OK.ordinal) {
                val errorMsg = rfidManager!!.GetLastError()
                Log.e(TAG, "Failed to read " + bank.name + ": " + errorMsg)
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception while reading " + bank.name + ": " + e.message, e)
            return false
        }
    }

    fun readTagMemoryByTID(
        tid: String?,
        bank: RFIDMemoryBank,
        offsetWords: Int,
        lengthWords: Int,
        password: ByteArray?
    ): Boolean {
        var password = password
        if (tid.isNullOrEmpty()) {
            Log.e(TAG, "Cannot read: TID is empty")
            return false
        }

        try {
            val tidBytes: ByteArray = UtilityHelp.StringToByteArray(tid)

            if (password == null) {
                password = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
            }

            val offsetBytes = offsetWords * 2
            val lengthBytes = lengthWords * 2

            Log.d(
                TAG,
                "Reading " + bank.name + " area, offset: " + offsetWords + " words, length: " + lengthWords + " words"
            )

            val result = rfidManager!!.RFIDDirectReadTagByTID(
                password,
                tidBytes,
                bank,
                offsetBytes,
                lengthBytes,
                3
            )

            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to read " + rfidManager!!.GetLastError())
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception while reading " + e.message)
            return false
        }
    }

    fun writeTagMemory(
        epc: String?,
        bank: RFIDMemoryBank,
        offsetWords: Int,
        data: String?,
        password: ByteArray?
    ): DeviceResponse {
        var password = password
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot write: EPC is empty")
            return DeviceResponse.OperationFail
        }

        if (data.isNullOrEmpty() || data.length % 2 != 0 || !isValidHexString(data)) {
            Log.e(TAG, "Cannot write: Invalid data format")
            return DeviceResponse.OperationFail
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)
            val dataBytes: ByteArray = UtilityHelp.StringToByteArray(data)

            if (password == null) {
                password = byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())
            }

            val offsetBytes = offsetWords * 2

            Log.d(
                TAG,
                "Writing to " + bank.name + " area, offset: " + offsetWords + " words, data: " + data
            )

            val response = rfidManager!!.RFIDDirectWriteTagByEPC(
                password,
                epcBytes,
                bank,
                offsetBytes,
                3,
                dataBytes
            )

            Log.d(TAG, "Write " + bank.name + " result: " + response)

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception while writing to " + bank.name + ": " + e.message, e)
            return DeviceResponse.OperationFail
        }
    }

    fun lockTagMemory(epc: String?, targetBank: LockTarget, password: ByteArray?): DeviceResponse {
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot lock: EPC is empty")
            return DeviceResponse.OperationFail
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)

            if (password == null || password.size != 4) {
                Log.e(TAG, "Cannot lock: Valid access password required")
                return DeviceResponse.OperationFail
            }

            Log.d(TAG, "Locking " + targetBank.name + " area")

            val response = rfidManager!!.RFIDDirectLockTag(
                password,
                epcBytes,
                targetBank
            )

            Log.d(TAG, "Lock " + targetBank.name + " result: " + response)

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception while locking " + targetBank.name + ": " + e.message, e)
            return DeviceResponse.OperationFail
        }
    }

    fun unlockTagMemory(
        epc: String?,
        targetBank: LockTarget,
        password: ByteArray?
    ): DeviceResponse {
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot unlock: EPC is empty")
            return DeviceResponse.OperationFail
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)

            if (password == null || password.size != 4) {
                Log.e(TAG, "Cannot unlock: Valid access password required")
                return DeviceResponse.OperationFail
            }

            Log.d(TAG, "Unlocking " + targetBank.name + " area")

            val response = rfidManager!!.RFIDDirectUnlockTag(
                password,
                epcBytes,
                targetBank
            )

            Log.d(TAG, "Unlock " + targetBank.name + " result: " + response)

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception while unlocking " + targetBank.name + ": " + e.message, e)
            return DeviceResponse.OperationFail
        }
    }

    fun permanentLockTagMemory(
        epc: String?,
        targetBank: LockTarget,
        password: ByteArray?
    ): DeviceResponse {
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot permanently lock: EPC is empty")
            return DeviceResponse.OperationFail
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)

            if (password == null || password.size != 4) {
                Log.e(TAG, "Cannot permanently lock: Valid access password required")
                return DeviceResponse.OperationFail
            }

            Log.d(TAG, "Permanently locking " + targetBank.name + " area")

            val response = rfidManager!!.RFIDDirectPermanentLockTag(
                password,
                epcBytes,
                targetBank
            )

            Log.d(TAG, "Permanent lock " + targetBank.name + " result: " + response)

            return response
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Exception while permanently locking " + targetBank.name + ": " + e.message,
                e
            )
            return DeviceResponse.OperationFail
        }
    }

    fun killTag(epc: String?, killPassword: ByteArray?): DeviceResponse {
        if (epc.isNullOrEmpty()) {
            Log.e(TAG, "Cannot kill: EPC is empty")
            return DeviceResponse.OperationFail
        }

        var isZeroPassword = true
        if (killPassword != null && killPassword.size == 4) {
            for (b in killPassword) {
                if (b.toInt() != 0) {
                    isZeroPassword = false
                    break
                }
            }
        }

        if (isZeroPassword) {
            Log.e(TAG, "Cannot kill: Kill password cannot be zero")
            return DeviceResponse.OperationFail
        }

        try {
            val epcBytes: ByteArray = UtilityHelp.StringToByteArray(epc)

            Log.d(TAG, "Killing tag, EPC: $epc")

            val response = rfidManager!!.RFIDDirectKillTag(
                killPassword,
                epcBytes
            )

            Log.d(TAG, "Kill tag result: $response")

            return response
        } catch (e: Exception) {
            Log.e(TAG, "Exception while killing tag: " + e.message, e)
            return DeviceResponse.OperationFail
        }
    }

    val deviceInfo: String

        get() {
            try {
                val info =
                    rfidManager!!.GetDeviceInfo() ?: return "Could not get device information"

                val sb = StringBuilder()
                sb.append("Serial Number: ").append(info.SerialNumber).append("\n")
                sb.append("Region: ").append(info.Region).append("\n")
                sb.append("Kernel Version: ").append(info.KernelVersion).append("\n")
                sb.append("User Version: ").append(info.UserVersion).append("\n")
                sb.append("RFID Module Version: ").append(info.RFIDModuleVersion)

                return sb.toString()
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting device information: " + e.message, e)
                return "Error getting device information: " + e.message
            }
        }

    val moduleTemperature: String

        get() {
            try {
                val temp = ModuleTemperature()
                val result = rfidManager!!.GetModuleTemperature(temp)

                return if (result == ClResult.S_OK.ordinal) {
                    "Module temperature: " + temp.GunModuleTemperature + "°C, Protect temperature: " + temp.GunProtectTemperature + "°C"
                } else {
                    "Could not get temperature information: " + rfidManager!!.GetLastError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting module temperature: " + e.message, e)
                return "Error getting temperature information: " + e.message
            }
        }

    val batteryInfo: String

        get() {
            try {
                val info = DeviceVoltageInfo()
                val result = rfidManager!!.GetBatteryLifePercent(info)

                if (result == ClResult.S_OK.ordinal) {
                    val chargeStatus = when (info.ChargeStatus) {
                        0 -> "Not charging"
                        1 -> "Charging"
                        2 -> "Charge complete"
                        3 -> "Battery fault"
                        else -> "Unknown status"
                    }

                    return "Battery level: " + info.Percentage + "%, Voltage: " + info.Voltage + "V, Status: " + chargeStatus
                } else {
                    return "Could not get battery information: " + rfidManager!!.GetLastError()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting battery information: " + e.message, e)
                return "Error getting battery information: " + e.message
            }
        }

    fun setTxPower(power: Int): Boolean {
        if (power < 5 || power > 30) {
            Log.e(
                TAG,
                "Transmission power out of range (5-30): $power"
            )
            return false
        }

        try {
            val result = rfidManager!!.SetTxPower(power)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set transmission power: " + rfidManager!!.GetLastError())
                return false
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Exception while setting transmission power: " + e.message, e)
            return false
        }
    }

    val txPower: Int

        get() {
            try {
                return rfidManager!!.GetTxPower()
            } catch (e: Exception) {
                Log.e(TAG, "Exception while getting transmission power: " + e.message, e)
                return -1
            }
        }

    fun findEPCTag(targetEpc: String): Boolean {
        if (!isInitialized() || rfidManager == null) {
            Log.e(TAG, "RFID not initialized")
            return false
        }

        if (!isValidHexString(targetEpc)) {
            Log.e(TAG, "Invalid EPC format")
            return false
        }

        try {

            val filter = RfidEpcFilter()
            filter.Enable = 1
            filter.Startbit_MSB = 0
            filter.Startbit_LSB = 0
            filter.PatternLength_MSB = 0
            filter.PatternLength_LSB = (targetEpc.length * 4).toByte()
            filter.Scheme = 0x30
            filter.EPCPattern1 = targetEpc
            filter.EPCPattern2 = ""

            var result = rfidManager!!.SetIncludedEPCFilter(filter)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to set EPC filter: " + rfidManager!!.GetLastError())
                return false
            }

            rfidManager!!.SetScanMode(ScanMode.Continuous)
            rfidManager!!.SetRFIDMode(RFIDMode.Inventory_EPC_TID)

            result = rfidManager!!.SoftScanTrigger(true)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to start scanning: " + rfidManager!!.GetLastError())
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error finding tag: " + e.message)
            return false
        }
    }

    fun findTIDTag(targetTid: String?): Boolean {
        if (!isInitialized() || rfidManager == null) {
            Log.e(TAG, "RFID not initialized")
            return false
        }

        if (!isValidHexString(targetTid)) {
            Log.e(TAG, "Invalid TID format")
            return false
        }

        try {

            val epcFilter = RfidEpcFilter()
            epcFilter.Enable = 0
            rfidManager!!.SetIncludedEPCFilter(epcFilter)

            rfidManager!!.SetRFIDMode(RFIDMode.Inventory_EPC_TID)
            rfidManager!!.SetScanMode(ScanMode.Continuous)

            val result = rfidManager!!.SoftScanTrigger(true)
            if (result != ClResult.S_OK.ordinal) {
                Log.e(TAG, "Failed to start scanning: " + rfidManager!!.GetLastError())
                return false
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error finding TID tag: " + e.message)
            return false
        }
    }

    fun stopFindingTag(): Boolean {
        if (!isInitialized() || rfidManager == null) {
            Log.e(TAG, "RFID not initialized")
            return false
        }

        try {
            rfidManager!!.SoftScanTrigger(false)
            rfidManager!!.ClearFilterDuplicate()

            val filter = RfidEpcFilter()
            filter.Enable = 0
            rfidManager!!.SetIncludedEPCFilter(filter)

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tag finding: " + e.message)
            return false
        }
    }

    fun release() {
        if (rfidManager != null) {
            rfidManager!!.Release()
            rfidManager = null
        }
    }

    companion object {
        const val TAG = "RFIDHelper"

        fun isValidHexString(hexString: String?): Boolean {
            if (hexString.isNullOrEmpty() || hexString.length % 2 != 0) {
                return false
            }

            return hexString.matches("^[0-9A-Fa-f]+$".toRegex())
        }

        fun getPasswordBytes(passwordStr: String?): ByteArray {
            val defaultPassword =
                byteArrayOf(0x00.toByte(), 0x00.toByte(), 0x00.toByte(), 0x00.toByte())

            if (passwordStr.isNullOrEmpty() || !isValidHexString(passwordStr) || passwordStr.length != 8) {
                return defaultPassword
            }

            try {
                return UtilityHelp.StringToByteArray(passwordStr)
            } catch (e: Exception) {
                Log.e(TAG, "Password conversion error: " + e.message, e)
                return defaultPassword
            }
        }

        fun handleWriteResponse(response: DeviceResponse, bank: RFIDMemoryBank): String {
            return when (response) {
                DeviceResponse.OperationSuccess -> "Writing to " + bank.name + " successful"
                DeviceResponse.OperationFail -> "Writing to " + bank.name + " failed"
                DeviceResponse.DeviceTimeOut -> "Writing to " + bank.name + " timed out"
                DeviceResponse.DeviceBusy -> "Device busy, cannot write"
                else -> "Writing to " + bank.name + " response: " + response
            }
        }
    }
}