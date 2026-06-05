package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.VideoEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.FocusedBorder
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.viewmodel.StreamingViewModel

@Composable
fun VODScreen(viewModel: StreamingViewModel) {
    val videos by viewModel.videos.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val downloadingIds by viewModel.downloadingIds.collectAsState()

    // Retrieve unique categories dynamically
    val categories = remember(videos) {
        listOf("All") + videos.map { it.category }.distinct()
    }

    // Filter list
    val filteredVideos = remember(videos, searchQuery, selectedCategory) {
        videos.filter { video ->
            val matchesQuery = video.title.contains(searchQuery, ignoreCase = true) || 
                               video.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == null || selectedCategory == "All" || video.category == selectedCategory
            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar Row
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchQuery.value = it },
            placeholder = { Text("Search matches, highlights, shows...", fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AccentGold) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            singleLine = true
        )

        // Category Scroll Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(categories) { cat ->
                val isSelected = (selectedCategory == cat) || (selectedCategory == null && cat == "All")
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedCategory.value = if (cat == "All") null else cat },
                    label = { Text(cat, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryRed,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceCard,
                        labelColor = Color.LightGray
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = Color.White.copy(alpha = 0.05f),
                        borderWidth = 1.dp,
                        selectedBorderColor = AccentGold
                    )
                )
            }
        }

        // Grid contents
        if (filteredVideos.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No sports videos found in current query.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 170.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredVideos) { video ->
                    val isDownloading = downloadingIds.contains(video.id)
                    VideoCard(
                        video = video,
                        isDownloading = isDownloading,
                        onPlayClick = { viewModel.playVideo(video) },
                        onDownloadClick = { viewModel.downloadVideo(video) },
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCard(
    video: VideoEntity,
    isDownloading: Boolean,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    viewModel: StreamingViewModel
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(if (isFocused) 1.05f else 1.00f, label = "videoScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scaleFactor)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusedBorder else Color.White.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable { onPlayClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                AsyncImage(
                    model = video.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Render play action overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Video Duration
                val minutes = video.durationSeconds / 60
                val seconds = video.durationSeconds % 60
                val durationText = String.format("%d:%02d", minutes, seconds)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .background(Color.Black.copy(alpha = 0.75f), shape = MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(durationText, fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    minLines = 2
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(video.category, fontSize = 9.sp, color = AccentGold, fontWeight = FontWeight.Bold)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Favorite Icon
                        val isFav = viewModel.isFavorite(video.id, "video")
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFav) PrimaryRed else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.toggleFavorite(video.id, "video") }
                        )

                        // Download Action
                        if (video.isDownloaded) {
                            Icon(
                                imageVector = Icons.Default.FileDownloadDone,
                                contentDescription = "Saved Offline",
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(16.dp)
                            )
                        } else if (isDownloading) {
                            CircularProgressIndicator(
                                color = AccentGold,
                                strokeWidth = 1.5.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Offline",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onDownloadClick() }
                            )
                        }
                    }
                }
            }
        }
    }
}
