package com.soltrak.ui.screen.rfid.inventoryScreen

import android.util.Log
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun InventoryScreenWrapper(
    onBackPressed: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel(),
    onNavigateTo: (RfidTag,String) -> Unit,
    externalReset: Boolean,
    scanMode: String,
    onResetConsumed: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val tagList by viewModel.tagList.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()


    LaunchedEffect(externalReset) {
        if (externalReset) {
            viewModel.resetTagList()
            onResetConsumed()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.d("InventoryScreen", "Registering receiver")
                    viewModel.registerReceiver()
                }

                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.unregisterReceiver()
                }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            viewModel.unregisterReceiver()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val filteredTags = remember(tagList, scanMode) {
        tagList.map {
            RfidTag(
                epc = if (scanMode == "EPC") it.epc else it.tid,
                tid = it.tid,
                count = it.count
            )
        }
    }

    InventoryScreen(
        tagList = filteredTags,
        isScanning = isScanning,
        currentMode = scanMode,
        onBackPressed = onBackPressed,
        onStartStopToggle = { viewModel.toggleScan() },
        onReset = { viewModel.resetTagList() },
        onModeChange = { },
        onItemClick = { tag -> onNavigateTo(tag, scanMode) }
    )
}
