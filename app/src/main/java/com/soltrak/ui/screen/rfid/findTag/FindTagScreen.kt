package com.soltrak.ui.screen.rfid.findTag

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.soltrak.ui.components.CommonTopAppBar
import com.soltrak.ui.theme.DarkOrange
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.Dp

@Composable
fun FindTagScreen(
    tagId: String,
    isFinding: Boolean,
    rssi: Float,
    mode: String,
    entered: Boolean,
    yellowThreshold: Int,
    greenThreshold: Int,
    onBackPressed: () -> Unit,
    onTagIdChange: (String) -> Unit,
    onModeChange: (String) -> Unit,
    onEnterClick: () -> Unit,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "Find Tag",
                onBackClick = onBackPressed
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isFinding && !entered && tagId.isNotBlank()) {
                    Button(
                        onClick = onEnterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkOrange)
                    ) {
                        Text("Enter")
                    }
                }

                if (entered) {
                    if (!isFinding) {
                        Button(
                            onClick = {
                                onStartClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkOrange)
                        ) {
                            Text("Start Finding")
                        }
                    } else {
                        Button(
                            onClick = {
                                onStopClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkOrange)
                        ) {
                            Text("Stop Finding")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Radio Buttons
            Text("Select Mode:")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("EPC", "TID").forEach { value ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(enabled = !entered) { onModeChange(value) }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = mode == value,
                            onClick = { onModeChange(value) },
                            enabled = !entered
                        )
                        Text(text = value)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = tagId,
                onValueChange = onTagIdChange,
                label = { Text("Tag ID") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !entered
            )

            Text(
                text = "Length: ${tagId.length}",
                modifier = Modifier.align(Alignment.End),
                style = MaterialTheme.typography.labelSmall
            )

            if (entered && isFinding) {
                Spacer(modifier = Modifier.height(24.dp))
                RssiBar(
                    rssi = rssi,
                    yellowThreshold = yellowThreshold,
                    greenThreshold = greenThreshold
                )
            }
        }
    }
}

@Composable
fun RssiBar(
    rssi: Float,
    yellowThreshold: Int,
    greenThreshold: Int,
    modifier: Modifier = Modifier,
    maxWidth: Dp = 300.dp,
    barHeight: Dp = 20.dp,
    cornerRadius: Dp = 10.dp
) {
    val ratio = ((-rssi) / 50f).coerceIn(0f, 1f)

    val animatedRatio by animateFloatAsState(
        targetValue = ratio,
        animationSpec = tween(durationMillis = 100)
    )

    val barColor = when {
        rssi > greenThreshold -> Color.Green
        rssi > yellowThreshold -> Color.Yellow
        else -> Color.Red
    }

    val barWidth = maxWidth * animatedRatio

    Box(
        modifier = modifier
            .width(maxWidth)
            .height(barHeight)
            .background(color = Color.LightGray, shape = RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight()
                .background(color = barColor, shape = RoundedCornerShape(cornerRadius))
        )
    }
}

