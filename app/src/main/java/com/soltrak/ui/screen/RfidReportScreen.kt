package com.soltrak.ui.screen

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlin.math.ceil
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.soltrak.data.database.ModuleEntity
import com.soltrak.ui.components.CommonTopAppBar
import org.json.JSONArray

@Composable
fun RfidReportScreen(
    moduleEntity: ModuleEntity,
    onBackPressed: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val dataArray = JSONArray(moduleEntity.ivPointsJson)

    Scaffold(
        topBar = {
            CommonTopAppBar(
                title = "RFID Report",
                onBackClick = onBackPressed
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Solar PV Module",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
//                    Text(
//                        text = "Manufactured By:\n${moduleEntity.pvMfgr ?: "-"}\nAddress line 1,\nAddress line 2",
//                        fontStyle = FontStyle.Italic,
//                        fontSize = 14.sp
//                    )
                }
                Text(
                    text = "RFID REPORT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()

            val details = listOf(
                "Module Serial Number" to (moduleEntity.serialNo ?: "-"),
                "Model Type" to (moduleEntity.modType ?: "-"),
                "PV Module Manufacturer Name" to (moduleEntity.pvMfgr ?: "-"),
                "PV Module Origin Country" to "India",
                "Module Testing Date and Time" to (moduleEntity.createdTS ?: "-"),
                "Module Maximum Power Pmax (W)" to (moduleEntity.pMax ?: "-"),
                "Module Maximum Voltage Vmax (V)" to (moduleEntity.vMax ?: "-"),
                "Module Maximum Current Imax (A)" to (moduleEntity.iMax ?: "-"),
                "Module Open Circuit Voltage Voc (V)" to (moduleEntity.voc ?: "-"),
                "Module Short Circuit Current Isc (A)" to (moduleEntity.isc ?: "-"),
                "Module Efficiency (%)" to (moduleEntity.moduleEff ?: "-"),
                "Module Fill Factor (%)" to (moduleEntity.ff ?: "-"),
                "Test Lab (IEC)" to (moduleEntity.iecLab ?: "-"),
                "IEC Certificate Date" to (moduleEntity.createdTS ?: "-")
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Detailed Specification:", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(label, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    Text(value, modifier = Modifier.weight(1f), fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("I-V Curve:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            RfidReportWithChartFromJson(dataArray)
        }
    }
}

@Composable
fun RfidReportWithChartFromJson(dataArray: JSONArray) {
    val ivPoints = remember { mutableStateListOf<Offset>() }
    val vPoints = remember { mutableStateListOf<Offset>() }

    LaunchedEffect(dataArray) {
        ivPoints.clear()
        vPoints.clear()

        for (i in 0 until dataArray.length()) {
            val obj = dataArray.getJSONObject(i)
            val current = obj.optString("i").toFloatOrNull() ?: 0f
            val voltage = obj.optString("v").toFloatOrNull() ?: 0f
            val power = current * voltage / 43

            ivPoints.add(Offset(current, voltage))
            vPoints.add(Offset(current, power))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        SolarIVCurveChartFromOffsets(ivPoints, vPoints)
    }
}

@Composable
fun SolarIVCurveChartFromOffsets(
    ivPoints: List<Offset>,
    vPoints: List<Offset>,
    modifier: Modifier = Modifier
) {
    val xMax = ceil((ivPoints + vPoints).maxOfOrNull { it.x } ?: 50f)
    val yMax = ceil((ivPoints + vPoints).maxOfOrNull { it.y } ?: 14f)

    val padding = 60f

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(8.dp)
    ) {
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        drawLine(Color.Black, Offset(padding, padding), Offset(padding, size.height - padding), strokeWidth = 3f)
        drawLine(Color.Black, Offset(padding, size.height - padding), Offset(size.width - padding, size.height - padding), strokeWidth = 3f)

        val xStep = 5
        val xStepsCount = (xMax / xStep).toInt()
        val stepX = chartWidth / (xMax / xStep)

        for (i in 0..xStepsCount) {
            val x = padding + i * stepX
            drawLine(Color.LightGray, Offset(x, padding), Offset(x, size.height - padding), strokeWidth = 1f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i * xStep}",
                x,
                size.height - padding + 30f,
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = Paint.Align.CENTER
                }
            )
        }

        val yStep = 2
        val yStepsCount = (yMax / yStep).toInt()
        val stepY = chartHeight / (yMax / yStep)

        for (i in 0..yStepsCount) {
            val y = size.height - padding - i * stepY
            drawLine(Color.LightGray, Offset(padding, y), Offset(size.width - padding, y), strokeWidth = 1f)
            drawContext.canvas.nativeCanvas.drawText(
                "${i * yStep}",
                padding - 30f,
                y + 10f,
                Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val ivPath = Path().apply {
            smoothCurve(ivPoints, xMax, yMax, chartWidth, chartHeight, padding, size.height)
        }
        drawPath(ivPath, color = Color.Red, style = Stroke(width = 5f))

        val vpPath = Path().apply {
            smoothCurve(vPoints, xMax, yMax, chartWidth, chartHeight, padding, size.height)
        }
        drawPath(vpPath, color = Color.Blue, style = Stroke(width = 5f))

        drawContext.canvas.nativeCanvas.drawText(
            "Solar PV I-V & I-P Curves",
            size.width / 2,
            padding - 30f,
            Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 36f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
        )
    }
}

private fun Path.smoothCurve(
    points: List<Offset>,
    xMax: Float,
    yMax: Float,
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    canvasHeight: Float
) {
    if (points.size < 2) return

    val extendedPoints = points.toMutableList()
    val lastPoint = extendedPoints.last()
    extendedPoints.add(Offset(lastPoint.x, 0f))

    val mapped = extendedPoints.map {
        Offset(
            padding + (it.x / xMax) * chartWidth,
            canvasHeight - padding - (it.y / yMax) * chartHeight
        )
    }

    moveTo(mapped.first().x, mapped.first().y)

    for (i in 1 until mapped.size - 1) {
        val prev = mapped[i - 1]
        val current = mapped[i]
        val next = mapped[i + 1]

        val control1 = Offset((prev.x + current.x) / 2, (prev.y + current.y) / 2)
        val control2 = Offset((current.x + next.x) / 2, (current.y + next.y) / 2)

        cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
    }
}
