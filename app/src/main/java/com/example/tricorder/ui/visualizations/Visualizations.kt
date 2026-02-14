package com.example.tricorder.ui.visualizations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tricorder.ui.theme.LcarsBlue
import com.example.tricorder.ui.theme.LcarsOrange
import com.example.tricorder.ui.theme.LcarsPurple
import com.example.tricorder.ui.theme.LcarsRed
import com.example.tricorder.viewmodel.SensorData

@Composable
fun SensorGraph(
    dataHistory: List<Triple<Float, Float, Float>>,
    modifier: Modifier = Modifier,
    xColor: Color = LcarsOrange,
    yColor: Color = LcarsBlue,
    zColor: Color = LcarsPurple,
    magColor: Color = LcarsRed
) {
    Canvas(modifier = modifier.fillMaxSize().background(Color.Black)) {
        if (dataHistory.isEmpty()) return@Canvas

        val width = size.width
        val height = size.height
        val maxPoints = 100 // adjust as needed
        val stepX = width / (maxPoints - 1)

        // Helper to get magnitude from Triple
        fun magnitude(t: Triple<Float, Float, Float>): Float {
            return kotlin.math.sqrt(t.first * t.first + t.second * t.second + t.third * t.third)
        }

        // Scaling factor (auto-scale or fixed?)
        val maxY = dataHistory.maxOfOrNull { maxOf(it.first, it.second, it.third, magnitude(it)) } ?: 10f
        val minY = dataHistory.minOfOrNull { minOf(it.first, it.second, it.third, magnitude(it)) } ?: -10f
        val range = (maxY - minY).coerceAtLeast(1f)
        
        fun normalize(value: Float): Float {
            return height - ((value - minY) / range * height)
        }

        val pathX = Path()
        val pathY = Path()
        val pathZ = Path()
        val pathMag = Path()

        dataHistory.takeLast(maxPoints).forEachIndexed { index, data ->
            val xPos = index * stepX
            val mag = magnitude(data)
            
            if (index == 0) {
                pathX.moveTo(xPos, normalize(data.first))
                pathY.moveTo(xPos, normalize(data.second))
                pathZ.moveTo(xPos, normalize(data.third))
                pathMag.moveTo(xPos, normalize(mag))
            } else {
                pathX.lineTo(xPos, normalize(data.first))
                pathY.lineTo(xPos, normalize(data.second))
                pathZ.lineTo(xPos, normalize(data.third))
                pathMag.lineTo(xPos, normalize(mag))
            }
        }

        drawPath(pathX, xColor, style = Stroke(width = 3f))
        drawPath(pathY, yColor, style = Stroke(width = 3f))
        drawPath(pathZ, zColor, style = Stroke(width = 3f))
        drawPath(pathMag, magColor, style = Stroke(width = 5f))
        
        // Grid lines
        drawLine(Color.Gray, start = Offset(0f, height/2), end = Offset(width, height/2), strokeWidth = 1f, alpha = 0.5f)
    }
}

@Composable
fun SensorValueDisplay(
    label: String,
    value: Float,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            color = color,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.width(30.dp)
        )
        Text(
            text = String.format("%.2f", value),
            color = color,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.width(8.dp))
        // Simple bar
        Box(
            modifier = Modifier
                .height(10.dp)
                .width(100.dp)
                .background(Color.DarkGray)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((100 * (value / 20f).coerceIn(0f, 1f)).dp) // visual scaling logic could be better
                    .background(color)
            )
        }
    }
}
