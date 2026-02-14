package com.example.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tricorder.ui.theme.LcarsSourceColors
import com.example.tricorder.ui.theme.LcarsDimensions

@Composable
fun LcarsHeader(
    text: String,
    color: Color,
    navWidth: Dp,
    height: Dp,
    titleHeight: Dp = height * 0.5f, // Default if not provided
    modifier: Modifier = Modifier
) {
    // HeaderBar.java logic:
    // height (h) is the full view height (topBarHeight).
    // titleHeight is the height of the horizontal bar part.
    // curve goes from (0, h) to (navW, 0).
    // line to (w, 0).
    // line to (w, titleHeight).
    // line to (navW + navW/2, titleHeight).
    // inner curve arc back to (navWidth, h).
    
    Box(modifier = modifier.height(height).fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val navW = navWidth.toPx()
            val tH = titleHeight.toPx()
            val innerWidth = navW / 2
            
            // outerCurve: RectF(0, 0, navW * 2, h * 2)
            // innerCurve: RectF(navW, tH, navW + innerWidth * 2, h + (h - tH))
            
            val outerRect = Rect(0f, 0f, navW * 2, h * 2)
            val subHeight = h - tH
            val innerRect = Rect(navW, tH, navW + innerWidth * 2, h + subHeight)

            val path = Path().apply {
                moveTo(0f, h)
                // arcTo(outerCurve, 180, 90)
                arcTo(
                    rect = outerRect,
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 90f,
                    forceMoveTo = false
                )
                lineTo(w, 0f)
                lineTo(w, tH)
                lineTo(navW + innerWidth, tH)
                
                // arcTo(innerCurve, 270, -90)
                arcTo(
                    rect = innerRect,
                    startAngleDegrees = 270f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                close()
            }

            drawPath(path, color)
        }
        
        // Stardate Text
        // HeaderBar.java: text1 (Stardate) at x = w - textWidth - 3, y = titleHeight * 0.8
        
        Text(
            text = text, 
            color = Color.Black,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp, // Tweak as needed
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp, top = 8.dp) // Approximate visual alignment
        )
    }
}

@Composable
fun LcarsNavButton(
    text: String,
    isParams: Boolean = false, // Aux button vs Mode button
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // NavButton.java:
    // TextColor: Black
    // Gravity: Bottom Right (Alignment.BottomEnd)
    // ScaleX: 0.5f
    // Typeface: Monospace
    
    Box(
        modifier = modifier
            .background(color)
            .clickable(onClick = onClick)
            .padding(end = 4.dp), // setPadding(0, 0, 4, 0)
        contentAlignment = Alignment.BottomEnd
    ) {
        Text(
            text = text,
            color = LcarsSourceColors.COL_TEXT_BLACK,
            style = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp, // Approximate
            ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun LcarsNavigationBar(
    buttons: List<Triple<String, Color, () -> Unit>>,
    width: Dp,
    gap: Dp = 4.dp, // Gauge.getInnerGap() approx
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(width),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        buttons.forEach { (text, color, onClick) ->
            LcarsNavButton(
                text = text,
                color = color,
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        }
    }
}
