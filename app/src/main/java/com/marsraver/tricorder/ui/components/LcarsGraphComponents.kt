package com.marsraver.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clipToBounds
import com.marsraver.tricorder.ui.theme.LcarsSourceColors

// Port of GaugeAtom.java
@Composable
fun LcarsGauge(
    level: Float, // 0.0 to 1.0 (normalized for simplicity in this port)
    color: Color,
    gridColor: Color = LcarsSourceColors.COL_BG, // Assuming a grid/bg color
    isVertical: Boolean = true,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // drawBody() logic from GaugeAtom.java
        // Draw Background
        drawRect(color = gridColor)
        
        // Draw Grid Lines (Simplified port: drawn every unitScale)
        // In source: y < plotRange * unitScale. Here we assume normalized 0-1
        val step = if (isVertical) h / 10f else w / 10f
        
        // Draw Value Bar/Pointer
        // GaugeAtom draws a "pointerPath" which is a triangle/arrow shape
        // For this port, we'll implement a solid bar for the value to match the visual look typically expected
        // If exact pointer needed involving Path:
        /*
        val pointerPath = Path().apply { ... }
        drawPath(pointerPath, color)
        */
        
        // Simple Bar Implementation for now as per typical LCARS
        if (isVertical) {
            val barH = h * level
            drawRect(
                color = color,
                topLeft = Offset(0f, h - barH),
                size = androidx.compose.ui.geometry.Size(w, barH)
            )
        } else {
            val barW = w * level
            drawRect(
                color = color,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(barW, h)
            )
        }
    }
}

// Port of AxisElement.java (Axis2DAtom)
@Composable
fun LcarsAxisGraph(
    xValue: Float, // Normalized -1 to 1
    yValue: Float, // Normalized -1 to 1
    zValue: Float, // Normalized -1 to 1 (for Z bar)
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val centerX = w / 2
        val centerY = h / 2
        
        // Draw Grid (Crosshair)
        drawLine(
            color = gridColor,
            start = Offset(centerX, 0f),
            end = Offset(centerX, h),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = gridColor,
            start = Offset(0f, centerY),
            end = Offset(w, centerY),
            strokeWidth = 1.dp.toPx()
        )
        
        // Plot Value (Line from Center)
        // xValue, yValue are -1 to 1. 
        // x: -1 is Left, 1 is Right. y: -1 is Bottom, 1 is Top (standard math) or inverse?
        // Android Canvas Y is down. So 1 should be Top (-y).
        
        val targetX = centerX + (xValue * centerX)
        val targetY = centerY - (yValue * centerY)
        
        drawLine(
            color = plotColor,
            start = Offset(centerX, centerY),
            end = Offset(targetX, targetY),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw Z Bar (usually distinct, but part of AxisElement visual in source)
        // Implemented separately in UI layout usually, but drawing here for debugging if needed
    }
}

