package io.github.swiftcut.ui.editor

import android.net.Uri
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun EditorScreen() {
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedVideoUri = uri
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {

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
                        .fillMaxWidth(),
                    videoUri = selectedVideoUri
                )

                TimelineView(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF121212))
                )
            }
        }

        // FAB to import video
        FloatingActionButton(
            onClick = {
                videoPickerLauncher.launch("video/mp4")
            },
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Import Video")
        }
    }
}

@Composable
fun VideoPreview(
    modifier: Modifier = Modifier,
    videoUri: Uri? = null
) {
    // Later: attach this SurfaceView to ExoPlayer with setVideoSurface
    AndroidView(
        modifier = modifier,
        factory = { context ->
            SurfaceView(context).apply {
                setZOrderOnTop(false)
            }
        },
        update = { surfaceView ->
            // Not playing yet — just logging selected video for now
            if (videoUri != null) {
                // TODO: Integrate ExoPlayer here in next step
                println("Video selected = $videoUri")
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

