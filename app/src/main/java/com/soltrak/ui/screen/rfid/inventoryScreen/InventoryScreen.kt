package com.soltrak.ui.screen.rfid.inventoryScreen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.soltrak.ui.components.CommonTopAppBar
import com.soltrak.ui.theme.DarkOrange
import com.soltrak.ui.theme.TextWhite

data class RfidTag(val epc: String, val tid: String, val count: Int)

@Composable
fun InventoryScreen(
    tagList: List<RfidTag>,
    isScanning: Boolean,
    currentMode: String,
    onBackPressed: () -> Unit,
    onStartStopToggle: () -> Unit,
    onReset: () -> Unit,
    onModeChange: (String) -> Unit,
    onItemClick: (RfidTag) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            var isMenuExpanded by remember { mutableStateOf(false) }

            CommonTopAppBar(
                title = "Inventory",
                onBackClick = onBackPressed,
                onResetClick = onReset,
                menuContent = {
                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = TextWhite
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = currentMode == "EPC",
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("EPC")
                                    }
                                },
                                onClick = {
                                    onModeChange("EPC")
                                    isMenuExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = currentMode == "TID",
                                            onClick = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("TID")
                                    }
                                },
                                onClick = {
                                    onModeChange("TID")
                                    isMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onStartStopToggle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkOrange)
            ) {
                Text(if (isScanning) "STOP" else "START")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Header Counts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox("Unique Tags", tagList.size)
                StatBox("Total Reads", tagList.sumOf { it.count })
            }

            // Tag List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(tagList) { tag ->
                    TagItem(
                        tag,
                        currentMode,
                        isEnabled = !isScanning,
                        onClick = { onItemClick(tag) })
                }
            }
        }
    }
}

@Composable
fun StatBox(title: String, value: Int) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .background(Color(0xFF64B5F6), shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = Color.White)
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White
        )
    }
}

@Composable
fun TagItem(
    tag: RfidTag,
    currentMode: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val displayValue = if (currentMode == "TID") tag.tid else tag.epc

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .let {
                if (isEnabled) {
                    it.clickable {
                        if (displayValue.isNullOrEmpty()) {
                            Toast.makeText(context, "Tag data is missing", Toast.LENGTH_SHORT).show()
                        } else {
                            onClick()
                        }
                    }
                } else it
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = displayValue.ifEmpty { "N/A" }, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Count: ${tag.count}", color = Color.Blue)
        }
    }
}


