package com.example.tricorder.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.tricorder.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.ui.theme.LcarsDimensions
import com.example.tricorder.ui.screens.TricorderMode

/**
 * Standard button component, additional to primitives if needed.
 */
@Composable
fun LcarsButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = LcarsSourceColors.COL_COM,
    shape: Shape = RoundedCornerShape(16.dp),
    textColor: Color = Color.Black
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}

/**
 * A classic LCARS elbow shape placeholder.
 */
@Composable
fun LcarsElbow(
    color: Color,
    width: Dp = 120.dp,
    height: Dp = 80.dp,
    cornerSize: Dp = 40.dp,
    bottomExtension: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.width(width).height(height + bottomExtension)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(bottomStart = cornerSize))
                .background(color)
        )
    }
}

@Composable
fun LcarsScaffold(
    currentMode: TricorderMode,
    onModeSelected: (TricorderMode) -> Unit,
    auxButtonText: String? = null,
    onAuxButtonClick: () -> Unit = {},
    content: @Composable BoxWithConstraintsScope.() -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(LcarsSourceColors.COL_BG)
            .padding(top = 32.dp) // Shift down to avoid camera hole
    ) {
        val navWidth = LcarsDimensions.NAV_PANEL_WIDTH.dp
        val topBarHeight = LcarsDimensions.HEADER_HEIGHT.dp
        val interPad = LcarsDimensions.INTER_PAD.dp

        // Main layout
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // Left Navigation Bar
            // Construct button list strictly for LcarsNavigationBar signature
            val navButtons = listOf(
                Triple("GRA", LcarsSourceColors.COL_GRA, { onModeSelected(TricorderMode.GRA) }),
                Triple("MAG", LcarsSourceColors.COL_MAG, { onModeSelected(TricorderMode.MAG) }),
                Triple("ACO", LcarsSourceColors.COL_AUD, { onModeSelected(TricorderMode.AUD) }),
                Triple("GEO", LcarsSourceColors.COL_GEO, { onModeSelected(TricorderMode.GEO) }),
                Triple("EMS", LcarsSourceColors.COL_COM, { onModeSelected(TricorderMode.EMS) }),
                Triple("SOL", LcarsSourceColors.COL_SOL, { onModeSelected(TricorderMode.SOL) })
            )
            
            LcarsNavigationBar(
                buttons = navButtons,
                width = navWidth,
                gap = 4.dp,
                modifier = Modifier.fillMaxHeight()
            )

            Column(modifier = Modifier.weight(1f)) {
                // Top Header Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topBarHeight)
                ) {
                    // Calculate strict titleHeight
                    val titleHeight = topBarHeight * 0.51f
                    
                    // 1. The Graphic Header (Swoop with Stardate)
                    LcarsHeader(
                        text = "47634.44", // Stardate
                        color = currentMode.color,
                        navWidth = navWidth,
                        height = topBarHeight,
                        titleHeight = titleHeight,
                        modifier = Modifier.width(navWidth * 1.5f)
                    )

                    // 2. The Title "topLabel" (Center)
                    // Placed to the right of LcarsHeader
                    // Use a Box or Row to position it?
                    // Source: toRightOf(swoopCorner), width = (w - navW*1.5)/1.75
                    
                    Box(
                        modifier = Modifier
                            .padding(start = navWidth * 1.5f) // Start after Swoop
                            .fillMaxWidth(0.55f) // Approximate width share (1/1.75 ~ 0.57)
                            .height(titleHeight) // Same height as title bar part
                            .padding(start = 8.dp) // innerGap
                    ) {
                        LcarsNavButton(
                            text = stringResource(currentMode.titleResId), // "ACCELERATION"
                            color = currentMode.color,
                            modifier = Modifier.fillMaxSize(),
                            onClick = {} // Title isn't clickable? well NavButton is.
                        )
                    }

                    // 3. Dynamic Aux Button (Top Right)

                    // Dynamic Aux Button (Top Right)
                    if (auxButtonText != null) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                // In source: margins were (innerGap, 0, 0, 0) relative to right of label?
                                // Let's sit it inside the bar for now.
                                .padding(end = interPad, top = 4.dp) 
                        ) {
                             // Using LcarsNavButton from PrimitiveComponents
                            LcarsNavButton(
                                text = auxButtonText,
                                color = currentMode.color, 
                                modifier = Modifier
                                    .width(navWidth * 0.8f) // Match side button width approx
                                    .height(titleHeight * 0.9f), // Fit in title bar
                                onClick = onAuxButtonClick
                            )
                        }
                    }
                }

                // Main Content Area
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(interPad)
                ) {
                    content()
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun LcarsScaffoldPreview() {
    LcarsScaffold(
        currentMode = TricorderMode.GRA,
        onModeSelected = {},
        auxButtonText = "SCAN"
    ) {
        Text("MAIN SENSOR DISPLAY", color = LcarsSourceColors.COL_COM, style = MaterialTheme.typography.displayMedium)
    }
}
