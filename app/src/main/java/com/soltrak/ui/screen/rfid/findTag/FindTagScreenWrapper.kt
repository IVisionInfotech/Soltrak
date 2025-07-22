package com.soltrak.ui.screen.rfid.findTag

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.soltrak.ui.screen.rfid.inventoryScreen.RfidTag

@Composable
fun FindTagScreenWrapper(
    rfidTag: RfidTag,
    currentMode: String,
    onBackPressed: () -> Unit,
    viewModel: FindTagViewModel = hiltViewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val yellowThreshold = viewModel.yellowThreshold
    val greenThreshold = viewModel.greenThreshold

    val tagId by viewModel.tagId.collectAsState()
    val isFinding by viewModel.isFinding.collectAsState()
    val rssi by viewModel.rssi.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val initialTagId = if (currentMode == "EPC") rfidTag.epc else rfidTag.tid
    var entered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setMode(currentMode)
        viewModel.onTagIdChanged(initialTagId)
    }

    LaunchedEffect(tagId) {
        if (tagId.isNotBlank() && !entered) {
            entered = true
        }
    }

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

    FindTagScreen(
        tagId = tagId,
        isFinding = isFinding,
        rssi = rssi,
        mode = mode,
        entered = entered,
        yellowThreshold = yellowThreshold,
        greenThreshold = greenThreshold,
        onBackPressed = onBackPressed,
        onTagIdChange = viewModel::onTagIdChanged,
        onModeChange = viewModel::setMode,
        onEnterClick = { entered = true },
        onStartClick = viewModel::startFinding,
        onStopClick = viewModel::stopFinding
    )
}

