package com.marsraver.tricorder.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SensorData(
    val gravity: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val gravityHistory: List<Float> = emptyList(),
    val magneticField: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val magneticHistory: List<Float> = emptyList(),
    val accelerometer: Triple<Float, Float, Float> = Triple(0f, 0f, 0f),
    val accelerometerHistory: List<Float> = emptyList(), 
    val rotationVector: FloatArray? = null, // New Fused Sensor Data
    val pressure: Float? = null,
    val light: Float? = null,
    val ambientTemp: Float? = null,
    val humidity: Float? = null,
    val networkLocation: android.location.Location? = null,
    val gpsLocation: android.location.Location? = null,
    val gnssStatus: android.location.GnssStatus? = null,
    val declination: Float = 0f // Magnetic Declination in degrees
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SensorData

        if (gravity != other.gravity) return false
        if (gravityHistory != other.gravityHistory) return false
        if (magneticField != other.magneticField) return false
        if (magneticHistory != other.magneticHistory) return false
        if (accelerometer != other.accelerometer) return false
        if (accelerometerHistory != other.accelerometerHistory) return false
        if (rotationVector != null) {
            if (other.rotationVector == null) return false
            if (!rotationVector.contentEquals(other.rotationVector)) return false
        } else if (other.rotationVector != null) return false
        
        if (pressure != other.pressure) return false
        if (light != other.light) return false
        if (ambientTemp != other.ambientTemp) return false
        if (humidity != other.humidity) return false
        if (networkLocation != other.networkLocation) return false
        if (gpsLocation != other.gpsLocation) return false
        if (gnssStatus != other.gnssStatus) return false
        if (declination != other.declination) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gravity.hashCode()
        result = 31 * result + gravityHistory.hashCode()
        result = 31 * result + magneticField.hashCode()
        result = 31 * result + magneticHistory.hashCode()
        result = 31 * result + accelerometer.hashCode()
        result = 31 * result + accelerometerHistory.hashCode()
        result = 31 * result + (rotationVector?.contentHashCode() ?: 0)
        result = 31 * result + (pressure?.hashCode() ?: 0)
        result = 31 * result + (light?.hashCode() ?: 0)
        result = 31 * result + (ambientTemp?.hashCode() ?: 0)
        result = 31 * result + (humidity?.hashCode() ?: 0)
        result = 31 * result + (networkLocation?.hashCode() ?: 0)
        result = 31 * result + (gpsLocation?.hashCode() ?: 0)
        result = 31 * result + (gnssStatus?.hashCode() ?: 0)
        result = 31 * result + declination.hashCode()
        return result
    }
}

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    // Sensors
    private val gravitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    private val magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _sensorData = MutableStateFlow(SensorData())
    val sensorData = _sensorData.asStateFlow()

    // Environmental Sensors
    private val pressureSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    private val ambientTempSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    private val humiditySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)
    
    // Location
    private val locationManager = application.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    
    // Gnss Status
    private val gnssStatusCallback = object : android.location.GnssStatus.Callback() {
        override fun onSatelliteStatusChanged(status: android.location.GnssStatus) {
            val currentData = _sensorData.value
            _sensorData.value = currentData.copy(gnssStatus = status)
        }
    }

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: android.location.Location) {
             android.util.Log.d("SensorViewModel", "Location Received from ${location.provider}: ${location.latitude}, ${location.longitude}")
             val currentData = _sensorData.value
             val geoField = android.hardware.GeomagneticField(
                 location.latitude.toFloat(),
                 location.longitude.toFloat(),
                 location.altitude.toFloat(),
                 System.currentTimeMillis()
             )
             
             if (location.provider == android.location.LocationManager.GPS_PROVIDER) {
                 _sensorData.value = currentData.copy(
                     gpsLocation = location,
                     declination = geoField.declination
                 )
             } else {
                 _sensorData.value = currentData.copy(
                     networkLocation = location,
                     declination = geoField.declination
                 )
             }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: android.os.Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    init {
        startListening()
    }

    private fun startListening() {
        val sensors = listOfNotNull(
            gravitySensor, magneticSensor, accelerometer, rotationVectorSensor,
            pressureSensor, lightSensor, ambientTempSensor, humiditySensor
        )
        
        sensors.forEach { sensor ->
            // Use GAME delay for smooth UI updates
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        
        val currentData = _sensorData.value
        
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                 _sensorData.value = currentData.copy(
                     rotationVector = event.values.clone() 
                 )
            }
            Sensor.TYPE_GRAVITY -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = kotlin.math.sqrt(x*x + y*y + z*z)
                
                val newHistory = (currentData.gravityHistory + mag).takeLast(100)
                
                _sensorData.value = currentData.copy(
                    gravity = Triple(x, y, z),
                    gravityHistory = newHistory
                )
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = kotlin.math.sqrt(x*x + y*y + z*z)
                
                val newHistory = (currentData.magneticHistory + mag).takeLast(100)
                
                _sensorData.value = currentData.copy(
                    magneticField = Triple(x, y, z),
                    magneticHistory = newHistory
                )
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val mag = kotlin.math.sqrt(x*x + y*y + z*z)
                
                val newHistory = (currentData.accelerometerHistory + mag).takeLast(100)
                
                // FALLBACK: If standard Gravity sensor is missing, use Accelerometer
                val updatedGravity = if (gravitySensor == null) Triple(x, y, z) else currentData.gravity
                val updatedGravityHistory = if (gravitySensor == null) newHistory else currentData.gravityHistory
                
                _sensorData.value = currentData.copy(
                    accelerometer = Triple(x, y, z),
                    accelerometerHistory = newHistory,
                    gravity = updatedGravity,
                    gravityHistory = updatedGravityHistory
                )
            }
            Sensor.TYPE_PRESSURE -> _sensorData.value = currentData.copy(pressure = event.values[0])
            Sensor.TYPE_LIGHT -> _sensorData.value = currentData.copy(light = event.values[0])
            Sensor.TYPE_AMBIENT_TEMPERATURE -> _sensorData.value = currentData.copy(ambientTemp = event.values[0])
            Sensor.TYPE_RELATIVE_HUMIDITY -> _sensorData.value = currentData.copy(humidity = event.values[0])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if necessary
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
    
    fun stopListening() {
        sensorManager.unregisterListener(this)
        locationManager.removeUpdates(locationListener)
        locationManager.unregisterGnssStatusCallback(gnssStatusCallback)
    }
    
    fun refreshLocationUpdates() {
        android.util.Log.d("SensorViewModel", "Refreshing Location Updates requested by UI")
        try {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    getApplication(), 
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                 android.util.Log.d("SensorViewModel", "Permission Granted - Requesting Updates")
                 locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER, 
                    1000L, 
                    0f, 
                    locationListener
                 )
                 locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER, 
                    1000L, 
                    0f, 
                    locationListener
                 )
                 locationManager.registerGnssStatusCallback(gnssStatusCallback, null)
            } else {
                 android.util.Log.e("SensorViewModel", "Permission Still Not Granted during refresh")
            }
        } catch (e: Exception) {
            android.util.Log.e("SensorViewModel", "Error refreshing location updates", e)
        }
    }
    
    fun startScanning() {
        startListening()
    }
}
