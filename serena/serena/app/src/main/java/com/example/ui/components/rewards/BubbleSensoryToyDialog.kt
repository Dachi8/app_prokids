package com.example.ui.components.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import kotlin.random.Random

data class BubbleItem(
    val id: Int,
    val xPercent: Float,
    val yPercent: Float,
    val sizeDp: Int,
    val color: Color
)

@Composable
fun BubbleSensoryToyDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var poppedCount by remember { mutableIntStateOf(0) }
    var nextId by remember { mutableIntStateOf(10) }

    val bubbles = remember {
        mutableStateListOf(
            BubbleItem(1, 0.2f, 0.25f, 65, Color(0xFF80D8FF)),
            BubbleItem(2, 0.65f, 0.32f, 80, Color(0xFFEA80FC)),
            BubbleItem(3, 0.35f, 0.50f, 70, Color(0xFFB9F6CA)),
            BubbleItem(4, 0.75f, 0.60f, 55, Color(0xFFFFD180)),
            BubbleItem(5, 0.15f, 0.70f, 75, Color(0xFFFF80AB)),
            BubbleItem(6, 0.50f, 0.15f, 60, Color(0xFF8C9EFF))
        )
    }

    fun spawnMoreBubbles() {
        for (i in 0..4) {
            bubbles.add(
                BubbleItem(
                    id = nextId++,
                    xPercent = Random.nextFloat().coerceIn(0.1f, 0.85f),
                    yPercent = Random.nextFloat().coerceIn(0.12f, 0.82f),
                    sizeDp = Random.nextInt(50, 85),
                    color = listOf(
                        Color(0xFF80D8FF),
                        Color(0xFFEA80FC),
                        Color(0xFFB9F6CA),
                        Color(0xFFFFD180),
                        Color(0xFFFF80AB),
                        Color(0xFF8C9EFF)
                    ).random()
                )
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF3F7FD)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🫧", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Burbujas Antiestrés de Calma",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Toca para inflar y reventar con paz",
                                fontSize = 12.sp,
                                color = Color(0xFF0091EA),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF4A3E62))
                    }
                }

                // Counter & Relaxation Hint
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Burbujas de paz reventadas: $poppedCount ✨",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0077C2)
                            )
                            Text(
                                text = "Imagina que cada pompa de jabón que explota se lleva una preocupación.",
                                fontSize = 12.sp,
                                color = Color(0xFF56476F)
                            )
                        }

                        ClayPuffyButton(
                            onClick = { spawnMoreBubbles() },
                            shape = RoundedCornerShape(12.dp),
                            containerColor = Color(0xFFE1F5FE),
                            contentColor = Color(0xFF0277BD)
                        ) {
                            Text("+ Soplar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Interactive Bubble Tank Field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .shadow(6.dp, RoundedCornerShape(26.dp), ambientColor = Color(0x2500BCD4))
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFE1F5FE), Color(0xFFFCE4EC))
                            )
                        )
                ) {
                    bubbles.forEach { bubble ->
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset {
                                    IntOffset(
                                        x = (bubble.xPercent * 600).toInt(),
                                        y = (bubble.yPercent * 800).toInt()
                                    )
                                }
                                .size(bubble.sizeDp.dp)
                                .shadow(8.dp, CircleShape, ambientColor = bubble.color)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.9f),
                                            bubble.color.copy(alpha = 0.6f),
                                            bubble.color.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    poppedCount++
                                    bubbles.remove(bubble)
                                    if (bubbles.size < 4) {
                                        spawnMoreBubbles()
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            // Shimmer highlight inside bubble
                            Box(
                                modifier = Modifier
                                    .size((bubble.sizeDp * 0.35f).dp)
                                    .offset(x = (-bubble.sizeDp * 0.2f).dp, y = (-bubble.sizeDp * 0.2f).dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.8f))
                            )
                        }
                    }

                    Text(
                        text = "¡Toca las burbujas para hacer ¡POP! 🫧",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0077C2),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 14.dp)
                    )
                }

                ClayPuffyButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Guardar Burbujero y Salir 🫧", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}
