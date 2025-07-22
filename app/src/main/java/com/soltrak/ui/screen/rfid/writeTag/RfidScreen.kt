package com.soltrak.ui.screen.rfid.writeTag

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soltrak.R
import com.soltrak.ui.components.CommonTopAppBar

@Composable
fun RfidScreen(
    bankOptions: List<String>,
    epcValue: String?,
    tidValue: String?,
    passwordValue: String?,
    writeDateValue: String?,
    selectedBank: String?,
    showChooseBank: Boolean?,
    showPasswordLayout: Boolean?,
    showWriteLayout: Boolean?,
    onReadClicked: () -> Unit,
    onClearClicked: () -> Unit,
    onWriteClicked: () -> Unit,
    onPasswordClicked: () -> Unit,
    onBankSelected: (String) -> Unit,
    onPasswordValueChange: (String) -> Unit,
    onWriteDataValueChange: (String) -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "RFID Manager",
                onBackClick = onBackPressed
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 10.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { onReadClicked() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Read", fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = { onClearClicked() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear", fontSize = 20.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(15.dp)
            ) {
                Text(text = "EPC: ${epcValue ?: ""}", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "TID: ${tidValue ?: ""}", fontWeight = FontWeight.Medium)

                if (showChooseBank == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Choose Bank:", fontSize = 22.sp)
                    DropdownMenuBox(
                        options = bankOptions,
                        selectedOption = selectedBank ?: "",
                        onOptionSelected = onBankSelected
                    )
                }

                if (showPasswordLayout == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Password:", fontSize = 22.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = passwordValue ?: "00000000",
                            onValueChange = onPasswordValueChange,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            textStyle = LocalTextStyle.current.copy(fontSize = 22.sp)
                        )
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onPasswordClicked() }
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ocr_button_background),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                            Image(
                                painter = painterResource(id = R.drawable.ok),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                if (showWriteLayout == true) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Write Data:", fontSize = 22.sp)
                    TextField(
                        value = writeDateValue ?: "",
                        onValueChange = onWriteDataValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(fontSize = 20.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onWriteClicked,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Write", fontSize = 20.sp)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun DropdownMenuBox(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text("Select Bank") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = true }
                )
            }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
