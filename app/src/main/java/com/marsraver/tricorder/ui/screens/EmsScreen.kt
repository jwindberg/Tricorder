package com.marsraver.tricorder.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marsraver.tricorder.ui.components.LcarsBargraph
import com.marsraver.tricorder.ui.components.LcarsHeaderBarElement
import com.marsraver.tricorder.ui.theme.LcarsSourceColors
import com.marsraver.tricorder.viewmodel.CommViewModel
import androidx.compose.ui.res.stringResource
import com.marsraver.tricorder.R

@Composable
fun EmsScreen(
    viewModel: CommViewModel = viewModel()
) {
    val context = LocalContext.current
    var hasPermissions by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasPermissions = perms.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            launcher.launch(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.startMonitoring()
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            viewModel.startMonitoring()
        }
    }
    
    // Stop monitoring when leaving screen composition? 
    // Ideally we should do this in DisposableEffect or ViewModel onCleared, 
    // but here we can just rely on ViewModel lifecycle if it's scoped to this screen.
    // However, since it's passed or scoped differently, let's just start it.

    if (hasPermissions) {
        val commData by viewModel.commData.collectAsState()
        
        Column(modifier = Modifier.fillMaxSize()) { // Removed padding(16.dp)
            
            // --- CELLULAR SECTION ---
            LcarsHeaderBarElement(
                text = stringResource(R.string.cellular),
                color = LcarsSourceColors.COL_COM,
                modifier = Modifier.fillMaxWidth().height(50.dp) // Increased to 50dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.operator_prefix, commData.operatorName),
                color = LcarsSourceColors.COL_COM,
                fontFamily = FontFamily.Monospace,
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp) // Added padding to content
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp)) { // Added padding
                Text(
                    text = stringResource(R.string.signal_dbm_format, commData.signalDbm),
                    color = LcarsSourceColors.COL_COM,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.align(Alignment.CenterVertically).width(120.dp)
                )
                
                // Map dBm to 0-1 range roughly. 
                // LTE: -120 (bad) to -60 (good)
                val signalPercent = ((commData.signalDbm + 120) / 60f).coerceIn(0f, 1f)
                
                LcarsBargraph(
                    value = signalPercent,
                    max = 1f,
                    segments = 10,
                    gridColor = LcarsSourceColors.COL_COM,
                    plotColor = LcarsSourceColors.COL_BRIGHT_RED, // Red for Cellular
                    modifier = Modifier.height(40.dp).fillMaxWidth().align(Alignment.CenterVertically)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // --- WIFI SECTION ---
            LcarsHeaderBarElement(
                text = stringResource(R.string.wifi),
                color = LcarsSourceColors.COL_COM,
                modifier = Modifier.fillMaxWidth().height(50.dp) // Increased to 50dp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (commData.isWifiEnabled) {
                val ssid = commData.wifiSsid
                if (ssid != null) {
                    Text(
                        text = stringResource(R.string.connected_prefix, ssid),
                        color = LcarsSourceColors.COL_COM,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp) // Added padding
                    )
                    Text(
                        text = stringResource(R.string.signal_dbm_format, commData.wifiRssi),
                        color = LcarsSourceColors.COL_COM,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp) // Added padding
                    )
                } else {
                    Text(
                        text = stringResource(R.string.scanning),
                        color = LcarsSourceColors.COL_COM,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 16.dp) // Added padding
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.disabled),
                    color = LcarsSourceColors.COL_COM,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp) // Added padding
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // WiFi Scan Results List
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items(commData.scanResults) { result ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = result.SSID.take(15).padEnd(15), // Truncate/Pad
                            color = LcarsSourceColors.COL_COM,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                            modifier = Modifier.width(140.dp)
                        )
                        
                        // Map RSSI to 0-1
                        // -100 (bad) to -40 (good)
                        val rssiPercent = ((result.level + 100) / 60f).coerceIn(0f, 1f)
                        
                        LcarsBargraph(
                            value = rssiPercent,
                            max = 1f,
                            segments = 5,
                            gridColor = LcarsSourceColors.COL_COM,
                            plotColor = LcarsSourceColors.COL_YELLOW_LCARS,
                            modifier = Modifier.height(20.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    } else {
        // Permission Denied View
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.ems_offline), color = Color.Red)
            Text(stringResource(R.string.req_perms), color = Color.Red)
            Button(onClick = { 
                launcher.launch(
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )
                ) 
            }) {
                Text(stringResource(R.string.initialize_sensors))
            }
        }
    }
}
