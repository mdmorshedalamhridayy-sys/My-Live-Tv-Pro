package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChannelEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.FocusedBorder
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.viewmodel.StreamingViewModel

@Composable
fun TVHomeScreen(viewModel: StreamingViewModel) {
    val channels by viewModel.channels.collectAsState()
    
    // Standard grouping for our custom BD Categories
    val bangladeshTv = remember(channels) { channels.filter { it.category == "Bangladesh TV" } }
    val internationalSports = remember(channels) { channels.filter { it.category == "Live Sports" } }
    val newsChannels = remember(channels) { channels.filter { it.category == "Live News" } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Featured Cinematic Hero Banner with CTA Buttons
        item {
            HeroCarouselBanner(viewModel)
        }

        // 2. Bangladesh TV Live Rows
        if (bangladeshTv.isNotEmpty()) {
            item {
                ChannelCategoryRow(
                    categoryTitle = "🇧🇩 Bangladesh Live TV Channels",
                    channels = bangladeshTv,
                    onChannelClick = { viewModel.playChannel(it) },
                    viewModel = viewModel
                )
            }
        }

        // 3. International Live Sports Rows
        if (internationalSports.isNotEmpty()) {
            item {
                ChannelCategoryRow(
                    categoryTitle = "🏏 Featured International Sports",
                    channels = internationalSports,
                    onChannelClick = { viewModel.playChannel(it) },
                    viewModel = viewModel
                )
            }
        }

        // 4. Live News Rows
        if (newsChannels.isNotEmpty()) {
            item {
                ChannelCategoryRow(
                    categoryTitle = "📰 24/7 Live News Feeds",
                    channels = newsChannels,
                    onChannelClick = { viewModel.playChannel(it) },
                    viewModel = viewModel
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HeroCarouselBanner(viewModel: StreamingViewModel) {
    // Standard mock match details
    val mockChannel = ChannelEntity(
        id = 9999,
        name = "Bangladesh vs Sri Lanka Live T20 Rematch Series",
        streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        thumbnail = "https://images.unsplash.com/photo-1540747737956-3787217ab2ad?w=1200",
        category = "Live Sports"
    )

    var isFocused by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(if (isFocused) 1.01f else 1.00f, label = "bannerScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .scale(scaleFactor)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusedBorder else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = MaterialTheme.shapes.large
            )
            .clickable { viewModel.playChannel(mockChannel) },
        shape = MaterialTheme.shapes.large
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Unsplash dynamic Sports background
            AsyncImage(
                model = "https://images.unsplash.com/photo-1540747737956-3787217ab2ad?w=800",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Dark elegant overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
            )

            // Banner Info Detail
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
                    .fillMaxWidth(0.9f)
            ) {
                Box(
                    modifier = Modifier
                        .background(PrimaryRed, shape = MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("LIVE ACTION", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "BANGLADESH VS SRI LANKA CLASSIC ROUND-2",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Exclusive sports streaming feed. High speed low latency multi-device optimized stream directly from Sher-e-Bangla Stadium.",
                    fontSize = 12.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row {
                    Button(
                        onClick = { viewModel.playChannel(mockChannel) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("TUNE IN LIVE NOW", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { viewModel.toggleFavorite(mockChannel.id, "channel") },
                        shape = MaterialTheme.shapes.small,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        val isFav = viewModel.isFavorite(mockChannel.id, "channel")
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (isFav) PrimaryRed else Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("MY LIST", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelCategoryRow(
    categoryTitle: String,
    channels: List<ChannelEntity>,
    onChannelClick: (ChannelEntity) -> Unit,
    viewModel: StreamingViewModel
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = categoryTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.focusGroup()
        ) {
            items(channels) { ch ->
                TVChannelCard(
                    channel = ch,
                    onClick = { onChannelClick(ch) },
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun TVChannelCard(
    channel: ChannelEntity,
    onClick: () -> Unit,
    viewModel: StreamingViewModel
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(if (isFocused) 1.08f else 1.00f, label = "cardScale")

    Column(
        modifier = Modifier
            .width(160.dp)
            .scale(scaleFactor)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) FocusedBorder else Color.White.copy(alpha = 0.05f),
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .background(SurfaceCard)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            // Channel logo layout
            AsyncImage(
                model = channel.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Channel category label overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.75f), shape = MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(channel.category, fontSize = 8.sp, color = AccentGold, fontWeight = FontWeight.Bold)
            }

            // Stream play overlay on focusing
            if (isFocused) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Details
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = channel.name,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("HLS FEED", fontSize = 9.sp, color = Color.White.copy(alpha = 0.4f))
                
                Icon(
                    imageVector = if (viewModel.isFavorite(channel.id, "channel")) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    tint = if (viewModel.isFavorite(channel.id, "channel")) PrimaryRed else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .size(14.dp)
                        .clickable { viewModel.toggleFavorite(channel.id, "channel") }
                )
            }
        }
    }
}
