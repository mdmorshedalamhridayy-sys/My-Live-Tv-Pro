package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.PrimaryRed
import com.example.ui.viewmodel.StreamingViewModel

@Composable
fun WatchlistScreen(viewModel: StreamingViewModel) {
    val favorites by viewModel.favorites.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val videos by viewModel.videos.collectAsState()

    // Map favorites back to detailed models
    val favChannels = remember(favorites, channels) {
        favorites.filter { it.contentType == "channel" }.mapNotNull { fav ->
            // Try matching ID
            channels.find { it.id == fav.contentId }
        }
    }

    val favVideos = remember(favorites, videos) {
        favorites.filter { it.contentType == "video" }.mapNotNull { fav ->
            videos.find { it.id == fav.contentId }
        }
    }

    val totalCount = favChannels.size + favVideos.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "My Personal Watchlist (${totalCount})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (totalCount == 0) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Your Watchlist is empty right now.",
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mark live channels or highlights with ❤️ to view them here.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Show channels
                items(favChannels) { ch ->
                    TVChannelCard(
                        channel = ch,
                        onClick = { viewModel.playChannel(ch) },
                        viewModel = viewModel
                    )
                }

                // Show videos
                items(favVideos) { video ->
                    VideoCard(
                        video = video,
                        isDownloading = viewModel.downloadingIds.value.contains(video.id),
                        onPlayClick = { viewModel.playVideo(video) },
                        onDownloadClick = { viewModel.downloadVideo(video) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
