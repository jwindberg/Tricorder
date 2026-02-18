package com.marsraver.tricorder.ui.visualizations

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsraver.tricorder.ui.theme.LcarsBlue
import com.marsraver.tricorder.ui.theme.LcarsOrange
import com.marsraver.tricorder.ui.theme.LcarsPurple
import com.marsraver.tricorder.ui.theme.LcarsRed
import com.marsraver.tricorder.viewmodel.SensorData
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import kotlin.math.max

// Original colors from PowerGauge.java
private val METER_AVERAGE_COL = Color(0xA0FF9900)
private val METER_POWER_COL = Color(0xFF0000FF)
// Peak color base is 0xFFFF0000 (Red), alpha varies.
private val METER_PEAK_COL_BASE = 0x00FF0000 
// Grid: -2115966 -> 0xFFDFC082 (Alpha FF, DF, C0, 82)
private val COLOUR_GRID = Color(0xFFDFC082)

/**
 * Reimplementation of org.hermit.android.instruments.SpectrumGauge
 */
@Composable
fun AudioSpectrumGraph(
    spectrumData: FloatArray,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val width = size.width
        val height = size.height
        
        // Grid Logic from SpectrumGauge.java
        // "spectGraphMargin = labelSize" -> default width/24
        val labelSize = width / 24f
        val margin = labelSize
        val graphX = margin
        val graphY = 0f
        val graphW = width - (margin * 2)
        val rangeBels = 6f
        val graphH = (height - labelSize) - rangeBels // weird java logic, let's just use available height minus label space
        
        // Let's stick to a clean layout: 
        // Graph area takes top (height - labelSize)
        // Labels take bottom (labelSize)
        
        val actualGraphH = height - labelSize
        
        // Draw Grid Background
        val gridPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
            color = android.graphics.Color.parseColor("#FFDFC082") // COLOUR_GRID -256? No, AudioView says COLOUR_GRID = -2115966 (Beige)
            // Wait, SpectrumGauge.java says: "paint.setColor(-256);" which is 0xFFFFFF00 (Yellow).
            // But AudioView passes COLOUR_GRID to ElementWrapper, not SpectrumGauge directly?
            // SpectrumGauge.drawBg uses hardcoded -256 (Yellow).
            // Let's use Color.Yellow or the standardized Lcars color if user prefers.
            // User said "yellow curved tip bars", so Yellow is likely the theme.
            // Let's use Standard Lcars Orange/Yellow. 0xFFFF9900 is close.
            color = android.graphics.Color.YELLOW
            style = android.graphics.Paint.Style.STROKE
            strokeWidth = 1f
        }
        
        // Frame
        drawRect(
            color = Color.Yellow,
            topLeft = Offset(graphX, graphY),
            size = Size(graphW, actualGraphH),
            style = Stroke(width = 1f)
        )
        
        // Vertical Grid (10 divisions)
        for (i in 1..9) {
            val x = graphX + (i * graphW / 10f)
            drawLine(
                color = Color.Yellow,
                start = Offset(x, graphY),
                end = Offset(x, actualGraphH),
                strokeWidth = 1f
            )
        }
        
        // Horizontal Grid (RANGE_BELS = 6)
        for (i in 1 until 6) {
            val y = graphY + (i * actualGraphH / 6f)
            drawLine(
                color = Color.Yellow,
                start = Offset(graphX, y),
                end = Offset(graphX + graphW, y),
                strokeWidth = 1f
            )
        }
        
        // Data Drawing (Linear Graph)
        // SpectrumGauge: "linearGraph"
        // HSV Colors: Hue = (i/len) * 300. (Rainbow)
        // Value = log10(data[i]) / 6.0 + 1.0 (Mapping -60dB..0dB to 0..1 height?)
        // Wait, "Math.log10(data[i]) / 6.0d) + 1.0d" -> implies data is 0..1? 
        // log10(1) = 0. log10(0.000001) = -6. 
        // So this maps 1e-6 (-60dB) to 0, and 1 (0dB) to 1.
        
        val len = spectrumData.size
        if (len > 0) {
           val barWidth = (graphW - 2f) / len
           
           for (i in 0 until len) {
               // Color
               val hue = (i.toFloat() / len) * 300f
               val color = Color.hsv(hue, 1f, 1f)
               
               // Height
               val dbVal = if (spectrumData[i] > 0) kotlin.math.log10(spectrumData[i].toDouble()).toFloat() else -10f
               // Clamp to -6 (1e-6)
               val normHeight = (dbVal / 6f) + 1f
               val clHeight = normHeight.coerceIn(0f, 1f)
               
               val barH = clHeight * (actualGraphH - 2f)
               
               val x = graphX + 1f + (i * barWidth)
               val y = (graphY + actualGraphH) - 1f - barH
               
               if (barH > 0) {
                   drawRect(
                       color = color,
                       topLeft = Offset(x, y),
                       size = Size(barWidth, barH)
                   )
               }
           }
        }
        
        // Labels (X-Axis)
        // Draw standard labels using native Paint for performance/simplicity in Canvas
        data class Label(val text: String, val xPct: Float)
        val labels = listOf(
            Label("0", 0f),
            Label("400", 400f / 4000f),
            Label("800", 800f / 4000f),
            Label("1.2k", 1200f / 4000f),
            Label("1.6k", 1600f / 4000f),
            Label("2.0k", 2000f / 4000f),
            Label("2.4k", 2400f / 4000f),
            Label("2.8k", 2800f / 4000f),
            Label("3.2k", 3200f / 4000f),
            Label("3.6k", 3600f / 4000f),
            Label("4.0k", 1.0f)
        )
        
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.YELLOW
            textSize = 24f // Adjust as needed
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }
        
        drawIntoCanvas { canvas ->
             labels.forEach { label ->
                 val x = graphX + (label.xPct * graphW)
                 val y = height - 5f // Bottom padding
                 canvas.nativeCanvas.drawText(label.text, x, y, textPaint)
             }
        }
    }
}



