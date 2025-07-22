package com.soltrak.ui.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.soltrak.ModuleViewModel
import com.soltrak.data.database.ModuleEntity
import com.soltrak.ui.screen.DashboardScreen
import com.soltrak.ui.screen.RfidReportScreen
import com.soltrak.ui.screen.SupportScreen
import com.soltrak.ui.screen.barcodeScanScreen.BarcodeScanScreenWrapper
import com.soltrak.ui.screen.barcodeScanScreen.BarcodeScanViewModel
import com.soltrak.ui.screen.rfid.findTag.FindTagScreenWrapper
import com.soltrak.ui.screen.rfid.inventoryScreen.InventoryScreenWrapper
import com.soltrak.ui.screen.rfid.inventoryScreen.RfidTag
import com.soltrak.ui.screen.settings.SettingsScreenWrapper

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    progressPercent: Int,
    importStatusText: String,
    navController: NavHostController,
    viewModel: ModuleViewModel,
    onPickFile: () -> Unit,
    onExportFile: () -> Unit,
    resetTriggered: Boolean,
    scanMode: String,
    onResetConsumed: () -> Unit
) {
    val context = LocalContext.current

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = AppScreen.BarcodeScanScreen.route
    ) {

        composable(AppScreen.DashboardScreen.route) {
            DashboardScreen(
                isLoading = isLoading,
                progressPercent = progressPercent,
                importStatusText = importStatusText,
                onPickFile = { onPickFile() },
                onScanBarcode = { navController.navigate(AppScreen.BarcodeScanScreen.route) },
                onScanRFID = { navController.navigate(AppScreen.InventoryScreen.route) },
                onExport = { onExportFile() },
                onSettingsClick = { navController.navigate(AppScreen.SettingsScreen.route) }
            )
        }

        composable(AppScreen.BarcodeScanScreen.route) {
            val barcodeScanViewModel: BarcodeScanViewModel = hiltViewModel()
            BarcodeScanScreenWrapper(
                navController = navController,
                viewModel = barcodeScanViewModel,
                onNavigateToReport = { moduleEntity ->
                    navController.navigate(AppScreen.RfidReportScreen.createRoute(moduleEntity))
                }
            )
        }

        composable(AppScreen.InventoryScreen.route) {
            InventoryScreenWrapper(onBackPressed = {
                navController.popBackStack()
            }, onNavigateTo = { rfidTag, currentMode ->
                navController.navigate(AppScreen.FindTagScreen.createRoute(rfidTag, currentMode))
            }, externalReset = resetTriggered, scanMode = scanMode, onResetConsumed = onResetConsumed)
        }

        composable(AppScreen.SupportScreen.route) {
            SupportScreen(onBackPressed = {
                navController.popBackStack()
            })
        }

        composable(AppScreen.SettingsScreen.route) {
            SettingsScreenWrapper(onBackPressed = {
                navController.popBackStack()
            })
        }

        composable(
            route = AppScreen.RfidReportScreen.route,
            arguments = listOf(
                navArgument("moduleEntityJson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moduleEntityJson = backStackEntry.arguments?.getString("moduleEntityJson")
            val moduleEntity = Gson().fromJson(moduleEntityJson, ModuleEntity::class.java)

            if (moduleEntity != null) {
                RfidReportScreen(moduleEntity = moduleEntity, onBackPressed = {
                    navController.popBackStack()
                })
            } else {
                Toast.makeText(context, "Failed to load report data", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

        composable(
            route = AppScreen.FindTagScreen.route,
            arguments = listOf(
                navArgument("rfidTag") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rfidTagJson = backStackEntry.arguments?.getString("rfidTag")
            val mode = backStackEntry.arguments?.getString("mode") ?: "EPC"
            val rfidTag = Gson().fromJson(rfidTagJson, RfidTag::class.java)

            if (rfidTag != null) {
                FindTagScreenWrapper(
                    rfidTag = rfidTag,
                    currentMode = mode,
                    onBackPressed = { navController.popBackStack() }
                )
            } else {
                Toast.makeText(context, "Invalid tag", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
        }

    }
}