package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChannelEntity
import com.example.data.NotificationEntity
import com.example.data.UserEntity
import com.example.data.VideoEntity
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.PrimaryRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.StreamingViewModel

@Composable
fun AdminPanelScreen(streamingViewModel: StreamingViewModel) {
    val adminViewModel: AdminViewModel = viewModel()
    var activeAdminTab by remember { mutableStateOf("dashboard") } // dashboard, channel_crud, video_crud, users, alerts, updates, gateways

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab Navigation Headers
        ScrollableTabRow(
            selectedTabIndex = when (activeAdminTab) {
                "dashboard" -> 0
                "channel_crud" -> 1
                "video_crud" -> 2
                "users" -> 3
                "alerts" -> 4
                "updates" -> 5
                "gateways" -> 6
                else -> 0
            },
            containerColor = DeepBackground,
            contentColor = PrimaryRed,
            edgePadding = 12.dp
        ) {
            Tab(
                selected = (activeAdminTab == "dashboard"),
                onClick = { activeAdminTab = "dashboard" },
                text = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "channel_crud"),
                onClick = { activeAdminTab = "channel_crud" },
                text = { Text("Live Channels", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "video_crud"),
                onClick = { activeAdminTab = "video_crud" },
                text = { Text("VOD Store", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "users"),
                onClick = { activeAdminTab = "users" },
                text = { Text("Manage Users", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "alerts"),
                onClick = { activeAdminTab = "alerts" },
                text = { Text("Push Alerts", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "updates"),
                onClick = { activeAdminTab = "updates" },
                text = { Text("Update System", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = (activeAdminTab == "gateways"),
                onClick = { activeAdminTab = "gateways" },
                text = { Text("Gateways", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AccentGold) }
            )
        }

        // Sub Screen Selection
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(DeepBackground)
        ) {
            when (activeAdminTab) {
                "dashboard" -> AdminDashboardTab(adminViewModel)
                "channel_crud" -> ChannelCrudTab(adminViewModel)
                "video_crud" -> VideoCrudTab(adminViewModel)
                "users" -> UsersManagementTab(adminViewModel)
                "alerts" -> PushAlertsTab(adminViewModel)
                "updates" -> UpdatesManagementTab(streamingViewModel)
                "gateways" -> GatewayManagementTab(streamingViewModel)
            }
        }
    }
}

@Composable
fun AdminDashboardTab(adminViewModel: AdminViewModel) {
    val totalViews by adminViewModel.totalViews.collectAsState()
    val activeUsers by adminViewModel.activeUsersCount.collectAsState()
    val mostWatchedType by adminViewModel.mostWatchedType.collectAsState()
    val chartData by adminViewModel.lastSevenDaysViews.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Web-Style Administrator Metrics", fontSize = 15.sp, color = AccentGold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        // Big Counter Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(
                title = "TOTAL STREAM VIEWS",
                value = totalViews.toString(),
                trend = "+12% peak today",
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryRed) }
            )
            AdminMetricCard(
                title = "ACTIVE USERS",
                value = activeUsers.toString(),
                trend = "Verified devices",
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Person, contentDescription = null, tint = AccentGold) }
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AdminMetricCard(
                title = "DOMINANT FORMAT",
                value = mostWatchedType,
                trend = "Direct live matches",
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Tv, contentDescription = null, tint = Color.Cyan) }
            )
            AdminMetricCard(
                title = "SYSTEM SYNC",
                value = "100%",
                trend = "Firestore & DB online",
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF00FFCC)) }
            )
        }

        // Custom Vector line chart representing viewing rates!
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Live Stream Views Projection (Days)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.LightGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Render vector line graph dynamically using Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .background(Color.Black.copy(alpha = 0.2f))
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Draw Y guides
                    val midY = height / 2
                    drawLine(color = Color.DarkGray, start = Offset(0f, midY), end = Offset(width, midY), strokeWidth = 1f)
                    drawLine(color = Color.DarkGray, start = Offset(0f, height), end = Offset(width, height), strokeWidth = 2f)

                    // Draw Line Path
                    val pointsCount = chartData.size
                    val segmentWidth = width / (pointsCount - 1)
                    val maxVal = chartData.maxOrNull() ?: 100
                    val path = Path()

                    for (i in 0 until pointsCount) {
                        val currentVal = chartData[i]
                        val x = i * segmentWidth
                        // Inverse scale
                        val y = height - (currentVal.toFloat() / maxVal * (height - 30f)) - 10f
                        
                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }

                        // Draw points dots
                        drawCircle(
                            color = AccentGold,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    drawPath(
                        path = path,
                        color = PrimaryRed,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Today").forEach { d ->
                        Text(d, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    trend: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                icon()
            }
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 6.dp))
            Text(trend, fontSize = 9.sp, color = Color(0xFF00FFCC), modifier = Modifier.padding(top = 4.dp))
        }
    }
}

