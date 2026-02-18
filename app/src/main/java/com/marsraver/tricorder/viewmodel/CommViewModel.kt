package com.marsraver.tricorder.viewmodel

import android.app.Application
import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.telephony.CellInfo
import android.telephony.CellInfoLte
import android.telephony.CellSignalStrengthLte
import android.telephony.TelephonyManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

data class CommData(
    val operatorName: String = "UNKNOWN",
    val signalDbm: Int = -120, // Low signal default
    val signalLevel: Int = 0, // 0-4
    val isWifiEnabled: Boolean = false,
    val wifiSsid: String? = null,
    val wifiRssi: Int = -100,
    val scanResults: List<ScanResult> = emptyList()
)

class CommViewModel(application: Application) : AndroidViewModel(application) {
    
    // Telephony
    private val telephonyManager = application.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    
    // Wifi
    private val wifiManager = application.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _commData = MutableStateFlow(CommData())
    val commData = _commData.asStateFlow()

    private var isMonitoring = false

    fun startMonitoring() {
        if (isMonitoring) return
        isMonitoring = true
        viewModelScope.launch(Dispatchers.IO) {
            while (isMonitoring) {
                updateCommData()
                delay(1000) // Update every second
            }
        }
    }

    fun stopMonitoring() {
        isMonitoring = false
    }

    fun toggleWifi() {
        // Deprecated in Android 10+ (Q). Apps cannot toggle WiFi anymore.
        // We can open settings panel instead if needed, but for now we'll try the legacy way 
        // or just show a message if it fails.
        // Actually, let's just trigger a scan if enabled, or do nothing.
        if (wifiManager.isWifiEnabled) {
            @Suppress("DEPRECATION")
            wifiManager.startScan()
        } else {
            // Cannot enable programmatically on modern Android
        }
    }

    private fun updateCommData() {
        val context = getApplication<Application>()
        
        // --- Cellular ---
        var opName = telephonyManager.networkOperatorName ?: "UNKNOWN"
        var dbm = -120
        var level = 0
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
             // For simplicity, we just look for LTE or first available cell info
             // In real app, we'd iterate and find registered cell.
             val allConfig = telephonyManager.allCellInfo
             if (!allConfig.isNullOrEmpty()) {
                 for (info in allConfig) {
                     if (info.isRegistered) {
                         if (info is CellInfoLte) {
                             dbm = info.cellSignalStrength.dbm
                             level = info.cellSignalStrength.level
                         }
                         // Add other types (GSM, WCDMA, NR) as needed, but let's stick to simple logic
                         break 
                     }
                 }
             }
        }
        
        // --- WiFi ---
        val wifiEnabled = wifiManager.isWifiEnabled
        var ssid: String? = null
        var rssi = -100
        var results = emptyList<ScanResult>()
        
        if (wifiEnabled) {
            val connInfo = wifiManager.connectionInfo
            if (connInfo != null && connInfo.networkId != -1) {
                ssid = connInfo.ssid.removePrefix("\"").removeSuffix("\"")
                rssi = connInfo.rssi
            }
            
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                results = wifiManager.scanResults
            }
        }

        _commData.value = CommData(
            operatorName = opName,
            signalDbm = dbm,
            signalLevel = level,
            isWifiEnabled = wifiEnabled,
            wifiSsid = ssid,
            wifiRssi = rssi,
            // Filter empty SSIDs and sort
            scanResults = results
                .filter { !it.SSID.isNullOrBlank() }
                .sortedByDescending { it.level }
                .take(10)
        )
    }
}
