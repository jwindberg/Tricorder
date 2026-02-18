package com.marsraver.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsraver.tricorder.ui.theme.LcarsSourceColors

// Constants based on Tricorder.java (minDim / 64)
// Assuming minDim ~ 480dp -> sideBarWidth ~ 7.5dp
val SIDE_BAR_WIDTH = 8.dp

// Port of HeaderBarElement.java (Internal Header Bracket)
@Composable
fun LcarsHeaderBarElement(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    textSize: androidx.compose.ui.unit.TextUnit = 18.sp // Smaller font as requested
) {
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val sbW = SIDE_BAR_WIDTH.toPx()
            val swoopW = sbW * 3
            val swoopH = sbW * 2
            val gutterH = sbW // Reduced from sbW * 2 (16dp) to 8dp to prevent text clipping
            
            // ... (Drawing code unchanged) ...
            
            // Outer Curve Left
            val outerLeft = Rect(0f, 0f, swoopW * 2, swoopH * 2)
            
            val outerRight = Rect(w - swoopW * 2, 0f, w, swoopH * 2)
            
            val innerRight = Rect(w - sbW - swoopW * 2, h - gutterH, w - sbW, h)
            
            val innerLeft = Rect(sbW, h - gutterH, sbW + swoopW * 2, h)
            
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(0f, swoopH)
                arcTo(outerLeft, 180f, 90f, false)
                lineTo(w - swoopW, 0f)
                arcTo(outerRight, 270f, 90f, false)
                lineTo(w, h)
                lineTo(w - sbW, h)
                lineTo(w - sbW, h - gutterH / 2)
                arcTo(innerRight, 0f, -90f, false)
                lineTo(sbW + swoopW, h - gutterH)
                arcTo(innerLeft, 270f, -90f, false)
                lineTo(sbW, h)
                close()
            }
            
            drawPath(path, color)
        }
        
        // Text
        Text(
            text = text,
            color = LcarsSourceColors.COL_TEXT_BLACK,
            fontSize = textSize,
            fontWeight = FontWeight.Bold, // Added Bold
            fontFamily = FontFamily.Monospace,
            style = androidx.compose.ui.text.TextStyle(
                platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                    includeFontPadding = false
                )
            ),
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.Center)
                .offset(y = (-3).dp) // Lift text slightly to center visually in the bracket
        )
    }
}

// Port of EllAtom.java (Corner piece)
@Composable
fun LcarsEll(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val sbW = SIDE_BAR_WIDTH.toPx() // barThickness
        val straights = 4f // STRAIGHTS
        
        // EllAtom layout:
        // Outer Curve: RectF(l, b - 2*ch, l + 2*cw, b)
        // cw = w - straights. ch = h - straights.
        // It seems to fill the Bottom-Left corner.
        
        val cw = w - straights
        val ch = h - straights
        
        val outerCurve = Rect(0f, h - ch * 2, cw * 2, h)
        // Inner Curve: RectF(sbW, b - 2*ch + sbW, 2*cw - sbW, b - sbW)
        val innerCurve = Rect(sbW, h - ch * 2 + sbW, cw * 2 - sbW, h - sbW)
        
        val path = Path().apply {
            moveTo(0f, 0f) // t
            lineTo(0f, straights) // t + straights
            arcTo(outerCurve, 180f, -90f, false) // West to South (CCW)? 
            // 180 is West. -90 is CCW. 180 -> 90? No 180-90 = 90 (South).
            // Wait. Standard: 0=East, 90=South, 180=West, 270=North.
            // CCW from 180 is towards South (270? No 90 is South).
            // 180 -> 270 is CW (West to North). 180 -> 90 is CCW (West to South).
            // So this arc goes from Leftdown to Bottom?
            
            lineTo(w, h)
            lineTo(w, h - sbW)
            lineTo(w - straights, h - sbW)
            arcTo(innerCurve, 90f, 90f, false) // South to West (CW)? 
            // 90 is South. +90 is CW to West (180).
            // Creates the inner curve.
            lineTo(sbW, 0f)
            close()
        }
        
        drawPath(path, color)
    }
}

// Port of Axis2DAtom.java (Solid Bar Graph + Cross)
@Composable
fun LcarsAxis2D(
    xValue: Float,
    yValue: Float,
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2
        val centerY = h / 2
        
        // Grid (Crosshair)
        drawLine(gridColor, Offset(centerX, 0f), Offset(centerX, h), 1.dp.toPx())
        drawLine(gridColor, Offset(0f, centerY), Offset(w, centerY), 1.dp.toPx())
        
        // Data Box (Unit Scale) - "if unitScale >= 1.0f"
        // We'll assume scale fits. Draws a box around center.
        // In source, unitScale was barLen / plotRange.
        // Let's draw a reference box at 50% or something?
        // Source: drawRect(crossX - unitScale, ...)
        // Let's just draw the main data for now.
        
        // Data Point (Solid Bar)
        // xValue, yValue are normalized -1 to 1 (relative to center)
        // Source: x = currentValues[0] * dataScale. 
        // We assume xValue is already scaled to -1..1 range of the View.
        // targetX = centerX + xValue * boxSize?
        // Let's assume input is normalized to the range [-1, 1].
        
        val targetX = xValue * (w / 2) // Relative offset
        val targetY = -yValue * (h / 2) // Y is inverted in screen coords
        
        val barPath = Path().apply {
            // Draw rect from center to target
            // It's a filled rect.
            // "canvas.drawRect(l, ..., r, ...)"
            // l = min(centerX, centerX + targetX)
            val l = minOf(centerX, centerX + targetX)
            val r = maxOf(centerX, centerX + targetX)
            val t = minOf(centerY, centerY + targetY)
            val b = maxOf(centerY, centerY + targetY)
            addRect(Rect(l, t, r, b))
        }
        
        drawPath(barPath, plotColor)
        
        // Cross Tip
        // At (centerX + targetX, centerY + targetY)
        val tipX = centerX + targetX
        val tipY = centerY + targetY
        val crossSize = 6.dp.toPx()
        
        drawLine(plotColor, Offset(tipX - crossSize, tipY), Offset(tipX + crossSize, tipY), 2.dp.toPx())
        drawLine(plotColor, Offset(tipX, tipY - crossSize), Offset(tipX, tipY + crossSize), 2.dp.toPx())
    }
}
