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
import androidx.compose.ui.res.stringResource
import com.example.tricorder.R
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

        // 1. Detect Screen Rotation
        val displayRotation = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            context.display?.rotation ?: android.view.Surface.ROTATION_0
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(android.content.Context.WINDOW_SERVICE) as android.view.WindowManager).defaultDisplay.rotation
        }

        // 2. Detect Device Pose (Flat vs Upright) using Gravity
        // If z-gravity is dominant (> 7.0 or < -7.0), it's FLAT. otherwise UPRIGHT.
        // Standard gravity is ~9.8.
        val (gx, gy, gz) = sensorData.gravity
        val isFlat = kotlin.math.abs(gz) > 7.0

        val remappedMatrix = FloatArray(9)
        var success = false

        if (isFlat) {
            // FLAT USE (Compass Mode)
            // Remap so "Up" on screen is North.
            // Standard remapping for screen rotation.
            when (displayRotation) {
                android.view.Surface.ROTATION_0 -> {
                    success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_X,
                        android.hardware.SensorManager.AXIS_Y,
                        remappedMatrix
                    )
                }
                android.view.Surface.ROTATION_90 -> { // Landscape Phone / Tablet Default
                     success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_Y,
                        android.hardware.SensorManager.AXIS_MINUS_X,
                        remappedMatrix
                    )
                }
                android.view.Surface.ROTATION_180 -> {
                     success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_MINUS_X,
                        android.hardware.SensorManager.AXIS_MINUS_Y,
                        remappedMatrix
                    )
                }
                android.view.Surface.ROTATION_270 -> {
                    success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_MINUS_Y,
                        android.hardware.SensorManager.AXIS_X,
                        remappedMatrix
                    )
                }
            }
        } else {
            // UPRIGHT USE (AR/Scanner Mode)
            // Remap so "Back of Camera" is North.
            // This is trickier. We generally want Z-axis to be sticking out of back.
            // Remap X -> X, Y -> Z
            
            val cameraMatrix = FloatArray(9)
            // Base remapping for upright holding (Camera pointing out)
            // Map Y (Device Top) to Z (World Up) -> No...
            // We want Azimuth to be direction camera is facing.
            // Standard getOrientation gives Azimuth = Y-axis projection on horizontal plane.
            
            // For AR, we often want remapping X, Z.
             when (displayRotation) {
                android.view.Surface.ROTATION_0 -> {
                    success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_X,
                        android.hardware.SensorManager.AXIS_Z, // Map Y-axis sensor to Z-axis?
                        remappedMatrix
                    )
                }
                android.view.Surface.ROTATION_90 -> {
                     success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_Z, 
                        android.hardware.SensorManager.AXIS_MINUS_X,
                        remappedMatrix
                    )
                }
                 // Simple Fallback to same as flat for now, but with Z-axis mapping?
                 // Actually, the simplest fix for "East when Upright" on many devices
                 // is that the axes are just swapped relative to flat.
                 
                 // Let's rely on the user's report: "Use to work upright" (implying default was OK for upright?).
                 // If default was OK for upright on a phone, then ROTATION_0 Flat logic might be correct for Upright too?
                 // User said "Holding upright... points East".
                 // This implies calculating Azimuth from Y-axis when Z-axis is horizontal.
                 // This is the classic "Gimbal Lock" / Magnetic Dip issue if not handled.
                 
                 // Let's try mapping X, Z for Upright (Standard AR Camera mapping).
                 else -> {
                      success = android.hardware.SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        android.hardware.SensorManager.AXIS_X,
                        android.hardware.SensorManager.AXIS_Z,
                        remappedMatrix
                    )
                 }
            }
        }
        
        if (success) {
            val orientation = FloatArray(3)
            android.hardware.SensorManager.getOrientation(remappedMatrix, orientation)
            val azDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
            // Apply -90 degree offset to correct for default orientation
            val trueAzimuth = (azDeg + 360) % 360
            (trueAzimuth + sensorData.declination + 360) % 360
        } else {
             // Fallback
             val orientation = FloatArray(3)
             android.hardware.SensorManager.getOrientation(rotationMatrix, orientation)
             val azDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
             // Apply -90 degree offset to correct for default orientation
             val trueAzimuth = (azDeg + 360) % 360
             (trueAzimuth + sensorData.declination + 360) % 360
        }
    } else {
         // Fallback vars (Accelerometer + Magnetometer)
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
                    title = stringResource(R.string.network_location),
                    location = null,
                    status = stringResource(R.string.permission_denied), // Using "PERMISSION REQUIRED" from strings as "permission_denied" or similar
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
                    Text(stringResource(R.string.grant_permission))
                }
            }
        } else {
             // Network Location (Top)
            LcarsGeoElement(
                title = stringResource(R.string.network_location),
                location = sensorData.networkLocation,
                status = if (sensorData.networkLocation == null) stringResource(R.string.searching) else null,
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // GPS Location (Middle)
            LcarsGeoElement(
                title = stringResource(R.string.gps_location),
                location = sensorData.gpsLocation,
                status = if (sensorData.gpsLocation == null) stringResource(R.string.searching) else null,
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
