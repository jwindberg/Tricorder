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
import androidx.compose.ui.unit.dp
import com.example.tricorder.ui.theme.LcarsOrange
import com.example.tricorder.ui.theme.LcarsBlue
import com.example.tricorder.viewmodel.SensorData
import androidx.compose.ui.res.stringResource
import com.example.tricorder.R
@Composable
fun EnvironmentScreen(
    sensorData: SensorData
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.atmospheric_sensors),
            color = LcarsOrange,
            style = MaterialTheme.typography.titleMedium
        )
        
        SensorRow(
            stringResource(R.string.pressure_label), 
            sensorData.pressure?.let { stringResource(R.string.pressure_format, it) } ?: stringResource(R.string.unavailable)
        )
        SensorRow(
            stringResource(R.string.light_label), 
            sensorData.light?.let { stringResource(R.string.light_format, it) } ?: stringResource(R.string.unavailable)
        )
        SensorRow(
            stringResource(R.string.temp_label), 
            sensorData.ambientTemp?.let { stringResource(R.string.temp_format, it) } ?: stringResource(R.string.unavailable)
        )
        SensorRow(
            stringResource(R.string.humidity_label), 
            sensorData.humidity?.let { stringResource(R.string.humidity_format, it) } ?: stringResource(R.string.unavailable)
        )
    }
}

@Composable
fun SensorRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = LcarsBlue, style = MaterialTheme.typography.labelLarge)
        Text(text = value, color = LcarsOrange, style = MaterialTheme.typography.headlineSmall)
    }
}