// Port of MagnitudeElement.java (ChartAtom) - Scrolling Chart
@Composable
fun LcarsChartGraph(
    dataPoints: List<Float>, // Normalized 0-1
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.background(Color.Black).clipToBounds()) {
        val w = size.width
        val h = size.height
        
        // Draw Grid Lines (Horizontal)
        val lines = 5
        for (i in 0..lines) {
            val y = h * (i / lines.toFloat())
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
        }
        
        // Plot Data
        if (dataPoints.size > 1) {
            val stepX = w / (dataPoints.size - 1).coerceAtLeast(1)
            
            val path = Path()
            dataPoints.forEachIndexed { index, rawValue ->
                val value = rawValue.coerceIn(0f, 1f) // Clamp to prevent drawing out of bounds
                val x = index * stepX
                val y = h - (value * h) // 0 is bottom
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            
            drawPath(
                path = path,
                color = plotColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

// Strict Port of Axis2DAtom.java
@Composable
fun LcarsAxis2DAtom(
    x: Float, // Normalized: 1.0 = 1 Unit (Grid Box Edge)
    y: Float, // Normalized: 1.0 = 1 Unit
    gridColor: Color,
    plotColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        
        // Axis2DAtom Logic:
        // Crosshairs extend to full available space (Range, e.g. 2g)
        // Grid Box represents 1 Unit (e.g. 1g)
        // Typically Range = 2 * Unit.
        
        val barLen = minOf(cx, cy)
        val unitRadius = barLen * 0.5f 
        
        // 1. Draw Grid Boundary Rect (The "Box" representing 1 Unit)
        drawRect(
            color = gridColor,
            topLeft = Offset(cx - unitRadius, cy - unitRadius),
            size = androidx.compose.ui.geometry.Size(unitRadius * 2, unitRadius * 2),
            style = Stroke(width = 1.dp.toPx())
        )
        
        // 2. Center Cross Lines (Full Range)
        drawLine(gridColor, Offset(cx, cy - barLen), Offset(cx, cy + barLen), 1.dp.toPx())
        drawLine(gridColor, Offset(cx - barLen, cy), Offset(cx + barLen, cy), 1.dp.toPx())

        // 3. Plot Value
        // Input x,y are normalized to Unit (1.0 = Box Edge).
        val tx = cx + (x * unitRadius)
        val ty = cy - (y * unitRadius)
        
        // Draw Rectangle from Center to Point
        // logic: min/max to define rect
        val left = minOf(cx, tx)
        val top = minOf(cy, ty)
        val right = maxOf(cx, tx)
        val bottom = maxOf(cy, ty)
        
        drawRect(
            color = plotColor,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top)
        )
        
        // Cross at Data Point
        val crossSize = 6.dp.toPx()
        drawLine(plotColor, Offset(tx - crossSize, ty), Offset(tx + crossSize, ty), 2.dp.toPx())
        drawLine(plotColor, Offset(tx, ty - crossSize), Offset(tx, ty + crossSize), 2.dp.toPx())
    }
}

enum class GaugeOrientation { LEFT, RIGHT, TOP, BOTTOM }

// Strict Port of GaugeAtom.java
// Strict Port of GaugeAtom.java
@Composable
fun LcarsGaugeAtom(
    value: Float, // 0 to 1
    color: Color, // Pointer Color
    gridColor: Color = LcarsSourceColors.COL_BG, // Bar Color
    orientation: GaugeOrientation = GaugeOrientation.LEFT,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Source defaults: barThickness is sidebar width.
        // We will derive barThickness from the narrow dimension of the allocated space 
        // OR assume fixed 12.dp if we want to enforce it, but here we adapt to layout.
        // Source: GaugeAtom uses getSidebarWidth() -> barThickness. 
        // In onDraw, uses barWidth/barHeight which are set in setGeometry.
        
        val barThickness = 6.dp.toPx() 
        // Strict logic: Bar is barThickness wide.
        
        // Determine Bar Rect and Pointer Geometry
        var barRect = Rect(0f, 0f, 0f, 0f)
        var pointerStart = Offset(0f, 0f)
        var pointerEnd = Offset(0f, 0f) // Tip direction (relative)
        var pointerBaseHalf = Offset(0f, 0f) // Perpendicular to direction
        
        // Pointer Dimensions
        val p = barThickness * 2f // Length
        val s = barThickness * 2f / 3f // Half-width of base
        val b = barThickness / 2f // Center of bar
        
        when (orientation) {
            GaugeOrientation.LEFT -> {
                // Bar on Left, Pointer points Right
                barRect = Rect(0f, 0f, barThickness, h)
                // Pointer Y varies with value (Top = 0 -> value 1 means Top? Source: h - val*h)
                // Source: yoff = (top + datum) - sval. Datum for Vertical is Bottom (barLength - 1). 
                // Checks logic: y = h - (value * h).
                val py = h - (value * h)
                
                // Pointer: Moves from center of bar (b) to right (b+p)
                // Triangle: (b, py-s), (b+p, py), (b, py+s)
                // Actually source: moveTo(b, -s), lineTo(b+p, 0), lineTo(b, s). (Relative to tip at Y=0)
                
                // Logic:
                // Tip is at X = b + p. Base is at X = b.
                // It points RIGHT.
                // Wait. Source: layoutXY places yGauge at LEFT. 
                // Does it point INTO the graph? Yes. Graph is to the Right.
                // So Pointer points RIGHT.
                
                // Draw Pointer
                val baseX = b
                val tipX = b + p
                
                val path = Path().apply {
                    moveTo(baseX, py - s)
                    lineTo(tipX, py)
                    lineTo(baseX, py + s)
                    close()
                }
                drawRect(gridColor, topLeft = barRect.topLeft, size = barRect.size)
                drawPath(path, color) // Solid Pointer
                drawPath(path, LcarsSourceColors.COL_BG, style = Stroke(width = 1.5f)) // Outline
            }
            GaugeOrientation.RIGHT -> {
                // Bar on Right, Pointer points Left
                barRect = Rect(w - barThickness, 0f, w, h)
                val py = h - (value * h)
                
                // Pointer: Center of bar (w - b) pointing Left to (w - b - p)
                val baseX = w - b
                val tipX = w - b - p
                
                val path = Path().apply {
                    moveTo(baseX, py - s)
                    lineTo(tipX, py)
                    lineTo(baseX, py + s)
                    close()
                }
                drawRect(gridColor, topLeft = barRect.topLeft, size = barRect.size)
                drawPath(path, color)
                drawPath(path, LcarsSourceColors.COL_BG, style = Stroke(width = 1.5f))
            }
            GaugeOrientation.BOTTOM -> {
                // Bar on Bottom, Pointer points Up
                barRect = Rect(0f, h - barThickness, w, h)
                val px = value * w // Horizontal: 0 is Left.
                
                // Pointer: Center of bar (h - b) pointing Up to (h - b - p)
                val baseY = h - b
                val tipY = h - b - p
                
                val path = Path().apply {
                    moveTo(px - s, baseY)
                    lineTo(px, tipY)
                    lineTo(px + s, baseY)
                    close()
                }
                drawRect(gridColor, topLeft = barRect.topLeft, size = barRect.size)
                drawPath(path, color)
                drawPath(path, LcarsSourceColors.COL_BG, style = Stroke(width = 1.5f))
            }
            GaugeOrientation.TOP -> {
                // Bar on Top, Pointer points Down
                barRect = Rect(0f, 0f, w, barThickness)
                val px = value * w
                
                // Pointer: Center of bar (b) pointing Down (b + p)
                val baseY = b
                val tipY = b + p
                
                val path = Path().apply {
                    moveTo(px - s, baseY)
                    lineTo(px, tipY)
                    lineTo(px + s, baseY)
                    close()
                }
                drawRect(gridColor, topLeft = barRect.topLeft, size = barRect.size)
                drawPath(path, color)
                drawPath(path, LcarsSourceColors.COL_BG, style = Stroke(width = 1.5f))
            }
        }
    }
}

// Strict Port of DialAtom.java (Arc with Pointer)
// Orientation.RIGHT -> Left Half Circle
@Composable
fun LcarsDialAtom(
    angle: Float, // Degrees.
    gridColor: Color, // Arc and Line Color (Lavender)
    plotColor: Color, // Pointer Color (Red)
    strokeWidth: Dp = 6.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // DialAtom Orientation.RIGHT (Case 3):
        // Arc Center is at Right Edge. Arc draws Left (90 to 270).
        val cx = w 
        val cy = h / 2
        val radius = w.coerceAtMost(h / 2)
        
        // Draw Arc
        drawArc(
            color = gridColor,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(cx - radius, cy - radius),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth.toPx())
        )
        
        // Draw Center Line
        drawLine(gridColor, Offset(cx, cy), Offset(cx - radius, cy), 1f)
        
        // Draw Pointer (Triangle)
        // Rotation: 
        // arcCentre (180) + value.
        // If angle is 0, it points Left (180).
        // If angle is -90, it points Down (90).
        // If angle is +90, it points Up (270).
        
        val rot = 180f + angle // Match source logic
        val rad = Math.toRadians(rot.toDouble())
        
        // Pointer is at the edge of the arc?
        // Source: pointerPath.moveTo(radius, 0); lineTo(radius-p, -s); lineTo(radius-p, s).
        // This defines a triangle pointing OUTWARD (Tip at radius, Base at radius-p).
        // Rotated around (0,0) (which is mapped to CentreX, CentreY).
        
        val tipX = cx + (radius * kotlin.math.cos(rad)).toFloat()
        val tipY = cy + (radius * kotlin.math.sin(rad)).toFloat()
        
        val p = strokeWidth.toPx() * 2f // Length
        val s = strokeWidth.toPx() * 2f / 3f  // Half width of arrow base
        
        // Base center point (radius - p)
        val baseRad = radius - p
        val baseX = cx + (baseRad * kotlin.math.cos(rad)).toFloat()
        val baseY = cy + (baseRad * kotlin.math.sin(rad)).toFloat()
        
        // We need the two base corners. They are perpendicular to the radius vector.
        // Perpendicular vector (-sin, cos)
        val perpX = -kotlin.math.sin(rad).toFloat()
        val perpY = kotlin.math.cos(rad).toFloat()
        
        val c1x = baseX + (perpX * s)
        val c1y = baseY + (perpY * s)
        val c2x = baseX - (perpX * s)
        val c2y = baseY - (perpY * s)
        
        // Draw Triangle
        val path = Path().apply {
            moveTo(tipX, tipY)
            lineTo(c1x, c1y)
            lineTo(c2x, c2y)
            close()
        }
        
        drawPath(path, plotColor) // Use plotColor (Red) for Pointer
    }
}

