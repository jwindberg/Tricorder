package com.marsraver.tricorder.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader

data class SolarData(
    val imageUrl: String = "https://sohowww.nascom.nasa.gov/data/realtime/eit_304/512/latest.jpg",
    val imageTitle: String = "EIT 304 (Prominences)",
    val sunspotNumber: String = "--",
    val radioFlux: String = "--",
    val solarFlares: String = "--", // C, M, X class
    val lastUpdate: String = "Waiting for data..."
)

class SolarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _solarData = MutableStateFlow(SolarData())
    val solarData = _solarData.asStateFlow()

    private val sunImageUrls = listOf(
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/eit_304/512/latest.jpg", "EIT 304 (Prominences)"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/eit_195/512/latest.jpg", "EIT 195 (Active Regions)"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/eit_284/512/latest.jpg", "EIT 284 (High Temp)"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/eit_171/512/latest.jpg", "EIT 171 (Corona)"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/hmi_mag/512/latest.jpg", "HMI Magnetogram"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/c2/512/latest.jpg", "LASCO C2 (Corona)"),
        Pair("https://sohowww.nascom.nasa.gov/data/realtime/c3/512/latest.jpg", "LASCO C3 (Wide Corona)")
    )
    
    private var currentImageIndex = 0

    init {
        refreshData()
    }

    fun cycleImage() {
        currentImageIndex = (currentImageIndex + 1) % sunImageUrls.size
        val (url, title) = sunImageUrls[currentImageIndex]
        _solarData.value = _solarData.value.copy(
            imageUrl = url,
            imageTitle = title
        )
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch Daily Solar Indices
                val url = URL("https://services.swpc.noaa.gov/text/daily-solar-indices.txt")
                val connection = url.openConnection()
                val reader = BufferedReader(InputStreamReader(connection.getInputStream()))
                
                var lastLine: String? = null
                var line: String? = reader.readLine()
                while (line != null) {
                    if (line.isNotEmpty() && !line.startsWith("#") && !line.startsWith(":")) {
                        lastLine = line
                    }
                    line = reader.readLine()
                }
                reader.close()
                
                if (lastLine != null) {
                    // Parse fixed width. Example:
                    // 2026 01 21  188    166      845      0    -999      *  11  2  0  4  1  0  0
                    // Flux is around index 10-15
                    // Sunspot is around 17-21
                    
                    val parts = lastLine.trim().split("\\s+".toRegex())
                    // parts[0,1,2] = Date
                    // parts[3] = Radio Flux (188)
                    // parts[4] = Sunspot Number (166)
                    // parts[9] = X-Ray Bkgd (*) - wait, index depends on exact spacing? 
                    // Let's rely on split by whitespace for now.
                    
                    if (parts.size >= 10) {
                        val flux = parts[3]
                        val sunspots = parts[4]
                        
                        // C M X flares are usually the last few columns
                        // Let's grab C, M, X specifically if possible.
                        // Format: [Date] [Flux] [Sunspot] [Area] [NewReg] [MeanField] [XRayBg] [C] [M] [X] [S] [1] [2] [3]
                        // Index: 0,1,2 = Date
                        // 3 = Flux
                        // 4 = Sunspot
                        // 5 = Area
                        // 6 = New
                        // 7 = Field
                        // 8 = Bg
                        // 9 = C
                        // 10 = M
                        // 11 = X
                        
                        val cFlares = parts.getOrNull(9) ?: "0"
                        val mFlares = parts.getOrNull(10) ?: "0"
                        val xFlares = parts.getOrNull(11) ?: "0"
                        
                        val flares = "C:$cFlares M:$mFlares X:$xFlares"
                        
                        _solarData.value = _solarData.value.copy(
                            radioFlux = flux,
                            sunspotNumber = sunspots,
                            solarFlares = flares,
                            lastUpdate = "Updated: ${parts[0]}-${parts[1]}-${parts[2]}"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _solarData.value = _solarData.value.copy(lastUpdate = "Error fetching data")
            }
        }
    }
}
