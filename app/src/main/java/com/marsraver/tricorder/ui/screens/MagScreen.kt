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
fun MagScreen(sensorData: SensorData) {
    // Rebuild of MagScreen to match TridataView layout
    // Colors: Teal Grid, Gold Plot
    // Unit: 60uT. Range: 2.2 units (132uT)
    
    val (x, y, z) = sensorData.magneticField
    val mag = sqrt(x*x + y*y + z*z)
    
    // Azimuth / Altitude Logic
    var az = 90.0f - Math.toDegrees(atan2(y.toDouble(), x.toDouble())).toFloat()
    if (az < 0f) az += 360f
    
    val alt = if (mag == 0f) 0f else Math.toDegrees(asin(z / mag.toDouble())).toFloat()

    val gridColor = LcarsSourceColors.COL_MAG_GRID
    val plotColor = LcarsSourceColors.COL_MAG_PLOT
    
    // Scale factors
    // Unit = 60uT. Range = 2.2 * Unit = 132uT.
    // Normalized input for gauges (-1 to +1) needs careful handling.
    // LcarsVectorElement expects x,y,z in range -1..1 (approx) for display? 
    // Actually LcarsAxis2DAtom draws based on -1..1 within "range" box.
    // But we need to normalize the RAW uT values to the visual range.
    // If range is 132uT, then 132uT = 1.0f?
    // Let's check GravityScreen again: x / 9.8f. Normalized to 1g?
    // GRA: Unit=9.8 (1g). Range=2.2*Unit.
    // The gauges likely expect normalized values where 1.0 = Unit?
    // Let's stick to normalizing by the Unit (60uT).
    
    val normX = x / 60f // Normalized to Unit (60uT) for Vector Grid
    val normY = y / 60f
    val normZ = z / 60f
    
    // Magnitude Graph uses Range (2.2 * Unit = 132uT)
    val normMag = mag / 132f 
    
    // History normalization
    val normHistory = sensorData.magneticHistory.map { it / 132f }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TOP: Absolute Vector
        LcarsVectorElement(
            x = normX, 
            y = normY,
            z = normZ, 
            az = az,
            alt = alt, 
            label = stringResource(R.string.abs_vector),
            gridColor = gridColor, 
            plotColor = plotColor,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // MIDDLE: Absolute Magnitude
        LcarsMagnitudeElement(
            value = normMag,
            history = normHistory, 
            label = stringResource(R.string.abs_magnitude),
            gridColor = gridColor,
            plotColor = plotColor,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // BOTTOM: Absolute Data
        LcarsNum3DElement(
            x = x, y = y, z = z, // Show raw uT
            mag = mag, az = az, alt = alt,
            label = stringResource(R.string.abs_data),
            gridColor = gridColor,
            plotColor = plotColor,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
    }
}
