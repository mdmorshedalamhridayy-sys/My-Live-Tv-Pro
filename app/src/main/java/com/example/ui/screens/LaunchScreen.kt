package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentGold
import com.example.ui.theme.DeepBackground
import com.example.ui.theme.PrimaryRed
import com.example.ui.viewmodel.StreamingViewModel
import kotlinx.coroutines.delay

@Composable
fun LaunchScreen(viewModel: StreamingViewModel) {
    var startAnimation by remember { mutableStateOf(false) }
    
    val transition = updateTransition(targetState = startAnimation, label = "splashTransition")
    
    val logoScale by transition.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "logoScale"
    ) { state ->
        if (state) 1.1f else 0.5f
    }
    
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(delayMillis = 100, durationMillis = 1000) },
        label = "logoAlpha"
    ) { state ->
        if (state) 1.0f else 0.0f
    }

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2000)
        // Redirect
        if (viewModel.currentUser.value != null) {
            viewModel.navigateTo("home")
        } else {
            viewModel.navigateTo("auth")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PrimaryRed.copy(alpha = 0.25f),
                        DeepBackground
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // High-fidelity vector splash layout representing Television Player
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .background(PrimaryRed, shape = MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "MyLive TV",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGold,
                modifier = Modifier.alpha(logoAlpha)
            )
            
            Text(
                text = "Bangladesh & International Entertainment",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(logoAlpha)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            CircularProgressIndicator(
                color = AccentGold,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
