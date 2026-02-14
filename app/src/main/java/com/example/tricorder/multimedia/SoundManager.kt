package com.example.tricorder.multimedia

import android.content.Context
import android.media.SoundPool
import com.example.tricorder.R

class SoundManager(context: Context) {
    private val soundPool = SoundPool.Builder().setMaxStreams(5).build()
    
    private val soundMap = mutableMapOf<Int, Int>()
    
    // Sound IDs matching Tricorder.java
    val switchSound: Int
    val activateSound: Int
    val deactivateSound: Int
    val secondarySound: Int
    val scanLow: Int
    val scanHigh: Int
    
    private var lastStreamId: Int = 0
    
    init {
        // Load sounds
        switchSound = load(context, R.raw.hu)
        activateSound = load(context, R.raw.boop_beep)
        deactivateSound = load(context, R.raw.beep_boop)
        secondarySound = load(context, R.raw.chirp_low)
        scanLow = load(context, R.raw.scan_low)
        scanHigh = load(context, R.raw.scan_high)
    }
    
    private fun load(context: Context, resId: Int): Int {
        return soundPool.load(context, resId, 1)
    }
    
    fun play(soundId: Int) {
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }
    
    fun loop(soundId: Int): Int {
        val streamId = soundPool.play(soundId, 1f, 1f, 1, -1, 1f) // -1 loop forever
        lastStreamId = streamId
        return streamId
    }
    
    fun stop(streamId: Int) {
        soundPool.stop(streamId)
    }
    
    fun stopLast() {
        if (lastStreamId != 0) {
            soundPool.stop(lastStreamId)
            lastStreamId = 0
        }
    }
    
    fun release() {
        soundPool.release()
    }
}