/**
 * Reimplementation of org.hermit.android.instruments.WaveformGauge
 */
@Composable
fun AudioWaveformGraph(
    dataPoints: FloatArray,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize().background(Color.Black)) {
        val width = size.width
        val height = size.height
        val margin = width / 24f
        val baseY = height / 2f
        
        // Draw Axis
        drawLine(
            color = Color.Yellow, 
            start = Offset(margin, 0f), 
            end = Offset(margin, height), 
            strokeWidth = 1f
        )
        
        // Draw Waveform
        // Usage in AudioViewModel: _waveform is FloatArray from Short buffer.
        // Original: assumes short array, scales by height/16384 (approx).
        // Our data is already floats (-1..1)?
        // AudioViewModel: buffer[i] / 32768f -> yes, -1.0 to 1.0.
        // WaveformGauge scale logic: 
        // scale = (1.0 / (range/6500))^0.7 / 16384 * height... 
        // Basically maps amplitude to height.
        // If data is -1..1, we map -1 to height, 1 to 0? Or centered?
        // "baseY - (buffer[i] * scale)" -> centered at baseY.
        
        val len = dataPoints.size
        if (len > 0) {
            val uw = (width - 2 * margin) / len.toFloat()
            // Scale: let's maximize height usage. +/- 1.0 fills height/2.
            // So scale = (height/2) * 0.9 (margin)
            val scale = (height / 2f) * 0.9f 

            val path = Path()
            var started = false
            
            for (i in 0 until len) {
                val x = margin + (i * uw)
                val y = baseY - (dataPoints[i] * scale)
                
                if (!started) {
                    path.moveTo(x, y)
                    started = true
                } else {
                    path.lineTo(x, y)
                }
                
                // Original uses drawLine for every point (vertical bars or connected?)
                // "canvas.drawLine(x, baseY, x, baseY - val*scale)" 
                // It draws VERTICAL LINES from center to value! (Bar chart style for waveform?)
                // Yes: "canvas.drawLine(x, baseY, x, baseY - ...)"
                drawLine(
                    color = Color.Yellow,
                    start = Offset(x, baseY),
                    end = Offset(x, y),
                    strokeWidth = 1f
                )
            }
        }
    }
}

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

