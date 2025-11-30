
package io.github.swiftcut.ui.editor

import android.view.SurfaceView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun EditorScreen() {
    Row(modifier = Modifier.fillMaxSize()) {
        ToolPanel(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(Color(0xFF1A1A1A))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {

            VideoPreview(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            TimelineView(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .background(Color(0xFF121212))
            )
        }
    }
}

@Composable
fun VideoPreview(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).apply {
                setZOrderOnTop(false)
            }
        }
    )
}

@Composable
fun ToolPanel(modifier: Modifier) {
    Column(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ToolButton("Cut")
        ToolButton("Speed")
        ToolButton("Filters")
        ToolButton("Audio")
    }
}

@Composable
fun ToolButton(text: String) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .background(Color.DarkGray, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun TimelineView(modifier: Modifier = Modifier) {
    LazyRow(
        modifier = modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(20) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
                    .padding(end = 4.dp)
                    .background(Color.Gray, shape = RoundedCornerShape(6.dp))
            )
        }
    }
}

