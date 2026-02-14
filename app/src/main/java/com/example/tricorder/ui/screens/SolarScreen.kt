package com.example.tricorder.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.tricorder.ui.components.LcarsHeaderBarElement
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.viewmodel.SolarViewModel

@Composable
fun SolarScreen(
    viewModel: SolarViewModel = viewModel()
) {
    val solarData by viewModel.solarData.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        
        // --- SOLAR IMAGING SECTION ---
        LcarsHeaderBarElement(
            text = "Solar Imaging",
            color = LcarsSourceColors.COL_SOL,
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "SOURCE: ${solarData.imageTitle}",
            color = LcarsSourceColors.COL_SOL,
            fontFamily = FontFamily.Monospace,
            fontSize = 18.sp
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Large square-ish area
                .clickable { viewModel.cycleImage() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(solarData.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Solar Image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            
            // Overlay instruction if needed, or just keep clean LCARS look
        }
        
        Text(
            text = "TAP IMAGE TO CYCLE WAVELENGTH",
            color = LcarsSourceColors.COL_SOL,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- SOLAR DATA SECTION ---
        LcarsHeaderBarElement(
            text = "Solar Indices",
            color = LcarsSourceColors.COL_SOL,
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SUNSPOTS",
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    text = solarData.sunspotNumber,
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "RADIO FLUX",
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    text = solarData.radioFlux,
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "FLARES",
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                )
                Text(
                    text = solarData.solarFlares,
                    color = LcarsSourceColors.COL_SOL,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp // Smaller font for "C:0 M:0 X:0"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "DATA: ${solarData.lastUpdate}",
            color = Color.Gray,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}