// Strict Port of EllAtom.java (Corner)
@Composable
fun LcarsEllAtom(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val thick = 6.dp.toPx() // Standard bar thickness estimate
        // EllAtom draws a corner: Top-Left to Bottom-Right typically?
        // Source: moveTo(l, t); lineTo(l, t+S); arcTo(outer)...
        // It's a bracket shape.
        // For AxisElement layout: Ell is at Bottom-Left (below Y-gauge, left of X-gauge).
        // It should connect the Left Vertical bar to the Bottom Horizontal bar.
        // So it's a "Bottom-Left Corner".
        
        val path = Path().apply {
            // Outer Corner (Bottom-Left)
            // Start at Top-Left of this cell (Top of vertical segment)
            moveTo(0f, 0f)
            lineTo(0f, h) // Down to bottom-left corner?
            lineTo(w, h) // Right to bottom-right (start of horiz bar)
            
            // Inner Corner
            lineTo(w, h - thick)
            // Arc? Or Line? Source uses arcTo.
            // Let's simplified L-shape for robustness first unless strictly curved needed.
            // Source: arcTo(outerCurve, 180, -90). 180 is Left. -90 is CounterClockwise to Top.
            // So it curves from Left to Top?
            // "Bottom-Left Corner" usually curves from Top (Vertical) to Right (Horizontal).
            
            lineTo(thick, h - thick)
            lineTo(thick, 0f)
            close()
        }
        drawPath(path, color)
    }
}

// Port of Num3DElement.java
@Composable
fun LcarsNum3D(
    numericValue: String,
    label: String,
    modifier: Modifier = Modifier
) {
    // This is text-heavy, not canvas heavy, but part of the graph package
    // For strictly canvas porting, we would use drawText on Canvas (native android)
    // But usage of Compose Text is acceptable and cleaner for text.
    // If strict pixel matching required, native Canvas text drawing:
    
    // For now, using standard Compose column as it's cleaner to read/maintain 
    // and likely indistinguishable from a static native text draw in this context.
    Column(modifier = modifier) {
        androidx.compose.material3.Text(
            text = label,
            color = LcarsSourceColors.COL_GRA, // Defaulting
            fontSize = 12.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
         androidx.compose.material3.Text(
            text = numericValue,
            color = LcarsSourceColors.COL_MAG, // Defaulting
            fontSize = 24.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        )
    }
}
