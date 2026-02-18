package com.marsraver.tricorder.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marsraver.tricorder.ui.theme.LcarsSourceColors



/**
 * Reimplementation of ElementWrapper.java
 * Container with HeaderBar, Right SideBar, and Child Content.
 */
@Composable
fun LcarsElementWrapper(
    title: String,
    gridColor: Color, // header color
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Layout:
    // Header (Top)
    // Sidebar (Right, fills height)
    // Content (Left of sidebar, Below header)
    
    // Decompiled: 
    // chartTop = bounds.top + headHeight + gap
    // barLeft = bounds.right - sidebarWidth
    // child rect = left, chartTop, barLeft - gap, bottom
    
    val sideBarWidth = 8.dp // Match SIDE_BAR_WIDTH from LcarsSystemComponents (was 16.dp, user said too thin? No, user said existing audio curve was wrong size. System one is 8dp base but layout is different)
    // User liked GRA/MAG. GRA uses LcarsVectorElement -> LcarsHeaderBarElement.
    // LcarsHeaderBarElement uses SIDE_BAR_WIDTH=8.dp.
    
    val headerHeight = 50.dp // GRA uses 50.dp for Vector, 40.dp for Magnitude. Let's use 50dp to be safe for title.
    val gap = 4.dp
    
    Column(modifier = modifier) {
        // Header
        LcarsHeaderBarElement(
            text = title,
            color = gridColor,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        )
        
        Spacer(modifier = Modifier.height(gap))
        
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Content
            Box(
                 modifier = Modifier
                     .weight(1f)
                     .fillMaxHeight()
            ) {
                content()
            }
            
            Spacer(modifier = Modifier.width(gap))
            
            // SideBar
            Box(
                modifier = Modifier
                    .width(sideBarWidth)
                    .fillMaxHeight()
                    .background(gridColor)
            )
        }
    }
}
