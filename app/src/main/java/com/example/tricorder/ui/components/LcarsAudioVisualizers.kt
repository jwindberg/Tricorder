package com.example.tricorder.ui.visualizations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun LcarsWaveformGraph(
    dataPoints: FloatArray,
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw Grid
        drawRect(color = gridColor, style = Stroke(width = 2f))
        drawLine(color = gridColor, start = Offset(0f, h / 2), end = Offset(w, h / 2), strokeWidth = 1f)

        // Draw Waveform
        if (dataPoints.isNotEmpty()) {
            val path = Path()
            val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)
            
            // Normalize: data is -1.0 to 1.0 (approx)
            // Center is h/2. Scale is h/2.
            
            for (i in dataPoints.indices) {
                val x = i * stepX
                // Invert Y because canvas Y is down
                val y = (h / 2) - (dataPoints[i] * (h / 2))
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            drawPath(path = path, color = plotColor, style = Stroke(width = 2f))
        }
    }
}

@Composable
fun LcarsSpectrumGraph(
    dataPoints: FloatArray,
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw Grid
        drawRect(color = gridColor, style = Stroke(width = 2f))

        // Draw Spectrum (Bar Graph styled)
        if (dataPoints.isNotEmpty()) {
            val stepX = w / dataPoints.size
            val maxVal = 50f // Arbitrary max magnitude for normalization? Need to be adaptive or set.
            
            for (i in dataPoints.indices) {
                val magnitude = dataPoints[i]
                val normalizedHeight = (magnitude / maxVal).coerceIn(0f, 1f) * h
                
                val x = i * stepX
                val y = h - normalizedHeight
                
                drawRect(
                    color = plotColor,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(stepX * 0.8f, normalizedHeight)
                )
            }
        }
    }
}
