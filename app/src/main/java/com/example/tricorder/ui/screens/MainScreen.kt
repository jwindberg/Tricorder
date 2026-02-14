package com.example.tricorder.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.sp
import com.example.tricorder.ui.components.LcarsStrictScaffold
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.viewmodel.AudioViewModel
import com.example.tricorder.viewmodel.SensorViewModel
import com.example.tricorder.multimedia.SoundManager
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.tricorder.viewmodel.CommViewModel

enum class TricorderMode(val color: Color, val title: String) {
    GRA(LcarsSourceColors.COL_GRA, "ACCELERATION"),
    MAG(LcarsSourceColors.COL_MAG, "MAGNETIC FLUX"),
    AUD(LcarsSourceColors.COL_AUD, "ACOUSTIC"),
    GEO(LcarsSourceColors.COL_GEO, "GEOGRAPHIC"),
    EMS(LcarsSourceColors.COL_COM, "EM SPECTRUM"),
    SOL(LcarsSourceColors.COL_SOL, "SOLAR ACTIVITY")
}


@Composable
fun MainScreen(
    sensorViewModel: SensorViewModel = viewModel(),
    audioViewModel: AudioViewModel = viewModel(),
    commViewModel: CommViewModel = viewModel()
) {
    var currentMode by rememberSaveable { mutableStateOf(TricorderMode.GRA) }
    val sensorData by sensorViewModel.sensorData.collectAsState()
    
    // Scan State
    var isScanning by rememberSaveable { mutableStateOf(true) } 
    
    val context = LocalContext.current
    val soundManager = remember { SoundManager(context) }
    
    DisposableEffect(Unit) {
        onDispose {
            soundManager.release()
        }
    }

    // Aux Button Text
    val auxText = if (isScanning) "HOLD" else "SCAN"

    // React to scanning state and mode changes
    LaunchedEffect(isScanning, currentMode) {
        if (isScanning) {
            // Start Sensors
            sensorViewModel.startScanning()
            
            // Audio only in AUD mode
            if (currentMode == TricorderMode.AUD) {
                audioViewModel.startScanning()
            } else {
                audioViewModel.stopScanning()
            }
            
            // Comm only in EMS mode
            if (currentMode == TricorderMode.EMS) {
                commViewModel.startMonitoring()
            } else {
                commViewModel.stopMonitoring()
            }
            
            // Sounds
            soundManager.stopLast() // Stop previous loop if any
            when (currentMode) {
                TricorderMode.GRA -> soundManager.loop(soundManager.scanLow)
                TricorderMode.MAG -> soundManager.loop(soundManager.scanHigh)
                // TricorderMode.EMS -> soundManager.loop(soundManager.scanLow) // Optional sound
                else -> { /* No loop for others or generic? */ }
            }
        } else {
            // Stop All
            sensorViewModel.stopListening()
            audioViewModel.stopScanning()
            commViewModel.stopMonitoring()
            soundManager.stopLast()
        }
    }

    LcarsStrictScaffold(
        currentMode = currentMode,
        onModeSelected = { mode -> 
            currentMode = mode
            soundManager.play(soundManager.switchSound)
        },
        auxButtonText = auxText,
        onAuxButtonClick = {
            isScanning = !isScanning
            if (isScanning) {
                soundManager.play(soundManager.activateSound)
            } else {
                soundManager.play(soundManager.deactivateSound)
            }
        }
    ) {
        when (currentMode) {
             TricorderMode.GRA -> GravityScreen(sensorData = sensorData)
             TricorderMode.MAG -> MagScreen(sensorData = sensorData)
             TricorderMode.AUD -> AudioScreen(viewModel = audioViewModel)
             TricorderMode.GEO -> GeoScreen(
                sensorData = sensorData,
                onPermissionGranted = { sensorViewModel.refreshLocationUpdates() }
             )
             TricorderMode.EMS -> EmsScreen(viewModel = commViewModel)
             TricorderMode.SOL -> SolarScreen()
             else -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                androidx.compose.material3.Text(
                    text = "Sensor: ${currentMode.name}", 
                    color = currentMode.color, 
                    fontSize = 24.sp
                )
            }
        }
    }
}
