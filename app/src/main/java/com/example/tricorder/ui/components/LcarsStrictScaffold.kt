package com.example.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tricorder.ui.screens.TricorderMode

import androidx.compose.ui.zIndex
import com.example.tricorder.ui.theme.LcarsSourceColors
import androidx.compose.ui.res.stringResource
import com.example.tricorder.R

// Port of HeaderBar.java (The Top-Left Swoop)
@Composable
fun LcarsSwoop(
    color: Color,
    navWidth: Dp,
    topBarHeight: Dp,
    titleHeight: Dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val nw = navWidth.toPx()
        val th = titleHeight.toPx()
        val iw = nw / 2
        val subH = h - th
        
        // Rect definitions from HeaderBar.onSizeChanged
        val outerCurve = Rect(0f, 0f, nw * 2, h * 2)
        val innerCurve = Rect(nw, th, nw + iw * 2, h + subH)
        
        val path = Path().apply {
            moveTo(0f, h)
            // arcTo(outerCurve, 180, 90) -> Left to Top
            arcTo(outerCurve, 180f, 90f, false)
            lineTo(w, 0f)
            lineTo(w, th)
            lineTo(nw + iw, th)
            // arcTo(innerCurve, 270, -90) -> Top to Left
            arcTo(innerCurve, 270f, -90f, false)
            close()
        }
        
        drawPath(path, color)
    }
}

@Composable
fun LcarsStrictScaffold(
    currentMode: TricorderMode,
    onModeSelected: (TricorderMode) -> Unit,
    auxButtonText: String? = null,
    onAuxButtonClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    // Dimensions
    // Dimensions
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val minDim = if (screenWidth < screenHeight) screenWidth else screenHeight

    // Dynamic calculations based on original Tricorder spec (minDim * 0.22)
    val navWidth = minDim * 0.22f
    val topBarHeight = minDim * 0.16f // Roughly 64dp on 400dp width
    val titleHeight = topBarHeight * 0.55f // Proportional title height

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 50.dp) // Avoid camera hole (nudged +2dp)
    ) {
        // TOP BAR ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarHeight)
                .zIndex(1f) // Ensure overlapping text draws on top of content
        ) {
            // SWOOP VIEW
            Box(modifier = Modifier.width(navWidth * 2.0f).fillMaxHeight()) {
                 LcarsSwoop(
                    color = currentMode.color,
                    navWidth = navWidth,
                    topBarHeight = topBarHeight,
                    titleHeight = titleHeight,
                    modifier = Modifier.fillMaxSize()
                )
                // Stardate (Top Right of Swoop View - Extended Bar)
                // Aligned vertically with Title (Center of titleHeight) and matching font.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .height(titleHeight) // Constrain height to title bar strip
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                     Text(
                        text = com.example.tricorder.util.Stardate.getStardate(),
                        color = Color.Black,
                        fontSize = (titleHeight.value * 0.55f).sp, // Dynamic Font Size
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.graphicsLayer(scaleX = 0.7f, transformOrigin = TransformOrigin(1f, 0.5f)) // Center pivot for vertical align? No, CenterEnd align handles pos.
                    )
                }

                // Time (Centered in Nav Width, Lowered)
                // Using offset to position below header without expanding header height
                Box(
                    modifier = Modifier
                        .width(navWidth)
                        .align(Alignment.TopStart)
                        .offset(y = 75.dp), // Explicit offset to lower text (~titleHeight + 40dp)
                    contentAlignment = Alignment.TopCenter // Horizontally Centered
                ) {
                    Text(
                        text = com.example.tricorder.util.Stardate.getTime(),
                        color = Color.Black, // Changed to Black because it overlays the colored Nav Bar
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .wrapContentWidth(unbounded = true)
                            .graphicsLayer(scaleX = 0.7f, transformOrigin = TransformOrigin(0.5f, 0f))
                    )
                }
            }

            
            // TOP LABEL ("ACCELERATION", "MAGNETIC FLUX")
            Box(
                modifier = Modifier
                    .weight(0.7f) // Reduced to 70% to give SCAN button plenty of room
                    .height(titleHeight)
                    .padding(start = 4.dp)
                    .background(currentMode.color),
                contentAlignment = Alignment.CenterStart // Left Aligned to use free space
            ) {
                 Text(
                    text = stringResource(currentMode.titleResId), 
                    color = Color.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = (titleHeight.value * 0.55f).sp, // Dynamic Font Size
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Visible, // Ensure it draws outside bounds if needed
                    modifier = Modifier
                        .wrapContentWidth(align = Alignment.Start, unbounded = true) // Align Start to prevent clipping left "M"
                        .padding(start = 12.dp) // Reverted to 12.dp padding as requested
                        .graphicsLayer(scaleX = 0.7f, transformOrigin = TransformOrigin(0f, 0.5f))
                )
            }
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // AUX BUTTON ("SCAN")
            Box(
                modifier = Modifier
                    .weight(0.3f) // Increased to 30% width to prevent squashing
                    .height(titleHeight)
                    .background(currentMode.color)
                    .clickable { onAuxButtonClick() },
                contentAlignment = Alignment.CenterEnd
            ) {
                if (auxButtonText != null) {
                    Text(
                        text = auxButtonText,
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontSize = (titleHeight.value * 0.55f).sp, 
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier
                            .wrapContentWidth(unbounded = true) // Allow overflowing the narrow box
                            .padding(end = 12.dp) // Adjusted padding
                            .graphicsLayer(scaleX = 0.7f, transformOrigin = TransformOrigin(1f, 0.5f))
                    )
                }
            }
        }
        
        // MAIN CONTENT ROW
        Row(modifier = Modifier.weight(1f)) {
            // LEFT NAV BAR
            // LEFT NAV BAR
            Column(
                modifier = Modifier
                    .width(navWidth)
                    .fillMaxHeight()
                    // Removed padding(top=4.dp) to allow flush connection to swoop
            ) {
                // Top Pad (Source: topPad, fills rem/2)
                Spacer(modifier = Modifier.weight(0.5f).fillMaxWidth().background(currentMode.color))
                
                // NavigationBar implementation
                val modes = TricorderMode.values()
                modes.forEach { mode ->
                    val isSelected = (mode == currentMode)
                    
                    // Gap above button (Source: setMargins(0, gap, 0, 0))
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(mode.color) // Always use the mode's own color
                            .clickable { onModeSelected(mode) },
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Text(
                            text = mode.name,
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontSize = (navWidth.value * 0.22f).sp, // Dynamic based on column width
                            modifier = Modifier
                                .padding(bottom = 4.dp, end = 4.dp)
                                .graphicsLayer(scaleX = 0.7f, transformOrigin = TransformOrigin(1f, 1f))
                        )
                    }
                }
                
                // Bottom Pad (Source: bottomPad, fills rem/2 if rem exists)
                // Source adds gap above bottomPad too? "layoutParams2.setMargins(0, gap, 0, 0)"
                Spacer(modifier = Modifier.height(4.dp))
                Spacer(modifier = Modifier.weight(0.5f).fillMaxWidth().background(currentMode.color))
            }
            
            // CONTENT AREA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 8.dp) // Removed top padding
            ) {
                content()
            }
        }
    }
}
