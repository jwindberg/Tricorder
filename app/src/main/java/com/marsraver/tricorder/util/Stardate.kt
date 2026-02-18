package com.marsraver.tricorder.util

import java.util.Calendar
import java.util.Locale

object Stardate {
    /**
     * Calculates Stardate using: 1000 * (Y - 2323) + 1000/n * (D + H/24)
     * Example for 2026-02-13: ~ -296880.3
     */
    fun getStardate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR) - 1.0 // 0-indexed day
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val daysInYear = if (isLeapYear(year)) 366.0 else 365.0
        
        // Fraction of day: H/24. 
        // User formula says (D + H/24). 
        // We'll include minutes for slightly better precision or stick to hour? 
        // User example 16.41 implies time is separate.
        // For the Stardate calculation itself, usually it includes full time resolution.
        val timeFraction = (hour + minute / 60.0) / 24.0
        
        val stardate = 1000 * (year - 2323) + (1000 / daysInYear) * (dayOfYear + timeFraction)
        
        return String.format(Locale.US, "%.1f", stardate)
    }

    /**
     * Calculates Metric Time: HH.ff (Hour + fraction of hour)
     * Example: 16:25 -> 16 + 25/60 = 16.41
     */
    fun getTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        val metricTime = hour + (minute / 60.0)
        return String.format(Locale.US, "%.2f", metricTime)
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}
