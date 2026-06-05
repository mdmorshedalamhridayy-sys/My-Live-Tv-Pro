package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.List
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
    
    var selectedLayoutMode by remember { mutableStateOf("grid") } // defaults to "grid" so the grid is displayed front and center!
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    // Filtered channels
    val filteredChannels = remember(channels, searchQuery, selectedCategoryFilter) {
        channels.filter { channel ->
            val matchQuery = channel.name.contains(searchQuery, ignoreCase = true) || 
                             channel.category.contains(searchQuery, ignoreCase = true)
            val matchCategory = selectedCategoryFilter == "All" || channel.category == selectedCategoryFilter
            matchQuery && matchCategory
        }
    }

    val bangladeshTv = remember(channels) { channels.filter { it.category == "Bangladesh TV" } }
    val internationalSports = remember(channels) { channels.filter { it.category == "Live Sports" } }
    val newsChannels = remember(channels) { channels.filter { it.category == "Live News" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // --- 1. PREMIUM HEADER SECTION ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Live Stream Hub",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Seamless low-latency stream networks",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Segmented Layout Selector
            Row(
                modifier = Modifier
                    .background(SurfaceCard, shape = MaterialTheme.shapes.medium)
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { selectedLayoutMode = "rows" },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (selectedLayoutMode == "rows") PrimaryRed else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Standard Rows View",
                        tint = if (selectedLayoutMode == "rows") Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = { selectedLayoutMode = "grid" },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (selectedLayoutMode == "grid") PrimaryRed else Color.Transparent,
                            shape = MaterialTheme.shapes.small
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.GridView,
                        contentDescription = "Live Channel Grid View",
                        tint = if (selectedLayoutMode == "grid") Color.White else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 2. INTEGRATED LIVE SEARCH BAR ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search 100+ Live Channels, leagues, news...", fontSize = 12.sp, color = Color.Gray) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Text("×", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = PrimaryRed,
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // --- 3. CATEGORY PILL FILTER OVERLAY (Shown for Grid) ---
        if (selectedLayoutMode == "grid") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Bangladesh TV", "Live Sports", "Live News").forEach { filter ->
                    val isSelected = selectedCategoryFilter == filter
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) PrimaryRed else SurfaceCard,
                                shape = MaterialTheme.shapes.medium
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PrimaryRed else Color.White.copy(alpha = 0.08f),
                                shape = MaterialTheme.shapes.medium
                            )
                            .clickable { selectedCategoryFilter = filter }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when (filter) {
                                "Bangladesh TV" -> "🇧🇩 Bangladesh TV"
                                "Live Sports" -> "🏏 Live Sports"
                                "Live News" -> "📰 Live News"
                                else -> "📺 All Streams"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color.LightGray
                        )
                    }
                }
            }
        }

        // --- 4. CONDITIONAL RENDER BY SELECTED LAYOUT MODE ---
        if (selectedLayoutMode == "rows") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    HeroCarouselBanner(viewModel)
                }

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
        } else {
            if (filteredChannels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📺", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Live Stream Matches Found",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Try searching for alternative live programs or clear filters.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 165.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredChannels) { channel ->
                        LiveGridChannelCard(
                            channel = channel,
                            onClick = { viewModel.playChannel(channel) },
                            viewModel = viewModel
                        )
                    }
                }
            }
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

// --- NEW COMPONENT FOR LIVE CONTROLLER ---

data class LiveProgramDetail(
    val title: String,
    val timeRemainingText: String,
    val progress: Float
)

fun getLiveProgramDetail(channelName: String, channelCategory: String): LiveProgramDetail {
    return when (channelCategory) {
        "Live Sports" -> {
            val title = when {
                channelName.contains("Cricket", ignoreCase = true) -> "LIVE Cricket: BAN vs SL Live T20"
                channelName.contains("Football", ignoreCase = true) || channelName.contains("La Liga", ignoreCase = true) -> "LIVE: Spanish La Liga Matchday"
                else -> "Live Sports Broadcast Special"
            }
            LiveProgramDetail(title, "42 min left", 0.65f)
        }
        "Live News" -> {
            val title = when {
                channelName.contains("Somoy", ignoreCase = true) -> "News Bulletin: Somoy Bises Protibedon"
                channelName.contains("Jamuna", ignoreCase = true) -> "Prime Time: Jamuna Sangbad Kotha"
                channelName.contains("Independent", ignoreCase = true) -> "Independent Talk: Bangladesh Agenda"
                else -> "Prime Live Evening News Coverage"
            }
            LiveProgramDetail(title, "18 min left", 0.35f)
        }
        else -> { // Bangladesh TV / General
            val title = when {
                channelName.contains("BTV", ignoreCase = true) -> "BTV Weekly Drama Special"
                channelName.contains("Channel i", ignoreCase = true) -> "Channel i Gaaner Uthesh"
                channelName.contains("RTV", ignoreCase = true) -> "RTV Mega serial: Songsar Kotha"
                else -> "Golden Era Family Drama Hour"
            }
            LiveProgramDetail(title, "25 min left", 0.50f)
        }
    }
}

@Composable
fun LiveGridChannelCard(
    channel: ChannelEntity,
    onClick: () -> Unit,
    viewModel: StreamingViewModel
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleFactor by animateFloatAsState(if (isFocused) 1.05f else 1.00f, label = "cardScale")

    // Pulsing live badge animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    // Get mock program and progress remaining
    val programDetail = remember(channel.name, channel.category) {
        getLiveProgramDetail(channel.name, channel.category)
    }

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
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            ) {
                // Channel Thumbnail Picture
                AsyncImage(
                    model = channel.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark visual lower gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Blinking/Pulsing Live action badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(PrimaryRed.copy(alpha = pulseAlpha), shape = MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "LIVE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Category overlay text in corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.7f), shape = MaterialTheme.shapes.extraSmall)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = when(channel.category) {
                            "Bangladesh TV" -> "🇧🇩 BD TV"
                            "Live Sports" -> "🏏 Sports"
                            "Live News" -> "📰 News"
                            else -> channel.category
                        },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold
                    )
                }

                // Play overlay icon when focused
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.40f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Tune in live stream",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // Channel metadata details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                // 1. Channel Title name
                Text(
                    text = channel.name,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 2. NOW PLAYING Title and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = programDetail.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color(0xFF00FFCC), // Live teal soft color indicator
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(0.7f)
                    )
                    Text(
                        text = programDetail.timeRemainingText,
                        fontSize = 8.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(0.3f),
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Mini Progress Bar representing elapsed duration of the currently playing show
                LinearProgressIndicator(
                    progress = { programDetail.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = PrimaryRed,
                    trackColor = Color.White.copy(alpha = 0.12f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Playback speed and heart option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HLS BROADCAST",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f)
                    )

                    Icon(
                        imageVector = if (viewModel.isFavorite(channel.id, "channel")) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Sync Favorite",
                        tint = if (viewModel.isFavorite(channel.id, "channel")) PrimaryRed else Color.White.copy(alpha = 0.40f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { viewModel.toggleFavorite(channel.id, "channel") }
                    )
                }
            }
        }
    }
}
