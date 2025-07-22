package com.soltrak.ui.screen.barcodeScanScreen

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import com.soltrak.data.database.ModuleEntity

@Composable
fun BarcodeScanScreenWrapper(
    navController: NavHostController,
    viewModel: BarcodeScanViewModel = viewModel(),
    onNavigateToReport: (ModuleEntity) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val barcodeValue by viewModel.barcodeValue.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uniqueEpcCount by viewModel.uniqueEpcCount.collectAsState()
    val isBarcodeValid by viewModel.isBarcodeValid.collectAsState()
    val epcValue by viewModel.epcValue.collectAsState()
    val tidValue by viewModel.tidValue.collectAsState()
    val showDialog by viewModel.showMismatchDialog.collectAsState()
    val rfidScanInProgress by viewModel.rfidScanInProgress.collectAsState()
    val isReportLoading by viewModel.isReportLoading.collectAsState()

    val showRfidCard = isBarcodeValid == true

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.registerReceiver()
                Lifecycle.Event.ON_PAUSE -> viewModel.unregisterReceiver()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.unregisterReceiver()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(epcValue, tidValue) {
        if (!epcValue.isNullOrEmpty() && !tidValue.isNullOrEmpty()) {
            viewModel.getReportData(onNavigateToReport)
        }
    }

    BarcodeScanScreen(
        barcodeValue = barcodeValue ?: "",
        onBarcodeChange = viewModel::onBarcodeChange,
        isLoading = isLoading,
        isBarcodeValid = isBarcodeValid,
        epcValue = epcValue,
        tidValue = tidValue,
        uniqueEpcCount = uniqueEpcCount,
        showRfidCard = showRfidCard,
        rfidScanInProgress = rfidScanInProgress,
        showMismatchDialog = showDialog,
        isReportLoading = isReportLoading,
        onDismissMismatchDialog = { viewModel.onDismissMismatchDialog() },
        onRescan = { viewModel.onRescan() },
        onCheckDatabase = { viewModel.checkDatabase() },
        onStartScan = {
            viewModel.startRfidScan {
                viewModel.autoStopAndProcess(onNavigateToReport)
            }
        },
        onStopScan = { viewModel.stopRfidScan(); viewModel.getReportData(onNavigateToReport) },
        onGetReport = { viewModel.getReportData(onNavigateToReport) },
        onBackPressed = { navController.popBackStack() }
    )
}
