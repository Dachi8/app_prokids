package com.example.ui.components.calm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.util.AmbientSoundManager
import com.example.util.AmbientSoundType

@Composable
fun AmbientSoundsTool(
    modifier: Modifier = Modifier
) {
    val currentSound by AmbientSoundManager.currentSound.collectAsState()
    val isPlaying by AmbientSoundManager.isPlaying.collectAsState()
    val volume by AmbientSoundManager.volume.collectAsState()
    val timerMinutes by AmbientSoundManager.timerMinutes.collectAsState()
    val timerSecondsLeft by AmbientSoundManager.timerSecondsLeft.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Biblioteca de Sonidos Ambientales 🎧",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1333)
            )
            Text(
                text = "Paisajes sonoros naturales para calmar la mente y acompañar tus respiraciones.",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF56476F)
            )
        }

        // Master Control Card: Volume & Sleep Timer
        item {
            ClayElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                containerColor = Color.White,
                elevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Header Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isPlaying) Color(0xFFECE4F8) else Color(0xFFF3F0F7),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentSound?.emoji ?: "🎵",
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isPlaying && currentSound != null) "Reproduciendo: ${currentSound!!.title}" else "Sonidos de Relajación",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = if (isPlaying) "Audio ambiental en segundo plano" else "Elige un sonido para comenzar",
                                    fontSize = 11.sp,
                                    color = if (isPlaying) Color(0xFF2E7D32) else Color(0xFF75678B),
                                    fontWeight = if (isPlaying) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }

                        if (currentSound != null) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { AmbientSoundManager.togglePlayPause() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(ClayDeepPurple)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { AmbientSoundManager.stop() },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEDE3FA))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Detener",
                                        tint = ClayDeepPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Volume Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                            contentDescription = null,
                            tint = Color(0xFF75678B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = volume,
                            onValueChange = { AmbientSoundManager.setVolume(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color(0xFFE9E1F7)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = Color(0xFF75678B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${(volume * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayDeepPurple
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Timer Selector Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (timerSecondsLeft > 0) {
                                    val m = timerSecondsLeft / 60
                                    val s = timerSecondsLeft % 60
                                    "Auto-apagado: %02d:%02d".format(m, s)
                                } else {
                                    "Temporizador:"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF56476F)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(0 to "Infinito", 5 to "5m", 10 to "10m", 15 to "15m").forEach { (mins, label) ->
                                val isSelected = timerMinutes == mins
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { AmbientSoundManager.setTimer(mins) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) ClayDeepPurple else Color(0xFFF3ECFA),
                                    border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color.White)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else ClayDeepPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Sonidos Disponibles
        item {
            Text(
                text = "Sonidos de la Naturaleza",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1333)
            )
        }

        // Sounds Cards List
        items(AmbientSoundType.entries) { soundType ->
            val isCurrent = currentSound == soundType
            val isThisPlaying = isCurrent && isPlaying

            AmbientSoundCard(
                soundType = soundType,
                isCurrent = isCurrent,
                isPlaying = isThisPlaying,
                onPlayToggle = {
                    AmbientSoundManager.playSound(soundType)
                }
            )
        }

        // Sensory Benefits Callout
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                containerColor = Color(0xFFE8F5E9),
                elevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🌿", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "¿Por qué ayudan los sonidos ambientales?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "El sonido continuo y suave de la lluvia, las olas del mar o el bosque activa el nervio vago y estimula el sistema nervioso parasimpático. Esto reduce el cortisol, desciende la frecuencia cardíaca y ayuda a los niños a autorregularse durante momentos de tensión o antes de dormir.",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun AmbientSoundCard(
    soundType: AmbientSoundType,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sound_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayToggle() }
            .testTag("ambient_sound_card_${soundType.id}"),
        shape = RoundedCornerShape(24.dp),
        containerColor = if (isCurrent) Color(0xFFFAF6FE) else Color.White,
        elevation = if (isCurrent) 6.dp else 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sound Emoji Avatar
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .scale(if (isPlaying) pulseScale else 1f)
                        .shadow(if (isCurrent) 6.dp else 2.dp, CircleShape, ambientColor = ClayShadowColor)
                        .clip(CircleShape)
                        .background(if (isCurrent) Color(0xFFECE4F8) else Color(0xFFF7F5FB))
                        .border(
                            width = if (isCurrent) 2.dp else 1.dp,
                            color = if (isCurrent) ClayDeepPurple else Color.White,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = soundType.emoji, fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = soundType.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        if (isPlaying) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "En reproducción",
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = soundType.subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClayDeepPurple
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = soundType.benefits,
                        fontSize = 11.sp,
                        color = Color(0xFF56476F),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Play / Pause Button
            ClayPuffyButton(
                onClick = onPlayToggle,
                shape = RoundedCornerShape(16.dp),
                containerColor = if (isPlaying) ClayDeepPurple else Color(0xFFEDE3FA),
                contentColor = if (isPlaying) Color.White else ClayDeepPurple,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun AmbientSoundMiniPlayer(
    modifier: Modifier = Modifier
) {
    val currentSound by AmbientSoundManager.currentSound.collectAsState()
    val isPlaying by AmbientSoundManager.isPlaying.collectAsState()
    val timerSecondsLeft by AmbientSoundManager.timerSecondsLeft.collectAsState()

    AnimatedVisibility(
        visible = currentSound != null,
        modifier = modifier
    ) {
        val sound = currentSound ?: return@AnimatedVisibility

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = ClayShadowColor),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF221142),
            border = BorderStroke(1.dp, Color(0xFF6B4FA0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = sound.emoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = sound.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isPlaying) {
                                if (timerSecondsLeft > 0) {
                                    val m = timerSecondsLeft / 60
                                    val s = timerSecondsLeft % 60
                                    "Reproduciendo • %02d:%02d".format(m, s)
                                } else {
                                    "Reproduciendo sonido de calma 🎧"
                                }
                            } else {
                                "Pausado"
                            },
                            fontSize = 10.sp,
                            color = Color(0xFFD0BCFF)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { AmbientSoundManager.togglePlayPause() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color(0xFF221142),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = { AmbientSoundManager.stop() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF38235C))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Detener",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