/**
 * Reimplementation of org.hermit.android.instruments.PowerGauge
 */
@Composable
fun AudioPowerGauge(
    currentPowerDb: Float, // This is expected to be in dB (-100 to 0)
    modifier: Modifier = Modifier
) {
    // State to hold persistent logic like average power and peaks
    // We use a class to encapsulate this state so we can update it frame-by-frame or on new data
    val gaugeState = remember { PowerGaugeState() }
    
    // Update the gauge state when new data comes in
    LaunchedEffect(currentPowerDb) {
        gaugeState.update(currentPowerDb)
    }

    // Animation loop for smooth decaying of peaks / UI updates
    // In Compose we can use a frame loop if we want smooth animations independent of data updates
    // or just rely on state changes triggering recomposition.
    // The original app used a SurfaceRunner (game loop). 
    // Here we can use withFrameMillis to create a loop for the time-based peak decay/fade.
    var now by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frameTime ->
                now = frameTime // This triggers recomposition every frame
                gaugeState.prunePeaks()
            }
        }
    }

    // Canvas Only
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                 val width = size.width
                 val height = size.height
                 
                 val margin = width * 0.05f // 5% margin
                 val safeWidth = width - 2 * margin
                 val barHeight = height * 0.2f // Reduced to 20% to make it thin again (within 100dp container)
                 val labelY = barHeight + 25f // Position for text below bar
                 
                 // Grid / Scale
                 // -100, -90, ... 0
                 val gridColor = COLOUR_GRID
                 
                 // Frame
                 drawRect(
                     color = gridColor,
                     topLeft = Offset(margin, 0f),
                     size = Size(safeWidth, barHeight),
                     style = Stroke(width = 2f)
                 )
                 
                 // Scale Labels Paint
                 val textPaint = androidx.compose.ui.graphics.Paint().asFrameworkPaint().apply {
                    color = android.graphics.Color.parseColor("#FFDFC082") // Beige
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.MONOSPACE
                 }
                 
                 // Ticks and Labels
                 drawIntoCanvas { canvas ->
                     for (i in 0..10) { // 0 to 10 mapped to -100 to 0
                         val x = margin + (i * safeWidth / 10f)
                         
                         // Tick
                         drawLine(
                             color = gridColor,
                             start = Offset(x, 0f),
                             end = Offset(x, barHeight),
                             strokeWidth = 1f
                         )
                         
                         // Label (-100, -90, ... 0)
                         val labelVal = -100 + (i * 10)
                         canvas.nativeCanvas.drawText(labelVal.toString(), x, labelY, textPaint)
                     }
                 }
                 
                 // 1. Average Power Bar (Orange)
                 val avgPower = gaugeState.averagePower.coerceIn(-100f, 0f)
                 val avgW = ((avgPower + 100f) / 100f) * safeWidth
                 if (avgW > 0) {
                     drawRect(
                         color = METER_AVERAGE_COL,
                         topLeft = Offset(margin + 2f, 2f),
                         size = Size(avgW, barHeight - 4f)
                     )
                 }
                 
                 // 2. Current Power Bar (Blue, narrower)
                 val curPower = gaugeState.currentPower.coerceIn(-100f, 0f)
                 val curW = ((curPower + 100f) / 100f) * safeWidth
                 val gap = barHeight * 0.25f
                 if (curW > 0) {
                     drawRect(
                         color = METER_POWER_COL,
                         topLeft = Offset(margin + 2f, gap),
                         size = Size(curW, barHeight - 2 * gap)
                     )
                 }
                 
                 // 3. Peaks (Red, fading)
                 val sysNow = System.currentTimeMillis()
                 gaugeState.meterPeaks.forEachIndexed { i, peakVal ->
                     val pTime = gaugeState.meterPeakTimes[i]
                     if (pTime > 0) {
                         val age = sysNow - pTime
                         if (age < 4000) {
                             val alpha = (1f - (age / 4000f)).coerceIn(0f, 1f)
                             val pParams = peakVal.coerceIn(-100f, 0f)
                             val pX = ((pParams + 100f) / 100f) * safeWidth
                             
                             drawRect(
                                 color = Color(1f, 0f, 0f, alpha),
                                 topLeft = Offset(margin + pX - 2f, gap),
                                 size = Size(4f, barHeight - 2 * gap)
                             )
                         }
                     }
                }
            }
        }
    }

