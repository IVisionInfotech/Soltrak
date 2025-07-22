package com.soltrak.utils

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    companion object {

        private const val KEY_PROGRESS_PERCENTAGE = "progress_percentage"
        private const val KEY_IMPORT_STATUS_TEXT = "import_status_text"

        private const val KEY_RSSI_THRESHOLD_YELLOW = "rssi_threshold_yellow"
        private const val KEY_RSSI_THRESHOLD_GREEN = "rssi_threshold_red"
        private const val KEY_SETTING_E = "setting_e"
        private const val KEY_SETTING_D = "setting_d"
        private const val KEY_POWER_LEVEL = "power_level"
        private const val KEY_SCAN_TIMEOUT = "scan_timeout"


        private const val KEY_IS_UNLOCKED = "is_unlocked"


        const val DEFAULT_RSSI_THRESHOLD_YELLOW = -37
        const val DEFAULT_RSSI_THRESHOLD_GREEN = -19

        const val DEFAULT_POWER_LEVEL = 24

        const val DEFAULT_SETTING_E = 10
        const val DEFAULT_SETTING_D = 5

        const val DEFAULT_SCAN_TIMEOUT = 5
    }

    var progressPercentage: Int
        get() = prefs.getInt(KEY_PROGRESS_PERCENTAGE, 0)
        set(value) = prefs.edit { putInt(KEY_PROGRESS_PERCENTAGE, value) }

    var importStatusText: String
        get() = prefs.getString(KEY_IMPORT_STATUS_TEXT, "") ?: ""
        set(value) = prefs.edit { putString(KEY_IMPORT_STATUS_TEXT, value) }

    var rssiThresholdYellow: Int
        get() = prefs.getInt(KEY_RSSI_THRESHOLD_YELLOW, DEFAULT_RSSI_THRESHOLD_YELLOW)
        set(value) = prefs.edit { putInt(KEY_RSSI_THRESHOLD_YELLOW, value) }

    var rssiThresholdGreen: Int
        get() = prefs.getInt(KEY_RSSI_THRESHOLD_GREEN, DEFAULT_RSSI_THRESHOLD_GREEN)
        set(value) = prefs.edit { putInt(KEY_RSSI_THRESHOLD_GREEN, value) }

    var settingE: Int
        get() = prefs.getInt(KEY_SETTING_E, DEFAULT_SETTING_E)
        set(value) = prefs.edit { putInt(KEY_SETTING_E, value) }

    var settingD: Int
        get() = prefs.getInt(KEY_SETTING_D, DEFAULT_SETTING_D)
        set(value) = prefs.edit { putInt(KEY_SETTING_D, value) }

    var powerLevel: Int
        get() = prefs.getInt(KEY_POWER_LEVEL, DEFAULT_POWER_LEVEL)
        set(value) = prefs.edit { putInt(KEY_POWER_LEVEL, value) }

    var scanTimeout: Int
        get() = prefs.getInt(KEY_SCAN_TIMEOUT, DEFAULT_SCAN_TIMEOUT)
        set(value) = prefs.edit { putInt(KEY_SCAN_TIMEOUT, value) }

    var isUnlocked: Boolean
        get() = prefs.getBoolean(KEY_IS_UNLOCKED, false)
        set(value) = prefs.edit { putBoolean(KEY_IS_UNLOCKED, value) }

    fun clear() {
        prefs.edit {
            remove(KEY_PROGRESS_PERCENTAGE)
                .remove(KEY_IMPORT_STATUS_TEXT)
        }
    }

}
