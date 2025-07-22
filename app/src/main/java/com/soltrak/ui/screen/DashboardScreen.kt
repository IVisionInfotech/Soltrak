package com.soltrak.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soltrak.R
import com.soltrak.ui.theme.*

import java.util.Calendar

@Composable
fun DashboardScreen(
    isLoading: Boolean,
    progressPercent: Int,
    importStatusText: String,
    onPickFile: () -> Unit,
    onScanBarcode: (serialNo: String) -> Unit,
    onScanRFID: () -> Unit,
    onExport: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            DashboardTopBar(
                profileName = "BizOrbit Technologies",
                profileImageRes = R.drawable.bizorbit_logo,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                EnergyUsageCircle(
                    percentage = progressPercent,
                    isLoading = isLoading,
                    importStatusText = importStatusText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            DeviceGrid(
                onPickFile = onPickFile,
                onScanBarcode = onScanBarcode,
                onScanRFID = onScanRFID,
                onExport = onExport
            )
        }
    }
}


@Composable
fun DashboardTopBar(
    profileName: String,
    profileImageRes: Int,
    onSettingsClick: () -> Unit
) {
    val currentHour = remember {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    val greeting = remember(currentHour) {
        when (currentHour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }

    val emoji = when (greeting) {
        "Good Morning" -> "\uD83D\uDD06"
        "Good Afternoon" -> "\u2600\uFE0F"
        "Good Evening" -> "\uD83C\uDF1D"
        else -> "\uD83C\uDF03"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = profileImageRes),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .size(50.dp)
                    .background(color = Color.White)
                    .border(1.dp, ProfileBorder)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextGray, fontSize = 14.sp)) {
                            append("$greeting ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFFFFCC80), fontSize = 14.sp)) {
                            append(emoji)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun EnergyUsageCircle(percentage: Int, isLoading: Boolean = false, importStatusText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        val sweepAngle = percentage * 3.6f

        // Background Circle
        Canvas(modifier = Modifier.fillMaxSize(0.8f)) {
            drawCircle(
                color = CardBackground,
                radius = size.minDimension / 2,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // Progress Arc
        Canvas(modifier = Modifier.fillMaxSize(0.8f)) {
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryOrange, DarkOrange),
                    startY = 0f,
                    endY = size.height
                ),
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Import Data",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp),
                    strokeWidth = 4.dp,
                    color = PrimaryOrange
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$percentage%",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = importStatusText,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp
                    )
                )
            } else {
                Text(
                    text = "$percentage%",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun DeviceGrid(
    onPickFile: () -> Unit,
    onScanBarcode: (serialNo: String) -> Unit,
    onScanRFID: () -> Unit,
    onExport: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.data_import,
                deviceName = "Import Data",
                onClick = onPickFile
            )
            DeviceCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.barcode,
                deviceName = "Scan Barcode",
                onClick = { onScanBarcode("sampleBarcode123") }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.rfid,
                deviceName = "Scan RFID",
                onClick = onScanRFID
            )
            DeviceCard(
                modifier = Modifier.weight(1f),
                iconRes = R.drawable.data_import,
                deviceName = "Export Data",
                onClick = onExport
            )
        }
    }
}

@Composable
fun DeviceCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    deviceName: String,
    onClick: () -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = deviceName,
                modifier = Modifier.size(30.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface)
            )
            Text(
                text = deviceName,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                fontSize = 14.sp,
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DashboardScreenPreview() {
    SoltrakTheme {
        DashboardScreen(
            isLoading = false,
            progressPercent = 65,
            importStatusText = "In Progress...",
            onPickFile = {},
            onScanBarcode = {},
            onScanRFID = {},
            onExport = {},
            onSettingsClick = {}
        )
    }
}
