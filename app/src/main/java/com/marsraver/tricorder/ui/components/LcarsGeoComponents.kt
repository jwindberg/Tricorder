package com.marsraver.tricorder.ui.components

import android.location.GnssStatus
import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Color as AndroidColor
import com.marsraver.tricorder.ui.theme.LcarsSourceColors

@Composable
fun LcarsGeoElement(
    title: String,
    location: Location?,
    status: String? = null, // e.g. "GPS Disabled"
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Header
        LcarsHeaderBarElement(
            text = title,
            color = LcarsSourceColors.COL_GEO,
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        if (location == null) {
            Text(
                text = status ?: "NO DATA",
                color = LcarsSourceColors.COL_BRIGHT_RED,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 8.dp)
            )
        } else {
            Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                // LAT (Line 1)
                Text(
                    text = "LAT: ${formatCoord(location.latitude, "N", "S")}",
                    color = LcarsSourceColors.COL_MAG,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                // LON (Line 2)
                Text(
                    text = "LON: ${formatCoord(location.longitude, "E", "W")}",
                    color = LcarsSourceColors.COL_MAG,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                // ALT / ACC (Line 3 - Side by Side)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ALT: ${if (location.hasAltitude()) "%.1f m".format(location.altitude) else "---"}",
                        color = LcarsSourceColors.COL_MAG,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "ACC: %.1f m".format(location.accuracy),
                        color = LcarsSourceColors.COL_MAG,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

fun formatCoord(value: Double, posDir: String, negDir: String): String {
    val absVal = Math.abs(value)
    val dir = if (value >= 0) posDir else negDir
    val deg = absVal.toInt()
    val min = (absVal - deg) * 60
    return "%d° %.3f' %s".format(deg, min, dir)
}

@Composable
fun LcarsSkyMap(
    gnssStatus: GnssStatus?,
    azimuth: Float, // Map rotation (Compass)
    modifier: Modifier = Modifier
) {
     Column(modifier = modifier) {
        LcarsHeaderBarElement(
            text = "Satellites",
            color = LcarsSourceColors.COL_GEO,
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val cx = size.width / 2
                val cy = size.height / 2
                val radius = size.width.coerceAtMost(size.height) / 2
                
                // Original SkyMapAtom Constants & Colors
                // COLOUR_MAG_NORTH = -226984 -> 0xFFFC8918 (Red/Orange)
                // COLOUR_MAG_SOUTH = -4669497 -> 0xFFB8B8C7 (Grey/Silver)
                // COLOUR_TRUE_NORTH = -8265488 -> 0xFF81E1F0 (Cyan/Teal)
                // COLOUR_RING_1 = -3877751 -> 0xFFC4D389 (Light Green/Yellow)
                // COLOUR_RING_2 = -5120329 -> 0xFFB1D9B7 (Sage Green)
                
                val colMagNorth = Color(0xFFFC8918)
                val colMagSouth = Color(0xFFB8B8C7)
                val colTrueNorth = Color(0xFF81E1F0)
                val colRing1 = Color(0xFFC4D389)
                val colRing2 = Color(0xFFB1D9B7)
                val colBackground = Color.Black
                
                val barWidth = radius / 7.0f
                val barBlock = radius / 7.2f
                val satRadius = 2.0f 
                
                val labelRadius = radius * 0.85f
                
                // --- LAYER 1: Rotated by Azimuth (True North Up relative to grid) ---
                rotate(degrees = -azimuth, pivot = Offset(cx, cy)) {
                    
                    // 1. Rings (Filled Circles based on original code)
                    val stroke = Stroke(width = 4.dp.toPx()) // Grid Width 4.0f
                    drawCircle(colRing1, radius, style = stroke)
                    drawCircle(colRing1, radius * 0.66f, style = stroke)
                    drawCircle(colRing1, radius * 0.33f, style = stroke)

                    // 2. Bars (Crosshairs - Thick Rects)
                    drawRect(
                        color = colRing1,
                        topLeft = Offset(cx - radius, cy - barWidth),
                        size = androidx.compose.ui.geometry.Size(radius * 2, barWidth * 2),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawRect(
                        color = colRing1,
                        topLeft = Offset(cx - barWidth, cy - radius),
                        size = androidx.compose.ui.geometry.Size(barWidth * 2, radius * 2),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    
                    // 3. True North Pointer (The "Complex Path")
                    val northPath = Path().apply {
                         val tipBaseY = (cy - barWidth) - (3.0f * barBlock)
                         val tipTipY = (cy - barWidth) - (4.2f * barBlock)
                         
                         moveTo(cx - (barWidth * 0.8f), tipBaseY) // Wider base
                         lineTo(cx, tipTipY)
                         lineTo(cx + (barWidth * 0.8f), tipBaseY)
                         close()
                         
                         // Add the 3 blocks below it
                         for (i in 0 until 3) {
                             val bH = barBlock * 0.8f
                             val bY = (cy - barWidth) - (i * barBlock) - bH
                             addRect(androidx.compose.ui.geometry.Rect(
                                 left = cx - (barWidth * 0.8f),
                                 top = bY,
                                 right = cx + (barWidth * 0.8f),
                                 bottom = bY + bH
                             ))
                         }
                    }
                    drawPath(northPath, colTrueNorth)
                    
                    // 4. Labels (N, E, S, W)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 40f // approx labelSize
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = Typeface.DEFAULT_BOLD
                    }
                    
                    val labels = listOf("N", "E", "S", "W")
                    for (i in labels.indices) {
                         // N is at -90 deg (Top) in grid space
                         val angleDeg = (i * 90) - 90
                         val angleRad = Math.toRadians(angleDeg.toDouble())
                         val lx = cx + (labelRadius * cos(angleRad)).toFloat()
                         val ly = cy + (labelRadius * sin(angleRad)).toFloat() + (textPaint.textSize/3)
                         
                         drawContext.canvas.nativeCanvas.drawText(labels[i], lx, ly, textPaint)
                    }
                }
                
                // --- LAYER 2: Magnetic Needle (Rotated by Declination relative to True North?) ---
                // Original: canvas.rotate(currentDeclination, ...) AFTER rotated by -azimuth.
                // It draws `needleBlock` (long rect) + `magSouthPath` + `magNorthPath`.
                
                // Need current declination. We don't have it in GnssStatus.
                // Ideally SensorManager gives it.
                // Assuming 0 for now or pass it in. If 0, it aligns with True North grid?
                // No, wait. 
                // Grid rotates by -Azimuth (True North Up).
                // Mag Needle rotates by Declination (offset from True North).
                // IF we don't have declination, Mag North = True North (roughly).
                
                // Let's assume Declination = 0 for now (aligns with Grid).
                // Or better: We want it to point to MAGNETIC North.
                // If Azimuth is Magnetic Azimuth, then "North" (Grid) IS Magnetic North.
                // So the needle should align with the N label.
                
                // `magNorthPath` in original:
                // Similar to `northPath` but pointing DOWN? Or Up? 
                // "magNorthPath.moveTo(..., ((crossY - mapRadius) + needLen) - SAT_RADIUS)"
                // It seems to point Up (North).
                
                rotate(degrees = -azimuth, pivot = Offset(cx, cy)) {
                    // Draw Magnetic Needle (Needle Block + Arrows)
                    // If declination is 0, this overlays True North.
                    // But visually it's distinct in color.
                    
                    val needWidth = radius / 8.0f // needBlock
                    val needLen = size.width / 15.0f
                    
                    // Mag North Arrow (Red/Orange)
                    // It's drawn at the TOP (North)
                    val magNorthPath = Path().apply {
                         val tipY = cy - radius // Top edge
                         val baseY = cy - radius + needLen
                         
                         moveTo(cx - (needWidth/2), baseY)
                         lineTo(cx, tipY)
                         lineTo(cx + (needWidth/2), baseY)
                         close()
                         
                         // Blocks below?
                         // Original loop 8 to 15...
                    }
                    drawPath(magNorthPath, colMagNorth)
                    
                    // Mag South Arrow (Grey)
                    // Drawn at BOTTOM (South)
                    val magSouthPath = Path().apply {
                         val tipY = cy + radius // Bottom edge
                         val baseY = cy + radius - needLen
                         
                         moveTo(cx - (needWidth/2), baseY)
                         lineTo(cx, tipY)
                         lineTo(cx + (needWidth/2), baseY)
                         close()
                    }
                    drawPath(magSouthPath, colMagSouth)
                }

                
                // --- SATELLITES (Screen Space) ---
                if (gnssStatus != null) {
                    for (i in 0 until gnssStatus.satelliteCount) {
                         val satAz = gnssStatus.getAzimuthDegrees(i)
                         val satEl = gnssStatus.getElevationDegrees(i)
                         val used = gnssStatus.usedInFix(i)
                         
                         // Standard Polar Plot
                         // Azimuth 0 (North) -> Top (-90 deg)
                         // Corrected for Map Rotation (-Azimuth)
                         val appAz = satAz - azimuth - 90
                         val azRad = Math.toRadians(appAz.toDouble())
                         
                         val dist = radius * ((90f - satEl) / 90f)
                         
                         val sx = cx + (cos(azRad) * dist).toFloat()
                         val sy = cy + (sin(azRad) * dist).toFloat()
                         
                         val satColor = if (used) LcarsSourceColors.COL_BRIGHT_GREEN else LcarsSourceColors.COL_YELLOW_LCARS
                         
                         drawCircle(
                             color = satColor,
                             radius = 4.dp.toPx(), // Dots
                             center = Offset(sx, sy)
                         )
                         
                         // Text ID?
                         val prn = if (android.os.Build.VERSION.SDK_INT >= 24) gnssStatus.getSvid(i).toString() else ""
                         val textPaint = android.graphics.Paint().apply {
                             color = android.graphics.Color.LTGRAY
                             textSize = 20f
                         }
                         drawContext.canvas.nativeCanvas.drawText(prn, sx + 5, sy + 5, textPaint)
                    }
                }
            }
        }
    }
}
