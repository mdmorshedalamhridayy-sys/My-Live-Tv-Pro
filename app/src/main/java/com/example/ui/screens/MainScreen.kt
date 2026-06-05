package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
                "admin" -> if (isUserAdminOrEditor) AdminPanelScreen(viewModel) else TVHomeScreen(viewModel = viewModel)
                "profile" -> ProfileScreen(viewModel = viewModel)
            }
        }
    }

    // App Update Overlays checking
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()
    val latestVersion by viewModel.latestAppVersion.collectAsState()
    val updateMessage by viewModel.updateMessage.collectAsState()
    val isMandatory by viewModel.isUpdateMandatory.collectAsState()

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isMandatory) viewModel.dismissUpdateDialog()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = PrimaryRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (isMandatory) "CRITICAL SYSTEM UPDATE" else "NEW UPDATE AVAILABLE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Version v$latestVersion is available (Installed: v${viewModel.currentAppVersion}).",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = updateMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                    if (isMandatory) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "* This update is mandatory to continue streaming high-definition channels.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // In simulated web sandbox, let user skip or show confirmation indicating successful download/upgrade simulation
                        viewModel.dismissUpdateDialog()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed)
                ) {
                    Text("UPDATE NOW", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                if (!isMandatory) {
                    TextButton(
                        onClick = { viewModel.dismissUpdateDialog() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Text("SKIP FOR NOW")
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: StreamingViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val gatewayNumbers by viewModel.gatewayNumbers.collectAsState()

    var selectedPlanForCheckout by remember { mutableStateOf<SubscriptionPlan?>(null) }
    var checkoutProvider by remember { mutableStateOf("bKash") } // bKash, Nagad, Rocket
    var paymentSenderNumber by remember { mutableStateOf("") }
    var paymentTrxId by remember { mutableStateOf("") }
    
    var isVerifyingPayment by remember { mutableStateOf(false) }
    var paymentSuccessStatus by remember { mutableStateOf<String?>(null) }
    var checkoutErrorMsg by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()) }

    val subscriptionPlans = listOf(
        SubscriptionPlan(name = "Free Trail Plan", price = 0, validityDays = 30, description = "Enjoy 30 days of high definition news streams without any costs.", badge = "TRIAL"),
        SubscriptionPlan(name = "Standard Streamer", price = 200, validityDays = 60, description = "Unlock premium cricket, sports and worldwide news networks in 1080p.", badge = "HOT"),
        SubscriptionPlan(name = "VIP Unlimited", price = 700, validityDays = -1, description = "Unrestricted forever access. Zero recurring payments or interruptions.", badge = "LIFETIME"),
        SubscriptionPlan(name = "Premium Sports Pass", price = 1000, validityDays = 365, description = "Exclusive 365 days pass for elite global channels with instant updates.", badge = "PREMIUM")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Core User Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = PrimaryRed
                    )
                    Box(
                        modifier = Modifier
                            .background(AccentGold, shape = MaterialTheme.shapes.extraSmall)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = currentUser?.role?.uppercase() ?: "GUEST",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentUser?.name ?: "Guest Streamer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = currentUser?.email ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))

                // Subscription Info
                val plan = currentUser?.subscriptionPlan ?: "None"
                val status = currentUser?.subscriptionStatus ?: "Inactive"
                val expiry = currentUser?.subscriptionExpiry ?: 0L

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Stream Plan", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = plan,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (status == "Active") Color(0xFF00FFCC) else Color.Red,
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = status.uppercase(),
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                if (expiry > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expiration Limit:", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = if (expiry == Long.MAX_VALUE) "Permanent / Unlimited" else dateFormat.format(java.util.Date(expiry)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                ) {
                    Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SECURE SIGN OUT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Subscriptions Section
        if (selectedPlanForCheckout == null) {
            Text(
                text = "UPGRADE YOUR EXPERIENCE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Start
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                subscriptionPlans.forEach { subPlan ->
                    val isCurrent = currentUser?.subscriptionPlan == subPlan.name
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!isCurrent) {
                                    selectedPlanForCheckout = subPlan
                                    checkoutProvider = "bKash"
                                    paymentSenderNumber = ""
                                    paymentTrxId = ""
                                    paymentSuccessStatus = null
                                    checkoutErrorMsg = null
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isCurrent) AccentGold else Color.DarkGray.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = subPlan.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) AccentGold else Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isCurrent) AccentGold else PrimaryRed,
                                                shape = MaterialTheme.shapes.extraSmall
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isCurrent) "ACTIVE" else subPlan.badge,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.Black
                                        )
                                    }
                                }

                                Text(
                                    text = if (subPlan.price == 0) "FREE" else "${subPlan.price} Tk",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isCurrent) AccentGold else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Validity: ${if (subPlan.validityDays == -1) "Lifetime / Unlimited" else "${subPlan.validityDays} Days"}",
                                fontSize = 12.sp,
                                color = AccentGold.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = subPlan.description,
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Checkout Screen Gateway Panel
            val planToBuy = selectedPlanForCheckout ?: return
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MyLive Secure Checkout",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        IconButton(onClick = { selectedPlanForCheckout = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                    // Plan confirmation block
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(planToBuy.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Validity: ${if (planToBuy.validityDays == -1) "Forever" else "${planToBuy.validityDays} Days"}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(
                            text = "${planToBuy.price} Tk",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = AccentGold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (paymentSuccessStatus != null) {
                        // Success block
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00FFCC),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Payment Verified Successfully!",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 16.sp
                            )
                            Text(
                                text = paymentSuccessStatus ?: "",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { selectedPlanForCheckout = null },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black)
                            ) {
                                Text("GO TO HOME STREAM", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Interactive payment flows
                        Text("Select Mobile Wallet Provider:", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("bKash", "Nagad", "Rocket").forEach { provider ->
                                val isSelected = checkoutProvider == provider
                                val bColor = when (provider) {
                                    "bKash" -> Color(0xFFE2136E)
                                    "Nagad" -> Color(0xFFF37021)
                                    "Rocket" -> Color(0xFF8C2D8C)
                                    else -> PrimaryRed
                                }
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { checkoutProvider = provider }
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) bColor else Color.Transparent,
                                            shape = MaterialTheme.shapes.small
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) bColor.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = provider,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) bColor else Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Retrieve dynamic active admin Gateway Number
                        val dynamicGatewayObj = gatewayNumbers.firstOrNull { it.provider.equals(checkoutProvider, ignoreCase = true) }
                        val gatewayNumber = dynamicGatewayObj?.number ?: when (checkoutProvider) {
                            "bKash" -> "01789456123"
                            "Nagad" -> "01876543210"
                            "Rocket" -> "01912345678"
                            else -> "01700000000"
                        }
                        val gatewayType = dynamicGatewayObj?.type ?: "Personal"

                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Instructions:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentGold
                                )
                                Text(
                                    text = "Send money / Transfer exactly ${planToBuy.price} Tk to the following official $checkoutProvider account.",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    lineHeight = 15.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$checkoutProvider (${gatewayType}):",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = gatewayNumber,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sender phone entry
                        OutlinedTextField(
                            value = paymentSenderNumber,
                            onValueChange = { inp -> if (inp.all { it.isDigit() }) paymentSenderNumber = inp },
                            label = { Text("Sender Mobile Wallet Number", fontSize = 11.sp, color = Color.Gray) },
                            placeholder = { Text("e.g. 01XXXXXXXXX", color = Color.White.copy(alpha = 0.2f)) },
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Transaction ID entry
                        OutlinedTextField(
                            value = paymentTrxId,
                            onValueChange = { paymentTrxId = it.trim() },
                            label = { Text("Payment Transaction ID (TrxID)", fontSize = 11.sp, color = Color.Gray) },
                            placeholder = { Text("e.g. TRX983KD9W", color = Color.White.copy(alpha = 0.2f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = AccentGold,
                                unfocusedBorderColor = Color.Gray
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (checkoutErrorMsg != null) {
                            Text(checkoutErrorMsg ?: "", color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }

                        // Checkout Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { selectedPlanForCheckout = null },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("CANCEL")
                            }

                            Button(
                                onClick = {
                                    checkoutErrorMsg = null
                                    if (planToBuy.price > 0 && paymentSenderNumber.length < 11) {
                                        checkoutErrorMsg = "Please input a valid sender account phone number."
                                    } else if (planToBuy.price > 0 && paymentTrxId.length < 5) {
                                        checkoutErrorMsg = "Please input a valid transaction ID from your SMS receipt."
                                    } else {
                                        // Process simulation beautifully with instant ripple activation
                                        isVerifyingPayment = true
                                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                            isVerifyingPayment = false
                                            viewModel.purchaseSubscription(
                                                planToBuy.name,
                                                planToBuy.validityDays,
                                                planToBuy.price,
                                                checkoutProvider,
                                                paymentSenderNumber,
                                                paymentTrxId
                                            )
                                            paymentSuccessStatus = "Subscription for ${planToBuy.name} activated successfully using $checkoutProvider. Thank you!"
                                        }, 2000)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGold, contentColor = Color.Black),
                                modifier = Modifier.weight(1.5f),
                                enabled = !isVerifyingPayment
                            ) {
                                if (isVerifyingPayment) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                                } else {
                                    Text("VERIFY PAYMENT", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SubscriptionPlan(
    val name: String,
    val price: Int,
    val validityDays: Int,
    val description: String,
    val badge: String
)
