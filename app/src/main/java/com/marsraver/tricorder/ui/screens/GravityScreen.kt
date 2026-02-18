package com.marsraver.tricorder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marsraver.tricorder.ui.components.LcarsVectorElement
import com.marsraver.tricorder.ui.components.LcarsMagnitudeElement
import com.marsraver.tricorder.ui.components.LcarsNum3DElement
import com.marsraver.tricorder.ui.theme.LcarsSourceColors
import com.marsraver.tricorder.viewmodel.SensorData
import androidx.compose.ui.res.stringResource
import com.marsraver.tricorder.R
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.asin

@Composable
fun GravityScreen(sensorData: SensorData) {
    // Strict Port of TridataView.java layoutPortrait for Gravity
    // The original app likely used TYPE_ACCELEROMETER for "Gravimetric" mode, 
    // as it allows the user to see fluctuations (movement) + gravity.
    // TYPE_GRAVITY is a virtual sensor that isolates gravity (constant magnitude), which results in a static gauge.
    
    val (x, y, z) = sensorData.accelerometer
    val mag = sqrt(x*x + y*y + z*z)
    
    // Calculate Azimuth and Altitude (from TridataView.onSensorData)
    // float az = 90.0f - ((float) Math.toDegrees(Math.atan2((double) y, (double) x)));
    // Note: The original source likely used raw accelerometer values for this too.
    var az = 90.0f - Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
    if (az < 0f) az += 360f
    
    // float alt = m == 0.0f ? 0.0f : (float) Math.toDegrees(Math.asin((double) (z / m)));
    val alt = if (mag == 0f) 0f else Math.toDegrees(asin(z / mag.toDouble())).toFloat()

    val color = LcarsSourceColors.COL_GRA_GRID

    Column(
        modifier = Modifier
            .fillMaxSize()
            // .padding(8.dp) // Removed to eliminate extra blank space
    ) {
        // TOP: Absolute Vector (AxisElement)
        LcarsVectorElement(
            x = x / 9.8f, 
            y = y / 9.8f,
            z = (z + 9.8f) / 19.6f, 
            az = az,
            alt = alt, 
            label = stringResource(R.string.abs_vector),
            gridColor = color, 
            plotColor = LcarsSourceColors.COL_GRA_PLOT, // Explicit Orange Plot
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // MIDDLE: Absolute Magnitude (MagnitudeElement)
        LcarsMagnitudeElement(
            value = mag / 19.6f, // Normalize 0-2g (center 1g)
            history = sensorData.accelerometerHistory.map { it / 19.6f }, 
            label = stringResource(R.string.abs_magnitude),
            gridColor = color,
            plotColor = LcarsSourceColors.COL_GRA_PLOT, // Explicit Orange Plot
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // BOTTOM: Absolute Data (Num3DElement)
        LcarsNum3DElement(
            x = x, y = y, z = z, // Show raw m/s^2
            mag = mag, az = az, alt = alt,
            label = stringResource(R.string.abs_data),
            gridColor = color,
            plotColor = LcarsSourceColors.COL_GRA_PLOT, // Explicit Orange Plot
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}