// Logic class to replicate the complex peak hold and averaging logic from PowerGauge.java
class PowerGaugeState {
    var currentPower by mutableFloatStateOf(-100f)
    var averagePower by mutableFloatStateOf(-100f)
    
    private val METER_PEAKS = 4
    val meterPeaks = FloatArray(METER_PEAKS) { -100f }
    val meterPeakTimes = LongArray(METER_PEAKS) { 0L }
    
    var maxPeak by mutableFloatStateOf(-100f)
    
    // History for simple average smoothing if needed, but original used inline EMA
    // "averagePower -= prev/32; averagePower += power/32;" -> EMA with alpha 1/32 ~ 0.03
    
    // We need to store previous power to detect rising edge for peaks
    private var prevPower = -100f
    
    // Circular buffer for Moving Average as per original "powerHistory" array?
    // Original: 
    // float[] powerHistory = new float[32];
    // avg -= history[idx]/32; history[idx] = now; avg += now/32;
    // This is a Simple Moving Average (SMA) over 32 samples.
    private val historySize = 32
    private val history = FloatArray(historySize) { -100f }
    private var historyIdx = 0
    
    fun update(newPower: Float) {
        // Clamp
        val power = newPower.coerceIn(-100f, 0f)
        currentPower = power
        
        // SMA Update
        val oldVal = history[historyIdx]
        history[historyIdx] = power
        historyIdx = (historyIdx + 1) % historySize
        
        // Recompute average to avoid drift or just incremental?
        // Incremental: avg = avg - (old/32) + (new/32)
        // Let's do exact sum to be safe and simple
        averagePower = history.average().toFloat()
        
        // Peak Logic
        val now = System.currentTimeMillis()
        calculatePeaks(now, power, prevPower)
        
        prevPower = power
    }
    
    fun prunePeaks() {
        val now = System.currentTimeMillis()
        // Just invalidate old peaks
        for (i in 0 until METER_PEAKS) {
            if (meterPeakTimes[i] != 0L && (now - meterPeakTimes[i] > 4000)) {
                meterPeakTimes[i] = 0L
            }
        }
    }

    private fun calculatePeaks(now: Long, power: Float, prev: Float) {
        // "if (power > prev)" -> rising edge logic from original
        if (power > prev) {
             // Logic: find a slot to update or add new peak
             // Original logic was a bit convoluted "if (meterPeaks[i2] - power < 2.5)" update it
             
             // Simplified faithful logic:
             // 1. If we have an existing peak close to this value (within 2.5dB), update its time and value
             var done = false
             for (i in 0 until METER_PEAKS) {
                 if (meterPeakTimes[i] != 0L && kotlin.math.abs(meterPeaks[i] - power) < 2.5f) {
                     meterPeaks[i] = max(meterPeaks[i], power) // Keep higher? Original checks diff.
                     meterPeakTimes[i] = now
                     done = true
                     break
                 }
             }
             
             // 2. If not updated, find an empty slot
             if (!done) {
                 for (i in 0 until METER_PEAKS) {
                     if (meterPeakTimes[i] == 0L) {
                         meterPeaks[i] = power
                         meterPeakTimes[i] = now
                         done = true
                         break
                     }
                 }
             }
             
             // 3. If full, maybe replace oldest? Original doesn't seem to, just "break".
             // We'll stick to simple "fill empty slots".
        }
        
        // Calculate Global Max for text display
        var m = -100f
        for (i in 0 until METER_PEAKS) {
             if (meterPeakTimes[i] != 0L && meterPeaks[i] > m) {
                 m = meterPeaks[i]
             }
        }
        maxPeak = m
    }
}
