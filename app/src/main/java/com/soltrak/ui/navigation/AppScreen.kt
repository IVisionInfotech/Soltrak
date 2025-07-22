package com.soltrak.ui.navigation

import android.net.Uri
import com.google.gson.Gson
import com.soltrak.data.database.ModuleEntity
import com.soltrak.ui.screen.rfid.inventoryScreen.RfidTag

sealed class AppScreen(val route: String) {
    object DashboardScreen : AppScreen("dashboard_screen")
    object BarcodeScanScreen : AppScreen("barcode_scan_screen")
    object InventoryScreen : AppScreen("inventory_screen")
    object SettingsScreen : AppScreen("settings_screen")
    object SupportScreen : AppScreen("support_screen")

    object RfidReportScreen : AppScreen("rfid_report_screen/{moduleEntityJson}") {
        fun createRoute(moduleEntity: ModuleEntity): String {
            val moduleEntityJson = Gson().toJson(moduleEntity)
            return "rfid_report_screen/$moduleEntityJson"
        }
    }

    object FindTagScreen : AppScreen("findTag_screen/{rfidTag}/{mode}") {
        fun createRoute(rfidTag: RfidTag, mode: String): String {
            val rfidTagJson = Gson().toJson(rfidTag)
            return "findTag_screen/${Uri.encode(rfidTagJson)}/$mode"
        }
    }
}