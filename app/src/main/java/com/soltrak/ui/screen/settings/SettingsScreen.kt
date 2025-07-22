package com.soltrak.ui.screen.settings

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.soltrak.ui.components.CommonTopAppBar
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.soltrak.R

enum class ResetType { APP, GENERAL }

@Composable
fun SettingsScreen(
    powerLevel: Int,
    settingE: Int,
    settingD: Int,
    scanTimeout: Int,
    rssiThresholdYellow: Int,
    rssiThresholdGreen: Int,
    errorMessage: String?,
    resetMessage: String?,
    onResetMessageShown: () -> Unit,
    onResetClick: () -> Unit,
    onAppResetClick: () -> Unit,
    onPowerLevelClick: (Int) -> Unit,
    onSettingEChange: (Int) -> Unit,
    onSettingDChange: (Int) -> Unit,
    onScanTimeoutChange: (Int) -> Unit,
    onRssiYellowChange: (Int) -> Unit,
    onRssiGreenChange: (Int) -> Unit,
    onBackPressed: () -> Unit
) {
    var showPowerDialog by remember { mutableStateOf(false) }
    var showDialogForE by remember { mutableStateOf(false) }
    var showDialogForD by remember { mutableStateOf(false) }
    var showDialogForYellow by remember { mutableStateOf(false) }
    var showDialogForGreen by remember { mutableStateOf(false) }
    var showDialogForSupport by remember { mutableStateOf(false) }
    var showDialogForScanTimeOut by remember { mutableStateOf(false) }
    var showResetConfirmDialogFor by remember { mutableStateOf<ResetType?>(null) }


    val context = LocalContext.current

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Settings",
                onBackClick = onBackPressed
            )
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Settings items
                SettingItem(
                    title = "Power Level",
                    value = powerLevel.toString(),
                    onClick = { showPowerDialog = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    title = "Setting E",
                    value = settingE.toString(),
                    onClick = { showDialogForE = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    title = "Setting D",
                    value = settingD.toString(),
                    onClick = { showDialogForD = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    title = "RSSI Threshold Yellow",
                    value = rssiThresholdYellow.toString(),
                    onClick = { showDialogForYellow = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    title = "RSSI Threshold Green",
                    value = rssiThresholdGreen.toString(),
                    onClick = { showDialogForGreen = true }
                )
                Spacer(modifier = Modifier.height(12.dp))

                SettingItem(
                    title = "Scan Time-out",
                    value = scanTimeout.toString(),
                    onClick = { showDialogForScanTimeOut = true }
                )

                errorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1.2f)) {
                        Button(
                            onClick = { showResetConfirmDialogFor = ResetType.APP },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("App Data Reset")
                        }
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { showResetConfirmDialogFor = ResetType.GENERAL },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset")
                        }
                    }
                }
            }

            if (showPowerDialog) {
                PowerLevelDialog(
                    selected = powerLevel,
                    onDismiss = { showPowerDialog = false },
                    onConfirm = {
                        onPowerLevelClick(it)
                        showPowerDialog = false
                    }
                )
            }

            if (resetMessage != null) {
                Toast.makeText(
                    context,
                    resetMessage,
                    Toast.LENGTH_SHORT
                ).show()
                onResetMessageShown()
            }

            if (showDialogForE) {
                EditableSettingDialog(
                    title = "Setting E",
                    initialValue = settingE,
                    onDismiss = { showDialogForE = false },
                    onConfirm = { newE ->
                        onSettingEChange(newE)
                        if (settingD > newE) {
                            onSettingDChange(newE)
                            Toast.makeText(
                                context,
                                "Setting D adjusted to $newE because it cannot be greater than Setting E",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }

            if (showDialogForD) {
                EditableSettingDialog(
                    title = "Setting D",
                    initialValue = settingD,
                    onDismiss = { showDialogForD = false },
                    extraValidation = { inputD ->
                        if (inputD > settingE) "Value cannot be greater than Setting E ($settingE)" else null
                    },
                    onConfirm = { newD -> onSettingDChange(newD) }
                )
            }

            if (showDialogForYellow) {
                EditableSettingDialog(
                    title = "RSSI Threshold Yellow",
                    initialValue = rssiThresholdYellow,
                    onDismiss = { showDialogForYellow = false },
                    onConfirm = { newVal -> onRssiYellowChange(newVal) }
                )
            }

            if (showDialogForGreen) {
                EditableSettingDialog(
                    title = "RSSI Threshold Green",
                    initialValue = rssiThresholdGreen,
                    onDismiss = { showDialogForGreen = false },
                    onConfirm = { newVal -> onRssiGreenChange(newVal) }
                )
            }

            if (showDialogForScanTimeOut) {
                EditableSettingDialog(
                    title = "Scan Time-out",
                    initialValue = scanTimeout,
                    onDismiss = { showDialogForScanTimeOut = false },
                    onConfirm = { newD -> onScanTimeoutChange(newD) }
                )
            }

            if (showDialogForSupport) {
                SupportDialog(onDismissRequest = { showDialogForSupport = false })
            }

            if (showResetConfirmDialogFor != null) {
                AlertDialog(
                    onDismissRequest = { showResetConfirmDialogFor = null },
                    title = { Text("Confirm Reset") },
                    text = {
                        Text(
                            when (showResetConfirmDialogFor) {
                                ResetType.APP -> "Are you sure you want to reset all app data?"
                                ResetType.GENERAL -> "Are you sure you want to reset settings?"
                                else -> ""
                            }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            when (showResetConfirmDialogFor) {
                                ResetType.APP -> onAppResetClick()
                                ResetType.GENERAL -> onResetClick()
                                else -> {}
                            }
                            showResetConfirmDialogFor = null
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetConfirmDialogFor = null }) {
                            Text("No")
                        }
                    }
                )
            }

        }
    }
}


