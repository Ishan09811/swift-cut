package io.github.swiftcut.ui.editor

import android.net.Uri
import android.view.TextureView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import io.github.swiftcut.R
import io.github.swiftcut.utils.ProjectStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun EditorScreen() {
    var importedVideoFile by remember { mutableStateOf<File?>(null) }
    var selectedTool by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            scope.launch(Dispatchers.IO) {
                val file = ProjectStorage.copyVideoToProject(context, uri!!, "project_1")
                withContext(Dispatchers.Main) {
                    importedVideoFile = file
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            ToolPanel(
                modifier = Modifier.width(90.dp).fillMaxHeight(),
                selectedTool = selectedTool,
                onToolSelected = { selectedTool = it }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.Black)
            ) {
                VideoPreview(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    videoUri = Uri.fromFile(importedVideoFile)
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
            TextureView(context)
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
fun ToolPanel(
    modifier: Modifier = Modifier,
    selectedTool: String? = null,
    onToolSelected: (String) -> Unit = {}
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {

        NavigationRailItem(
            selected = selectedTool == "cut",
            onClick = { onToolSelected("cut") },
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_content_cut), contentDescription = null) },
            label = { Text("Cut") }
        )

        NavigationRailItem(
            selected = selectedTool == "speed",
            onClick = { onToolSelected("speed") },
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_speed), contentDescription = null) },
            label = { Text("Speed") }
        )

        NavigationRailItem(
            selected = selectedTool == "filters",
            onClick = { onToolSelected("filters") },
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_colorize), contentDescription = null) },
            label = { Text("Filters") }
        )

        NavigationRailItem(
            selected = selectedTool == "audio",
            onClick = { onToolSelected("audio") },
            icon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.ic_graphic_eq), contentDescription = null) },
            label = { Text("Audio") }
        )
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