// Sub Screen Tab: Channel CRUD
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCrudTab(adminViewModel: AdminViewModel) {
    val channels by adminViewModel.channels.collectAsState()
    
    // Forms state
    val formName by adminViewModel.formChannelName.collectAsState()
    val formUrl by adminViewModel.formChannelUrl.collectAsState()
    val formThumbnail by adminViewModel.formChannelThumbnail.collectAsState()
    val formCategory by adminViewModel.formChannelCategory.collectAsState()
    val editingId by adminViewModel.editingChannelId.collectAsState()

    val categories = listOf("Bangladesh TV", "Live Sports", "Live News", "International TV")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (editingId == null) "➕ ADD NEW STREAMS CHANNEL" else "✏️ EDIT ACTIVE CHANNEL",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = formName,
                    onValueChange = { adminViewModel.formChannelName.value = it },
                    label = { Text("Channel Full Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formUrl,
                    onValueChange = { adminViewModel.formChannelUrl.value = it },
                    label = { Text("HLS Stream (.m3u8) / MP4 Stream URL") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formThumbnail,
                    onValueChange = { adminViewModel.formChannelThumbnail.value = it },
                    label = { Text("Display Image Thumbnail URL") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                // Category selection dropdown
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("Select Streams Channel Category:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (formCategory == cat) PrimaryRed else DeepBackground,
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .clickable { adminViewModel.formChannelCategory.value = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { adminViewModel.submitChannelForm() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (editingId == null) "SAVE CHANNEL" else "APPLY CHANGES")
                    }

                    if (editingId != null) {
                        Button(
                            onClick = { adminViewModel.clearChannelForm() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("CANCEL")
                        }
                    }
                }
            }
        }

        // Active Channels CRUD listing table
        Text("Database Active Channels List (${channels.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold, modifier = Modifier.padding(bottom = 10.dp))

        channels.forEach { ch ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ch.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text(ch.streamUrl, fontSize = 9.sp, color = Color.Gray, maxLines = 1)
                    }

                    Row {
                        IconButton(onClick = { adminViewModel.loadChannelToForm(ch) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGold, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { adminViewModel.deleteChannel(ch) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Sub Screen Tab: Video VOD CRUD
@Composable
fun VideoCrudTab(adminViewModel: AdminViewModel) {
    val videos by adminViewModel.videos.collectAsState()

    val formTitle by adminViewModel.formVideoTitle.collectAsState()
    val formUrl by adminViewModel.formVideoUrl.collectAsState()
    val formThumbnail by adminViewModel.formVideoThumbnail.collectAsState()
    val formCategory by adminViewModel.formVideoCategory.collectAsState()
    val formDuration by adminViewModel.formVideoDuration.collectAsState()
    val editingId by adminViewModel.editingVideoId.collectAsState()

    val categories = listOf("Match Highlights", "Cricket Specials", "Talk Shows", "Documentaries")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (editingId == null) "➕ ADD NEW VOD CONTENT" else "✏️ EDIT ACTIVE VOD CONTENT",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = formTitle,
                    onValueChange = { adminViewModel.formVideoTitle.value = it },
                    label = { Text("Video Title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formUrl,
                    onValueChange = { adminViewModel.formVideoUrl.value = it },
                    label = { Text("Video Source URL (MP4 etc.)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = formThumbnail,
                    onValueChange = { adminViewModel.formVideoThumbnail.value = it },
                    label = { Text("Thumbnail Image URL") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = formDuration,
                        onValueChange = { adminViewModel.formVideoDuration.value = it },
                        label = { Text("Duration (Seconds)") },
                        modifier = Modifier.weight(1f).padding(bottom = 12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // VOD Category Select
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text("Select Video Category:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (formCategory == cat) PrimaryRed else DeepBackground,
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .clickable { adminViewModel.formVideoCategory.value = cat }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(cat, fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { adminViewModel.submitVideoForm() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (editingId == null) "SAVE VIDEO" else "APPLY CHANGES")
                    }

                    if (editingId != null) {
                        Button(
                            onClick = { adminViewModel.clearVideoForm() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("CANCEL")
                        }
                    }
                }
            }
        }

        // Active VOD Lists
        Text("Database Active VOD Videos (${videos.size})", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold, modifier = Modifier.padding(bottom = 10.dp))

        videos.forEach { video ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(video.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        Text("${video.category} • ${video.durationSeconds / 60} min", fontSize = 9.sp, color = Color.Gray)
                    }

                    Row {
                        IconButton(onClick = { adminViewModel.loadVideoToForm(video) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGold, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { adminViewModel.deleteVideo(video) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PrimaryRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Sub Screen Tab: Users management
@Composable
fun UsersManagementTab(adminViewModel: AdminViewModel) {
    val users by adminViewModel.users.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Manage User Registrations & Accounts", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold, modifier = Modifier.padding(bottom = 16.dp))

        users.forEach { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text(user.email, fontSize = 11.sp, color = Color.Gray)
                        }

                        // Role Tag
                        Box(
                            modifier = Modifier
                                .background(
                                    if (user.role == "Admin") PrimaryRed else Color.DarkGray,
                                    shape = MaterialTheme.shapes.extraSmall
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(user.role.uppercase(), fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Promoting roles dropdown simulated
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { adminViewModel.changeUserRole(user.id, "Admin") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Set Admin", fontSize = 9.sp, color = AccentGold)
                            }
                            Button(
                                onClick = { adminViewModel.changeUserRole(user.id, "Editor") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text("Set Editor", fontSize = 9.sp, color = Color.White)
                            }
                        }

                        // Block/Banish user button
                        Button(
                            onClick = { adminViewModel.setBanUser(user.id, !user.isBanned) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isBanned) Color(0xFF00FFCC) else PrimaryRed
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Text(if (user.isBanned) "UNBAN USER" else "BAN ACCOUNT", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Sub Screen Tab: Push broadcaster alerts
@Composable
fun PushAlertsTab(adminViewModel: AdminViewModel) {
    val notifTitle by adminViewModel.formNotifTitle.collectAsState()
    val notifMessage by adminViewModel.formNotifMessage.collectAsState()
    val isSending by adminViewModel.isSendingNotif.collectAsState()
    val hasNotifs by adminViewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("FCM Broadcast Push Notification Dispatcher", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold, modifier = Modifier.padding(bottom = 12.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = notifTitle,
                    onValueChange = { adminViewModel.formNotifTitle.value = it },
                    label = { Text("Push Alert Notification Title") },
                    placeholder = { Text("e.g. Bangladesh vs Lanka Match Streaming Now!") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notifMessage,
                    onValueChange = { adminViewModel.formNotifMessage.value = it },
                    label = { Text("Push Alert Short Message") },
                    placeholder = { Text("Tap now to tune into live Bangladesh TV commentary...") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                Button(
                    onClick = { adminViewModel.submitPushNotification() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isSending
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DISPATCH CHANNELS BROADCAST NOW")
                    }
                }
            }
        }

        // Broadcaster log alerts
        Text("Historic Notification Alerts Sent", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold, modifier = Modifier.padding(bottom = 10.dp))

        if (hasNotifs.isEmpty()) {
            Text("No push notification events found.", fontSize = 11.sp, color = Color.Gray)
        } else {
            hasNotifs.forEach { notif ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            Text(notif.message, fontSize = 10.sp, color = Color.LightGray)
                        }

                        // Broadcast Status Badge
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF00FFCC).copy(alpha = 0.15f), shape = MaterialTheme.shapes.extraSmall)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("PUSH SENT", color = Color(0xFF00FFCC), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpdatesManagementTab(streamingViewModel: StreamingViewModel) {
    val currentLocalVer = streamingViewModel.currentAppVersion
    val latestVer by streamingViewModel.latestAppVersion.collectAsState()
    val updateMsg by streamingViewModel.updateMessage.collectAsState()
    val updateUrl by streamingViewModel.updateUrl.collectAsState()
    val isMandatory by streamingViewModel.isUpdateMandatory.collectAsState()

    var inputVersion by remember { mutableStateOf(latestVer) }
    var inputMessage by remember { mutableStateOf(updateMsg) }
    var inputUrl by remember { mutableStateOf(updateUrl) }
    var setMandatory by remember { mutableStateOf(isMandatory) }

    var successStatusMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Enterprise App Update & Version Controller",
            fontSize = 15.sp,
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Publish system update details instantly to all user devices over the air via Firestore sync.",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("CURRENT APP ENGINE STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryRed)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Local Installed Engine", fontSize = 10.sp, color = Color.Gray)
                        Text("v$currentLocalVer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Published Live version", fontSize = 10.sp, color = Color.Gray)
                        Text("v$latestVer", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                    }
                    Column {
                        Text("Type", fontSize = 10.sp, color = Color.Gray)
                        Text(
                            text = if (isMandatory) "FORCE / MANDATORY" else "RECOMMENDED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMandatory) PrimaryRed else Color(0xFF00FFCC)
                        )
                    }
                }
            }
        }

        // New Update Settings Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("CONSTRUCT LIVE SYSTEM UPDATE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = inputVersion,
                    onValueChange = { inputVersion = it },
                    label = { Text("App version (e.g. 1.1.0)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    label = { Text("Update logs / description message") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { inputUrl = it },
                    label = { Text("Update Download URL") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setMandatory,
                        onCheckedChange = { setMandatory = it }
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text("Force/Mandatory Update", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("User application will block accesses until updated.", fontSize = 9.sp, color = Color.Gray)
                    }
                }

                if (successStatusMsg != null) {
                    Text(
                        text = successStatusMsg ?: "",
                        color = Color(0xFF00FFCC),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Button(
                    onClick = {
                        if (inputVersion.isNotEmpty()) {
                            streamingViewModel.publishAppUpdate(inputVersion, inputMessage, inputUrl, setMandatory)
                            successStatusMsg = "Successfully synchronized update package (v${inputVersion}) to Firebase!"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PUBLISH OTA SYSTEM UPDATE", color = Color.White)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GatewayManagementTab(viewModel: StreamingViewModel) {
    val gatewayNumbers by viewModel.gatewayNumbers.collectAsState()
    
    var providerInput by remember { mutableStateOf("bKash") } // bKash, Nagad, Rocket
    var numberInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("Personal") } // Personal, Agent, Merchant
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Title
        Text(
            text = "PAYMENT GATEWAY NUMBERS",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Add New Gateway Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Add New Payment Channel Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Provider selection row
                Text("Select Provider:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("bKash", "Nagad", "Rocket").forEach { provider ->
                        val isSelected = providerInput == provider
                        val providerColor = when (provider) {
                            "bKash" -> Color(0xFFE2136E)
                            "Nagad" -> Color(0xFFF37021)
                            "Rocket" -> Color(0xFF8C2D8C)
                            else -> PrimaryRed
                        }
                        Button(
                            onClick = { providerInput = provider },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) providerColor else Color.DarkGray
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(provider, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Number entry
                OutlinedTextField(
                    value = numberInput,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            numberInput = input
                        }
                    },
                    label = { Text("Gateway Number", color = Color.Gray, fontSize = 12.sp) },
                    placeholder = { Text("e.g. 01712345678", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Account Type selection row
                Text("Account Type:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Personal", "Agent", "Merchant").forEach { type ->
                        val isSelected = typeInput == type
                        Button(
                            onClick = { typeInput = type },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentGold else Color.DarkGray,
                                contentColor = if (isSelected) Color.Black else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(type, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                if (successMsg != null) {
                    Text(successMsg ?: "", color = Color.Green, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                Button(
                    onClick = {
                        errorMsg = null
                        successMsg = null
                        if (numberInput.length < 11) {
                            errorMsg = "Gateway number must be at least 11 digits!"
                        } else {
                            viewModel.addGatewayNumberByAdmin(providerInput, numberInput, typeInput)
                            successMsg = "Successfully added $providerInput gateway number!"
                            numberInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ADD GATEWAY CHANNEL", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Existing Gateways Header
        Text(
            text = "Active Gateway Channels (${gatewayNumbers.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (gatewayNumbers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No payment gateway numbers added yet.", color = Color.Gray, fontSize = 12.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                gatewayNumbers.forEach { gateway ->
                    val colorAccent = when (gateway.provider) {
                        "bKash" -> Color(0xFFE2136E)
                        "Nagad" -> Color(0xFFF37021)
                        "Rocket" -> Color(0xFF8C2D8C)
                        else -> AccentGold
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        border = borderStroke(1.dp, colorAccent.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(colorAccent, shape = MaterialTheme.shapes.small)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = gateway.provider.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column {
                                    Text(
                                        text = gateway.number,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = gateway.type,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            IconButton(
                                onClick = { viewModel.removeGatewayNumberByAdmin(gateway.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove Gateway",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper function to create borders dynamically 
@Composable
fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = androidx.compose.foundation.BorderStroke(width, color)
