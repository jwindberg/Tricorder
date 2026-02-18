package com.marsraver.tricorder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import com.marsraver.tricorder.ui.visualizations.AudioWaveformGraph
import com.marsraver.tricorder.ui.visualizations.AudioSpectrumGraph
import com.marsraver.tricorder.ui.visualizations.AudioPowerGauge
import com.marsraver.tricorder.ui.components.LcarsElementWrapper
import com.marsraver.tricorder.ui.theme.LcarsSourceColors
import com.marsraver.tricorder.viewmodel.AudioViewModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.marsraver.tricorder.R

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
        // Removed auto-start. Recording is now controlled by MainScreen via viewModel.startScanning()
        
        // Strict Port of AudioView.java
        // Layout: Waveform (Top), Spectrum (Bottom), Power (Bottom-ish/Integrated)
        
        // Collect Real Data
        val waveform by viewModel.waveform.collectAsState()
        val spectrum by viewModel.spectrum.collectAsState()

        Column(modifier = Modifier.fillMaxSize()) {
            // Header

            
            // Waveform Window
            Box(modifier = Modifier.weight(0.33f).fillMaxWidth()) {
                LcarsElementWrapper(
                    title = stringResource(R.string.waveform),
                    gridColor = LcarsSourceColors.COL_GRA,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Start of Content
                    AudioWaveformGraph(
                        dataPoints = waveform,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Spectrum Window
            Box(modifier = Modifier.weight(0.33f).fillMaxWidth()) {
                 LcarsElementWrapper(
                    title = stringResource(R.string.spectrum),
                    gridColor = LcarsSourceColors.COL_GRA,
                    modifier = Modifier.fillMaxSize()
                ) {
                     AudioSpectrumGraph(
                        spectrumData = spectrum,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Power Window
            val powerState = viewModel.power.collectAsState()
            val peakState = viewModel.peak.collectAsState()
            
            Box(modifier = Modifier.weight(0.33f).fillMaxWidth()) {
                 LcarsElementWrapper(
                    title = stringResource(R.string.power),
                    gridColor = LcarsSourceColors.COL_GRA,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        AudioPowerGauge(
                            currentPowerDb = powerState.value,
                            modifier = Modifier.fillMaxWidth().height(100.dp).padding(top = 12.dp) // Lowered by ~2mm (12dp)
                        )
                        
                        // Digital Readout (Moved below power gauge as requested)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 0.dp, start = 16.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically // Center the two blocks vertically relative to each other
                        ) {
                            // Left: Real Power Value
                             Text(
                                text = String.format("%.1fdB", powerState.value),
                                color = LcarsSourceColors.COL_MAG, // Teal/Cyan
                                fontSize = 32.sp, // Reduced from 36 to 32
                                lineHeight = 32.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                maxLines = 1,
                                softWrap = false
                            )
                            
                            Spacer(modifier = Modifier.weight(1f))
                            
                            // Right: Peak Value + "Peak" Label
                            Column(
                                modifier = Modifier.clickable { viewModel.resetPeak() }, // Tap to reset
                                horizontalAlignment = Alignment.End // Align to the right side
                            ) {
                                Text(
                                    text = String.format("%.1fdB", peakState.value),
                                    color = LcarsSourceColors.COL_MAG, // Teal/Cyan
                                    fontSize = 18.sp, // Reduced from 20 to 18
                                    lineHeight = 18.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "PEAK", // Uppercase usually looks better in LCARS
                                    color = LcarsSourceColors.COL_MAG, // Teal/Cyan
                                    fontSize = 12.sp, // Kept at 12
                                    lineHeight = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                        
                        // Push everything up
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.audio_offline), color = Color.Red)
            Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) {
                Text(stringResource(R.string.initialize_sensors))
            }
        }
    }
}
