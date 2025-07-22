package com.soltrak.ui.screen.barcodeScanScreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.soltrak.ui.components.CommonTopAppBar
import com.soltrak.ui.theme.*

@Composable
fun BarcodeScanScreen(
    title: String = "Barcode Scan",
    barcodeValue: String,
    onBarcodeChange: (String) -> Unit,
    isLoading: Boolean,
    isBarcodeValid: Boolean?,
    epcValue: String?,
    tidValue: String?,
    uniqueEpcCount: String?,
    showRfidCard: Boolean = true,
    rfidScanInProgress: Boolean,
    showMismatchDialog: Boolean,
    isReportLoading: Boolean,
    onDismissMismatchDialog: () -> Unit,
    onRescan: () -> Unit,
    onCheckDatabase: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onGetReport: () -> Unit,
    onBackPressed: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(title = title, onBackClick = onBackPressed)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().height(70.dp),
                colors = CardDefaults.cardColors(containerColor = TextWhite),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
                ) {
                    TextField(
                        value = barcodeValue,
                        onValueChange = onBarcodeChange,
                        placeholder = { Text("Waiting for barcode...") },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            cursorColor = DarkText,
                            focusedTextColor = DarkText,
                            unfocusedTextColor = DarkText,
                            unfocusedPlaceholderColor = DarkText.copy(alpha = 0.5f)
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { onCheckDatabase() })
                    )
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = PrimaryOrange)
            }

            if (barcodeValue.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                when (isBarcodeValid) {
                    true -> Text("✅ Sr. No.: $barcodeValue — Please scan the RFID tag.", color = PrimaryOrange)
                    false -> Text("❌ Sr. No.: $barcodeValue not available.", color = DangerRed)
                    else -> {}
                }
            }

            if (showRfidCard) {
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { if (rfidScanInProgress) onStopScan() else onStartScan() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrownishPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (rfidScanInProgress) "Stop Scan" else "Start Scan")
                }
            }

            if (showMismatchDialog) {
                AlertDialog(
                    onDismissRequest = onDismissMismatchDialog,
                    title = { Text("Multiple RFID tags detected") },
                    text = { Text("Total tags found: $uniqueEpcCount.\nPlease try again.") },
                    confirmButton = {
                        TextButton(onClick = onDismissMismatchDialog) {
                            Text("OK")
                        }
                    }
                )
            }

            if (isReportLoading) {
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Generating Report") },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = PrimaryOrange,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Please wait while we prepare the report...")
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}


