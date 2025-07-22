package com.soltrak.ui.screen.rfid.writeTag

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController

@Composable
fun RfidScreenWrapper(
    navController: NavHostController,
    viewModel: RfidViewModel = viewModel(),
) {
    val bankOptions = listOf("Reserved", "EPC", "USER")
    val lifecycleOwner = LocalLifecycleOwner.current

    val epcValue by viewModel.epcValue.collectAsState()
    val tidValue by viewModel.tidValue.collectAsState()
    val passwordValue by viewModel.passwordValue.collectAsState()
    val writeDataValue by viewModel.writeDataValue.collectAsState()
    val showPasswordLayout by viewModel.showPasswordLayout.collectAsState()
    val showWriteLayout by viewModel.showWriteLayout.collectAsState()
    val showChooseBank by viewModel.showChooseBank.collectAsState()

    var bank by remember { mutableStateOf<String?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
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


    RfidScreen(
        bankOptions = bankOptions,
        epcValue = epcValue,
        tidValue = tidValue,
        passwordValue = passwordValue,
        writeDateValue = writeDataValue,
        selectedBank = bank,
        showChooseBank = showChooseBank,
        showPasswordLayout = showPasswordLayout,
        showWriteLayout = showWriteLayout,
        onReadClicked = {
            viewModel.onReadClicked()
            bank = null
        },
        onClearClicked = { viewModel.onClearClicked() },
        onWriteClicked = { viewModel.onWriteClicked() },
        onPasswordClicked = { viewModel.onPasswordClicked() },
        onBankSelected = {
            bank = it
            viewModel.onBankSelected(it)
        },
        onPasswordValueChange = { viewModel.updatePassword(it) },
        onWriteDataValueChange = { viewModel.updateWriteData(it) },
        onBackPressed = { navController.popBackStack() }
    )
}


