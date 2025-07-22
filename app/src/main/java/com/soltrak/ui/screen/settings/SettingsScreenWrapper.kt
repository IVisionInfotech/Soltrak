package com.soltrak.ui.screen.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SettingsScreenWrapper(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val powerLevel = viewModel.powerLevel.collectAsState()
    val settingE = viewModel.settingE.collectAsState()
    val settingD = viewModel.settingD.collectAsState()
    val scanTimeout = viewModel.scanTimeout.collectAsState()
    val rssiThresholdYellow = viewModel.rssiThresholdYellow.collectAsState()
    val rssiThresholdGreen = viewModel.rssiThresholdGreen.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()
    val resetMessage = viewModel.showResetMessage.collectAsState()


    SettingsScreen(
        powerLevel = powerLevel.value,
        settingE = settingE.value,
        settingD = settingD.value,
        scanTimeout = scanTimeout.value,
        rssiThresholdYellow = rssiThresholdYellow.value,
        rssiThresholdGreen = rssiThresholdGreen.value,
        errorMessage = errorMessage.value,
        resetMessage = resetMessage.value,
        onResetMessageShown = viewModel::clearResetMessage,
        onResetClick = viewModel::resetFactoryData,
        onAppResetClick = viewModel::resetAppFactoryData,
        onPowerLevelClick = viewModel::updatePowerLevel,
        onSettingEChange = viewModel::updateSettingE,
        onSettingDChange = viewModel::updateSettingD,
        onScanTimeoutChange = viewModel::updateScanTimeOut,
        onRssiYellowChange = viewModel::updateRssiThresholdYellow,
        onRssiGreenChange = viewModel::updateRssiThresholdGreen,
        onBackPressed = { onBackPressed() },
    )
}
