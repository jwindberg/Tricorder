package com.example.tricorder.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.viewmodel.SensorData

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.tricorder.ui.components.LcarsGeoElement
import com.example.tricorder.ui.components.LcarsSkyMap
import kotlin.math.atan2

@Composable
fun GeoScreen(
    sensorData: SensorData,
    onPermissionGranted: () -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { perms ->
            hasPermission = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
    )

    LaunchedEffect(Unit) {
        android.util.Log.d("GeoScreen", "Composing GeoScreen. hasPermission=$hasPermission")
        if (!hasPermission) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
             onPermissionGranted()
        }
    }
    
    LaunchedEffect(hasPermission) {
        android.util.Log.d("GeoScreen", "hasPermission changed to: $hasPermission")
        if (hasPermission) {
            onPermissionGranted()
        }
    }

    val azimuth = if (sensorData.rotationVector != null) {
        val rotationMatrix = FloatArray(9)
        android.hardware.SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorData.rotationVector)
        
        val orientation = FloatArray(3)
        android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
        val azDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
        (azDeg + 360) % 360
    } else {
         // Fallback vars
         val rawGravity = if (sensorData.gravity == Triple(0f, 0f, 0f)) sensorData.accelerometer else sensorData.gravity
         val (gravX, gravY, gravZ) = rawGravity
         val (magX, magY, magZ) = sensorData.magneticField

         if (rawGravity == Triple(0f, 0f, 0f) || sensorData.magneticField == Triple(0f, 0f, 0f)) {
            0f
         } else {
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val orientation = FloatArray(3)
            val gravityArr = floatArrayOf(gravX, gravY, gravZ)
            val geomagneticArr = floatArrayOf(magX, magY, magZ)
            
            if (android.hardware.SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravityArr, geomagneticArr)) {
                android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
                val azDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                (azDeg + 360) % 360
            } else {
                0f
            }
         }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (!hasPermission) {
            Column {
                LcarsGeoElement(
                    title = "Network Location",
                    location = null,
                    status = "PERMISSION REQUIRED",
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )
                androidx.compose.material3.Button(
                    onClick = { 
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("GRANT PERMISSION")
                }
            }
        } else {
             // Network Location (Top)
            LcarsGeoElement(
                title = "Network Location",
                location = sensorData.networkLocation,
                status = if (sensorData.networkLocation == null) "SEARCHING..." else null,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GPS Location (Middle)
            LcarsGeoElement(
                title = "GPS Location",
                location = sensorData.gpsLocation,
                status = if (sensorData.gpsLocation == null) "SEARCHING..." else null,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            )
            
            Spacer(modifier = Modifier.height(8.dp))



            // Satellite Sky Map (Bottom / Fill)
            LcarsSkyMap(
                gnssStatus = sensorData.gnssStatus,
                azimuth = azimuth.toFloat(),
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun GeoScreenPreview() {
    GeoScreen(
        sensorData = SensorData(),
        onPermissionGranted = {}
    )
}