@Composable
fun PowerLevelDialog(
    selected: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val values = (5..26).toList()
    val visibleItems = 5
    val centerIndex = visibleItems / 2
    val itemHeightDp = 40.dp
    val listState = rememberLazyListState()

    val paddedValues = listOf<Int?>(null, null) + values + listOf(null, null)

    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(Unit) {
        val selectedIndex = values.indexOf(selected)
        val scrollIndex = (selectedIndex + 2 - centerIndex).coerceIn(0, paddedValues.lastIndex)
        listState.scrollToItem(scrollIndex)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Power Level") },
        text = {
            Box(
                modifier = Modifier
                    .height(itemHeightDp * visibleItems)
                    .fillMaxWidth()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    flingBehavior = flingBehavior  // <-- add this line
                ) {
                    itemsIndexed(paddedValues) { _, item ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(itemHeightDp),
                            contentAlignment = Alignment.Center
                        ) {
                            item?.let {
                                Text(
                                    text = "$it",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = -itemHeightDp / 2),
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = itemHeightDp / 2)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val index = (listState.firstVisibleItemIndex + centerIndex)
                    .coerceIn(2, paddedValues.size - 3)
                paddedValues[index]?.let { onConfirm(it) }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditableSettingDialog(
    title: String,
    initialValue: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    extraValidation: ((Int) -> String?)? = null
) {
    var input by remember { mutableStateOf(initialValue.toString()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        if (it.matches(Regex("^-?\\d*\$"))) {
                            input = it
                            error = null
                        }
                    },
                    label = { Text("Enter value") },
                    isError = error != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val number = input.toIntOrNull()
                            error = when {
                                number == null -> "Please enter a valid number"
                                extraValidation != null -> extraValidation(number)
                                else -> null
                            }
                            if (error == null) {
                                onConfirm(number!!)
                                onDismiss()
                            }
                        }
                    )
                )

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val number = input.toIntOrNull()
                error = when {
                    number == null -> "Please enter a valid number"
                    extraValidation != null -> extraValidation(number)
                    else -> null
                }
                if (error == null) {
                    onConfirm(number!!)
                    onDismiss()
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SettingItem(
    title: String,
    value: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f)) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            value?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SupportDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    val phone = "+91 98984 58844"
    val email = "pulkit@bizorbit.co.in"
    val web = "www.BizOrbit.co.in"

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Support") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.bizorbit_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(80.dp)
                        .padding(bottom = 8.dp)
                )

                Text(
                    "BizOrbit Technologies",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "10/G, Bodycare Complex,\nNr. Supath 2 Complex,\nAshram Road, Ahmedabad - 380013,\nGujarat, India",
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                ClickableText(
                    text = AnnotatedString(
                        "Phone : $phone",
                        spanStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ClickableText(
                    text = AnnotatedString(
                        "Email : $email",
                        spanStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$email")
                        }
                        context.startActivity(intent)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                ClickableText(
                    text = AnnotatedString(
                        "Web : $web",
                        spanStyle = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    onClick = {
                        val url = "https://$web"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Close")
            }
        }
    )
}

