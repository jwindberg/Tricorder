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
            text = "ATMOSPHERIC SENSORS",
            color = LcarsOrange,
            style = MaterialTheme.typography.titleMedium
        )
        
        SensorRow("PRESSURE", sensorData.pressure?.let { "${"%.1f".format(it)} hPa" } ?: "UNAVAILABLE")
        SensorRow("LIGHT", sensorData.light?.let { "${"%.0f".format(it)} lx" } ?: "UNAVAILABLE")
        SensorRow("TEMPERATURE", sensorData.ambientTemp?.let { "${"%.1f".format(it)} C" } ?: "UNAVAILABLE")
        SensorRow("HUMIDITY", sensorData.humidity?.let { "${"%.0f".format(it)} %" } ?: "UNAVAILABLE")
    }
}

@Composable
fun SensorRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = label, color = LcarsBlue, style = MaterialTheme.typography.labelLarge)
        Text(text = value, color = LcarsOrange, style = MaterialTheme.typography.headlineSmall)
    }
}
