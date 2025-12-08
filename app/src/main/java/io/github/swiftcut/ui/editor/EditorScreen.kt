package io.github.swiftcut.ui.editor

import android.net.Uri
import android.view.TextureView
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import io.github.swiftcut.R
import io.github.swiftcut.utils.PPMLoader
import io.github.swiftcut.utils.ProjectStorage
import io.github.swiftcut.utils.ProjectStorage.nameWithoutExtension 
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun EditorScreen(projectName: String) {
    var importedVideoFile by remember { mutableStateOf<File?>(null) }
    var selectedTool by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            scope.launch(Dispatchers.IO) {
                val file = ProjectStorage.importVideo(context, uri!!, projectName)
                withContext(Dispatchers.Main) {
                    importedVideoFile = file
                }
            }
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            ToolPanel(
                modifier = Modifier.width(90.dp).fillMaxHeight().windowInsetsPadding(WindowInsets.displayCutout),
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
                    videoUri = if (importedVideoFile != null) Uri.fromFile(importedVideoFile) else null
                )

                var isTimelineSelected by remember { mutableStateOf(false) }
                
                TimelineView(
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                        .background(Color(0xFF121212)),
                    thumbDir = ProjectStorage.getThumbDir(
                        context, 
                        importedVideoFile.nameWithoutExtension(), 
                        projectName
                    ),
                    isSelected = isTimelineSelected,
                    onSelect = { isTimelineSelected = true }
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
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build()
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(videoUri) {
        if (videoUri != null) {
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUri))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }
    
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextureView(context).apply {
                exoPlayer.setVideoTextureView(this)
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
fun TimelineView(
    modifier: Modifier = Modifier, 
    thumbDir: File,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val thumbs = remember(thumbDir) {
        if (!thumbDir.exists()) emptyList()
        else {
            thumbDir.listFiles { f -> f.extension == "ppm" }?.sortedBy { it.name } ?: emptyList()
        }
    }

    val bitmaps = produceState<List<Bitmap?>>(initialValue = emptyList(), thumbs) {
        value = withContext(Dispatchers.IO) { 
            thumbs.map { PPMLoader.loadPPM(it) }
        }
    }

    Box(
        modifier = modifier
            .padding(12.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(bitmaps.value) { bmp ->
                if (bmp != null) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .width(80.dp)
                            .height(60.dp)
                            .padding(end = 4.dp)
                            .background(Color.DarkGray, RoundedCornerShape(6.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(60.dp)
                            .padding(end = 4.dp)
                            .background(Color.Gray, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}





