package com.marsraver.tricorder.ui.visualizations

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
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw Grid
        drawRect(color = gridColor, style = Stroke(width = 2f))

        // Draw Spectrum
        if (dataPoints.isNotEmpty()) {
            val stepX = w / dataPoints.size
            // Logarithmic scalling for magnitude usually looks better, but source uses:
            // y = be - (((Math.log10(data[i]) / 6.0) + 1.0) * bh)
            // Range is roughly 6 Bels (60dB).
            
            // For simplicity in this replicate, let's stick to linear or simple log mapping
            // ensuring we map to the height.
            // Source: 0 is Red, 300 is Purple.
            
            for (i in dataPoints.indices) {
                // Hue from 0 (Red) to 300 (Purple)
                val hue = (i.toFloat() / dataPoints.size) * 300f
                val color = Color.hsv(hue, 1f, 1f)
                
                val magnitude = dataPoints[i]
                // Simple normalization for now, can be improved to match "6 Bels" logic if needed
                val normalizedHeight = (magnitude / 50f).coerceIn(0f, 1f) * h
                
                val x = i * stepX
                val y = h - normalizedHeight
                
                drawRect(
                    color = color,
                    topLeft = Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(stepX * 0.9f, normalizedHeight)
                )
            }
        }
    }
}

@Composable
fun LcarsPowerGraph(
    powerDb: Float,
    peakDb: Float,
    gridColor: Color,
    modifier: Modifier = Modifier
) {
    // Range -100 dB to 0 dB
    
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw Border
        drawRect(color = gridColor, style = Stroke(width = 2f))
        
        // Background Grid Lines
        val steps = 10
        val stepW = w / steps
        for (i in 1 until steps) {
            drawLine(
                color = gridColor.copy(alpha = 0.5f),
                start = Offset(i * stepW, 0f),
                end = Offset(i * stepW, h),
                strokeWidth = 1f
            )
        }
        
        // Bar Logic
        // Scale: -100 maps to 0 width, 0 maps to 100% width.
        // x = (db + 100) / 100 * w
        
        val normalize = { db: Float ->
            ((db.coerceIn(-100f, 0f) + 100) / 100f) * w
        }
        
        // Average Power Bar (Blue - Source: -16776961 0xFF0000FF)
        val powerW = normalize(powerDb)
        drawRect(
            color = Color.Blue, 
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(powerW, h)
        )
        
        // Peak Indicator (Red - Source: 0xFFFF0000)
        // In source, it fades. Here we'll just show it solid for specific peak
        val peakX = normalize(peakDb)
        if (peakX > 0) {
            drawRect(
                color = Color.Red,
                topLeft = Offset(peakX - 2f, 0f), // Small strip
                size = androidx.compose.ui.geometry.Size(4f, h)
            )
        }
    }
}
