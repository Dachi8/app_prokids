package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Beating heart pulse animation
    val pulseAnim = rememberInfiniteTransition(label = "heartbeat")
    val scale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val haloScale by pulseAnim.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    // Automatically transition to login after heartbeat animation plays
    LaunchedEffect(Unit) {
        delay(2000)
        onAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6749E8),
                        Color(0xFF5A3BD9),
                        Color(0xFF482CB8)
                    )
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Beating Heart Container with 3D Clay Halo Glow
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Outer diffuse soft glow halo
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .scale(haloScale)
                        .clip(CircleShape)
                        .background(Color(0x30FFFFFF))
                )

                // Middle clay disc
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(scale * 0.95f)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = Color(0x60000000),
                            spotColor = Color(0x60000000)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFF7E9B),
                                    Color(0xFFF34D77),
                                    Color(0xFFD62B57)
                                )
                            )
                        )
                )

                // Inner beating heart icon
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Corazón latiendo",
                    tint = Color.White,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(scale)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Text "calma" requested by user
            Text(
                text = "Calma",
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Respira hondo... estás a salvo",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFDDD2FF),
                textAlign = TextAlign.Center
            )
        }
    }
}
