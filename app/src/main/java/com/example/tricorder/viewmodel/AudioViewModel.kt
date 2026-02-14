package com.example.tricorder.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class AudioViewModel(application: Application) : AndroidViewModel(application) {

    private val sampleRate = 44100
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    private val _waveform = MutableStateFlow(FloatArray(0))
    val waveform = _waveform.asStateFlow()
    
    private val _spectrum = MutableStateFlow(FloatArray(0))
    val spectrum = _spectrum.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    @SuppressLint("MissingPermission") // Checked in UI or assumed for this demo/template context
    fun startRecording() {
        if (isRecording) return

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()
            isRecording = true

            viewModelScope.launch(Dispatchers.IO) {
                val buffer = ShortArray(bufferSize)
                while (isActive && isRecording) {
                    val readResult = audioRecord?.read(buffer, 0, bufferSize) ?: 0
                    if (readResult > 0) {
                        // Normalize waveform
                        val floats = FloatArray(readResult) { i -> buffer[i] / 32768f }
                        _waveform.value = floats
                        
                         // Simple FFT (Magnitude only)
                         // For performance, we might want to downsample or use a smaller window
                         val fftSize = 128
                         if (readResult >= fftSize) {
                             val fftData = computeFFT(floats.sliceArray(0 until fftSize))
                             _spectrum.value = fftData
                         }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        audioRecord = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }
    
    // Very simple DFT/FFT implementation for visualization
    private fun computeFFT(input: FloatArray): FloatArray {
        val n = input.size
        val output = FloatArray(n / 2)
        
        for (k in 0 until n / 2) {
            var sumReal = 0.0
            var sumImag = 0.0
            
            for (t in 0 until n) {
                val angle = 2 * PI * t * k / n
                sumReal += input[t] * cos(angle)
                sumImag -= input[t] * sin(angle)
            }
            
            output[k] = kotlin.math.sqrt(sumReal * sumReal + sumImag * sumImag).toFloat()
        }
        return output
    }
    
    fun startScanning() {
        if (!isRecording) {
            startRecording()
        }
    }
    
    fun stopScanning() {
        stopRecording()
    }
}
