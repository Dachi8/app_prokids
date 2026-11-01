package com.example.ui.components.rewards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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

data class XylophoneBar(
    val note: String,
    val syllable: String,
    val color: Color,
    val widthFraction: Float
)

@Composable
fun XylophoneToyDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val bars = remember {
        listOf(
            XylophoneBar("DO", "1", Color(0xFFFF5252), 0.98f),
            XylophoneBar("RE", "2", Color(0xFFFF7A00), 0.92f),
            XylophoneBar("MI", "3", Color(0xFFFFC107), 0.86f),
            XylophoneBar("FA", "4", Color(0xFF4CAF50), 0.80f),
            XylophoneBar("SOL", "5", Color(0xFF00BCD4), 0.74f),
            XylophoneBar("LA", "6", Color(0xFF2979FF), 0.68f),
            XylophoneBar("SI", "7", Color(0xFF9C27B0), 0.62f),
            XylophoneBar("DO+", "8", Color(0xFFE91E63), 0.56f)
        )
    }

    var lastPlayedNote by remember { mutableStateOf<String?>(null) }
    var notesHistory by remember { mutableStateOf(listOf<String>()) }
    var guidedStep by remember { mutableIntStateOf(0) }

    // Guided melody "Estrellita" notes sequence: DO DO SOL SOL LA LA SOL
    val songSequence = remember { listOf(0, 0, 4, 4, 5, 5, 4) }
    val isSongCompleted = guidedStep >= songSequence.size

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF7F4FD)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎶", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Xilófono Didáctico Mágico",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Descubre notas, ritmos y melodías de calma",
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

                // Guided Song Prompt Card
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
                                text = if (isSongCompleted) "¡Bravo! 🎉 ¡Completaste Estrellita!" else "Canción Guiada: Estrellita dónde estás ⭐",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isSongCompleted) Color(0xFF2E7D32) else Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isSongCompleted)
                                    "Tocaste todas las notas a la perfección. ¡Eres un gran músico!"
                                else
                                    "Toca la nota que parpadea: ${bars[songSequence[guidedStep]].note} (${songSequence[guidedStep] + 1})",
                                fontSize = 12.sp,
                                color = Color(0xFF56476F)
                            )
                        }

                        if (isSongCompleted) {
                            IconButton(
                                onClick = { guidedStep = 0 },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEDE3FA))
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Reiniciar", tint = ClayDeepPurple)
                            }
                        }
                    }
                }

                // Xylophone Frame Container (Claymorphic)
                ClayCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(28.dp),
                    containerColor = Color(0xFFEDE3FA),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        bars.forEachIndexed { index, bar ->
                            val isTarget = !isSongCompleted && songSequence[guidedStep] == index
                            var isPressed by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(
                                targetValue = if (isPressed || isTarget) 1.03f else 1f,
                                animationSpec = tween(150),
                                label = "bar_scale"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(bar.widthFraction)
                                    .height(38.dp)
                                    .scale(scale)
                                    .shadow(
                                        elevation = if (isTarget) 10.dp else 4.dp,
                                        shape = RoundedCornerShape(12.dp),
                                        ambientColor = bar.color
                                    )
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bar.color)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        lastPlayedNote = bar.note
                                        notesHistory = (notesHistory + bar.note).takeLast(6)

                                        if (!isSongCompleted && songSequence[guidedStep] == index) {
                                            guidedStep++
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Screw pin dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.8f))
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = bar.note,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        if (isTarget) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(text = "👈", fontSize = 14.sp)
                                        }
                                    }

                                    // Screw pin dot
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.8f))
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes played history
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = ClayDeepPurple, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (notesHistory.isEmpty()) "Toca las notas para crear tu música" else "Melodía: ${notesHistory.joinToString(" - ")}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C3E63)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                ClayPuffyButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("¡Terminar de Tocar! 🎶", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}
