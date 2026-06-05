package com.example.ui.screens

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownloadDone
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.viewmodel.StreamingViewModel

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(viewModel: StreamingViewModel) {
    val context = LocalContext.current
    val playbackChannel by viewModel.playbackChannel.collectAsState()
    val playbackVideo by viewModel.playbackVideo.collectAsState()
    val selectedResolution by viewModel.selectedResolution.collectAsState()

    // Determine target URL and title safely
    val targetUrl = playbackChannel?.streamUrl ?: playbackVideo?.videoUrl ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    val targetTitle = playbackChannel?.name ?: playbackVideo?.title ?: "Live Streaming Program"
    val targetCategory = playbackChannel?.category ?: playbackVideo?.category ?: "Live TV"

    // Construct and memoize standard ExoPlayer lifecycle safely!
    val exoPlayer = remember(context, targetUrl) {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(targetUrl)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    // Auto-release player on navigation disposal
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    var showControlsMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Android Media3 Video Player Interop View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    setKeepScreenOn(true)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Custom HUD Controls Overlay at the TOP of the screen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo("home") },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Exit Player")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(PrimaryRed, shape = MaterialTheme.shapes.extraSmall)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (playbackChannel != null) "LIVE TV" else "VOD STREAM",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = targetCategory.uppercase(),
                                fontSize = 10.sp,
                                color = AccentGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = targetTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 2.dp),
                            maxLines = 1
                        )
                    }
                }

                // Bandwidth / Multi-resolution settings action trigger
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box {
                        Button(
                            onClick = { showControlsMenu = !showControlsMenu },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray.copy(alpha = 0.6f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Resolution", modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = selectedResolution, fontSize = 10.sp, color = Color.White)
                        }

                        DropdownMenu(
                            expanded = showControlsMenu,
                            onDismissRequest = { showControlsMenu = false },
                            modifier = Modifier.background(Color(0xFF22212E))
                        ) {
                            listOf(
                                "1080p (HQ Auto)", 
                                "720p (Standard HD)", 
                                "480p (Medium Buffer)", 
                                "360p (Data Saver Mode)"
                            ).forEach { resOpt ->
                                DropdownMenuItem(
                                    text = { Text(resOpt, fontSize = 11.sp, color = Color.White) },
                                    onClick = {
                                        viewModel.updatePlayerResolution(resOpt)
                                        showControlsMenu = false
                                        Toast.makeText(
                                            context,
                                            "Bandwidth optimized: Switched codec to $resOpt",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    // VOD specific offline download inside player HUD
                    if (playbackVideo != null) {
                        val video = playbackVideo!!
                        val isDownloading = viewModel.downloadingIds.collectAsState().value.contains(video.id)
                        
                        if (video.isDownloaded) {
                            Icon(
                                imageVector = Icons.Default.FileDownloadDone,
                                contentDescription = "Cached",
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (isDownloading) {
                            CircularProgressIndicator(
                                color = AccentGold,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            IconButton(
                                onClick = { viewModel.downloadVideo(video) },
                                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                            ) {
                                Icon(imageVector = Icons.Default.Download, contentDescription = "Download Video")
                            }
                        }
                    }
                }
            }
        }
    }
}
