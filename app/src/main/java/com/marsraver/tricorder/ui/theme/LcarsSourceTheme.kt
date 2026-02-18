package com.marsraver.tricorder.ui.theme

import androidx.compose.ui.graphics.Color

object LcarsSourceColors {
    // Tricorder.java constants
    val COL_BG = Color(0xFF000000) // -16777216

    val COL_TEXT_BLACK = Color(0xFF000000) // -16777216

    val COL_TEXT = Color(0xFFFF9900) // Standard Orange
    val COL_TEXT_MED = Color(0xFFCC9966) // Beige/Medium Text

    // TridataView.java Colors
    val COL_BEIGE = Color(0xFFDBCF6B) // -2380181 (0xFFDBCF6B) - Matches Tricorder.java GRA color
    val COL_POINTER_RED = Color(0xFFFF0000) // -65536
    
    // ViewDefinition Colors (TricorderView.java)
    val COL_GRA = Color(0xFFDBB66B) // -2380181
    val COL_MAG = Color(0xFF5DADE3) // -10633005
    
    // Actual Instantiation Colors (createGui)
    // GRA: -3355393 (0xFFCCCEFF), -24989 (0xFFFF9E63)
    val COL_GRA_GRID = Color(0xFFCCCEFF) // Lavender
    val COL_GRA_PLOT = Color(0xFFFF9E63) // Orange
    
    // MAG Identity
    val COL_MAG_GRID = Color(0xFF6666FF) // Strict Teal/Periwinkle Grid
    val COL_MAG_PLOT = Color(0xFFFFCC00) // Strict Gold/Yellow Plot (Values, Chart)
    val COL_RED_STRICT = Color(0xFFFF0000) // -65536
    
    val COL_BRIGHT_RED = Color(0xFFFF0000)
    val COL_BRIGHT_GREEN = Color(0xFF00FF00)
    val COL_YELLOW_LCARS = Color(0xFFFFCC00)
    val COL_AUD = Color(0xFF9C6B6E) // -6526098
    val COL_GEO = Color(0xFFB9A9C4) // -4609596
    val COL_COM = Color(0xFFFF9C63) // -25501
    val COL_SOL = Color(0xFFFF9900) // -26368

    // TridataView.java
    val XYZ_PLOT_COLS = listOf(
        Color(0xFFFF0000), // -65536
        Color(0xFF00FF00), // -16711936
        Color(0xFF0000FF)  // -16776961
    )
    
    // Aliases for compatibility with my previous code
    val COL_POINTER = Color(0xFFFFCC99) // Light Orange Pointer
    val COL_TEXT_MED_LCARS = Color(0xFFCC99CC)
}

object LcarsDimensions {
    // Tricorder.java createGui() logic
    // navBarWidth = minDim * 0.22f
    // topBarHeight = minDim * 0.15f
    // innerGap = minDim / 100
    // textScaleX = 0.6f (0.5f in NavButton)
    
    const val FONT_SCALE_X_DEFAULT = 0.6f
    const val FONT_SCALE_X_BUTTON = 0.5f
    
    // Approximate constants for layout
    const val RATIO_NAV_WIDTH = 0.22f // of minDim
    const val RATIO_TOP_BAR_HEIGHT = 0.15f // of minDim
    const val RATIO_TITLE_HEIGHT = 0.0762f // derived from 0.0635 * 1.2
    
    val NAV_PANEL_WIDTHRatio = 0.22f
    val HEADER_HEIGHTRatio = 0.15f
    
    const val NAV_PANEL_WIDTH = 120 // dp (fallback)
    const val HEADER_HEIGHT = 80 // dp (fallback)
    const val SCREEN_HEIGHT = 800 // dp (reference)
    const val SCREEN_WIDTH = 480 // dp (reference)
    const val INTER_PAD = 8 // dp
}
