package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.PrimaryRed
import com.example.ui.viewmodel.StreamingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StreamingViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf("livetv") } // livetv, vod, favorites, profile, admin

    val isUserAdminOrEditor = currentUser?.role == "Admin" || currentUser?.role == "Editor"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryRed, shape = MaterialTheme.shapes.small)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("LIVE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MyLive TV",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    currentUser?.let { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Color.White.copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.extraSmall
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = user.role.uppercase(),
                                    fontSize = 10.sp,
                                    color = if (user.role == "Admin") AccentGold else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hi, ${user.name}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepBackground
                )
            )
        },
        bottomBar = {
            // Adaptive design: bottom bar only on smaller screens (or standard layout)
            // Keeping layout robust by showing classic bottom bar for portable navigation
            NavigationBar(
                containerColor = DeepBackground
            ) {
                NavigationBarItem(
                    selected = (selectedTab == "livetv"),
                    onClick = { selectedTab = "livetv" },
                    label = { Text("Live TV") },
                    icon = { Icon(Icons.Default.Tv, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRed,
                        selectedTextColor = PrimaryRed,
                        indicatorColor = AccentGold.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = (selectedTab == "vod"),
                    onClick = { selectedTab = "vod" },
                    label = { Text("VOD Store") },
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRed,
                        selectedTextColor = PrimaryRed,
                        indicatorColor = AccentGold.copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    selected = (selectedTab == "favorites"),
                    onClick = { selectedTab = "favorites" },
                    label = { Text("My List") },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRed,
                        selectedTextColor = PrimaryRed,
                        indicatorColor = AccentGold.copy(alpha = 0.15f)
                    )
                )

                if (isUserAdminOrEditor) {
                    NavigationBarItem(
                        selected = (selectedTab == "admin"),
                        onClick = { selectedTab = "admin" },
                        label = { Text("Admin Panel") },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryRed,
                            selectedTextColor = PrimaryRed,
                            indicatorColor = AccentGold.copy(alpha = 0.15f)
                        )
                    )
                }

                NavigationBarItem(
                    selected = (selectedTab == "profile"),
                    onClick = { selectedTab = "profile" },
                    label = { Text("Account") },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryRed,
                        selectedTextColor = PrimaryRed,
                        indicatorColor = AccentGold.copy(alpha = 0.15f)
                    )
                )
            }
        },
        containerColor = DeepBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DeepBackground)
        ) {
            when (selectedTab) {
                "livetv" -> TVHomeScreen(viewModel = viewModel)
                "vod" -> VODScreen(viewModel = viewModel)
                "favorites" -> WatchlistScreen(viewModel = viewModel)
                "admin" -> if (isUserAdminOrEditor) AdminPanelScreen() else TVHomeScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ProfileScreen(viewModel: StreamingViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = PrimaryRed
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = currentUser?.name ?: "Guest User",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGold
                )
                
                Text(
                    text = currentUser?.email ?: "guest@mylivetv.com",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Account Level:", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(currentUser?.role ?: "GUEST", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AccentGold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIGN OUT FROM MYLIVE TV")
                }
            }
        }
    }
}
