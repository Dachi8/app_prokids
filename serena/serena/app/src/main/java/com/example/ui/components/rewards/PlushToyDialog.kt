package com.example.ui.components.rewards

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor

@Composable
fun PlushToyDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var selectedAccessory by remember { mutableStateOf<String?>(null) }
    var currentSpeech by remember {
        mutableStateOf("¡Hola amiguito/a! Soy tu Peluche Serena. ¡Abrázame o juguemos juntos!")
    }
    var reactionEmoji by remember { mutableStateOf("💖") }
    var showHearts by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "plush_breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF9F5FE)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Soft Glow
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .align(Alignment.Center)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x35E0B0FF), Color.Transparent)
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                            Text(text = "🧸", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Peluche Interactivo Serena",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Tu compañero de abrazos y calma",
                                    fontSize = 12.sp,
                                    color = ClayDeepPurple,
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

                    // Speech Bubble
                    ClayCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = reactionEmoji, fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = currentSpeech,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF281C3F),
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Interactive Animated Plush Figure
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .scale(breathScale)
                            .shadow(12.dp, CircleShape, ambientColor = Color(0x356B4FA0))
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFEDE3FA), Color(0xFFF9EAF3))
                                )
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                showHearts = !showHearts
                                reactionEmoji = "🥰"
                                currentSpeech = listOf(
                                    "¡Ronrrón! Qué rico abrazo, me haces muy feliz.",
                                    "¡Eres mi persona favorita! ¡Todo va a estar bien!",
                                    "¡Cosquillas! Jijiji, respiremos juntos: 1... 2... 3...",
                                    "¡Qué suavito! Recuerda que eres un niño/a increíblemente fuerte."
                                ).random()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (selectedAccessory != null) {
                                Text(text = selectedAccessory!!, fontSize = 34.sp)
                            }
                            Text(
                                text = "🧸",
                                fontSize = 110.sp
                            )
                            Text(
                                text = "Tócame para acariciarme 💖",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple
                            )
                        }

                        // Floating Hearts Overlay
                        if (showHearts) {
                            Text(
                                text = "💖✨✨",
                                fontSize = 24.sp,
                                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                            )
                        }
                    }

                    // Interactive Action Controls for Kids
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Acciones interactivas:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333),
                            modifier = Modifier.padding(start = 4.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Button: Cosquillas
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        reactionEmoji = "😄"
                                        currentSpeech = "¡Gigi! Me dan cosquillitas en la pancita. ¡Gracias por hacerme sonreír!"
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFEDE3FA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFFE05A87), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Cosquillas", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF281C3F))
                                }
                            }

                            // Button: Dar Fruta
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        reactionEmoji = "🍎"
                                        currentSpeech = "¡Ñam ñam! ¡Qué manzana tan deliciosa y fresca! Comer fruta nos da superpoderes."
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFEDE3FA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Restaurant, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Alimentar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF281C3F))
                                }
                            }

                            // Button: Canción de Cuna
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        reactionEmoji = "😴"
                                        currentSpeech = "La la la... Cierra los ojitos, respira profundo y siente la paz en tu corazón."
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFEDE3FA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = ClayDeepPurple, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Canción", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF281C3F))
                                }
                            }

                            // Button: Mensaje de Valor
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        reactionEmoji = "⭐"
                                        currentSpeech = "¡Estoy muy orgulloso de ti! Cada tarea y rutina que completas es un gran paso."
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.5.dp, Color(0xFFEDE3FA))
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text("Motivar", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF281C3F))
                                }
                            }
                        }

                        // Accessories Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Accesorios para vestir:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E4F77)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    Pair("👑", "Corona"),
                                    Pair("🥳", "Fiesta"),
                                    Pair("🌸", "Flor"),
                                    Pair("🕶️", "Gafas"),
                                    Pair(null, "Quitar")
                                ).forEach { (acc, _) ->
                                    val isSelected = selectedAccessory == acc
                                    Surface(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                selectedAccessory = acc
                                                reactionEmoji = if (acc != null) "✨" else "😊"
                                            },
                                        shape = CircleShape,
                                        color = if (isSelected) ClayDeepPurple else Color.White,
                                        border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color(0xFFDDD2ED))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = acc ?: "✖",
                                                fontSize = if (acc != null) 16.sp else 12.sp,
                                                color = if (isSelected) Color.White else Color(0xFF5E4F77)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Finish Button
                    ClayPuffyButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("¡Gracias por jugar conmigo! 💖", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
