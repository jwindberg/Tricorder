package com.example.tricorder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import com.example.tricorder.ui.visualizations.LcarsWaveformGraph
import com.example.tricorder.ui.visualizations.LcarsSpectrumGraph
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.viewmodel.AudioViewModel

@Composable
fun AudioScreen(
    viewModel: AudioViewModel
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (hasPermission) {
        LaunchedEffect(Unit) {
            viewModel.startRecording()
        }
        
        // Strict Port of AudioView.java
        // Layout: Waveform (Top), Spectrum (Bottom), Power (Bottom-ish/Integrated)
        
        // Collect Real Data
        val waveform by viewModel.waveform.collectAsState()
        val spectrum by viewModel.spectrum.collectAsState()

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Header
             Text(
                text = "Acoustic Analysis",
                color = LcarsSourceColors.COL_AUD,
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Waveform Window
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                 LcarsWaveformGraph(
                    dataPoints = waveform,
                    gridColor = LcarsSourceColors.COL_AUD,
                    plotColor = LcarsSourceColors.XYZ_PLOT_COLS[0],
                    modifier = Modifier.fillMaxSize()
                )
                Text("Waveform", color = LcarsSourceColors.COL_AUD, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Spectrum Window
            Box(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
                    .background(Color.Black)
            ) {
                 LcarsSpectrumGraph(
                    dataPoints = spectrum,
                    gridColor = LcarsSourceColors.COL_AUD,
                    plotColor = LcarsSourceColors.XYZ_PLOT_COLS[2],
                    modifier = Modifier.fillMaxSize()
                )
                 Text("Spectrum", color = LcarsSourceColors.COL_AUD, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
            }
        }

    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AUDIO SENSORS OFFLINE - PERMISSION DENIED", color = Color.Red)
            Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text("INITIALIZE SENSORS")
            }
        }
    }
}
