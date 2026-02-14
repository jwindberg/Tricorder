package com.example.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import com.example.tricorder.ui.theme.LcarsSourceColors

@Composable
fun LcarsBargraph(
    value: Float, // Current value
    max: Float, // Max range
    segments: Int, // Number of grid lines
    gridColor: Color,
    plotColor: Color,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            
            // 1. Draw Outline
            drawRect(
                color = gridColor,
                style = Stroke(width = 2.dp.toPx())
            )
            
            // 2. Draw Fill (Horizontal Bar - Left to Right)
            val fillWidth = (value / max).coerceIn(0f, 1f) * w
            drawRect(
                color = plotColor,
                topLeft = Offset(0f, 0f),
                size = Size(fillWidth, h)
            )
            
            // 3. Draw Grid Lines
            if (segments > 0) {
                val step = w / segments
                for (i in 1 until segments) {
                    val x = i * step
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, h),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
        }
        
        if (label != null) {
            Text(
                text = label,
                color = gridColor,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}
