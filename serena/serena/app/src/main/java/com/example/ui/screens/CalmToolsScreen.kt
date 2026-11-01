package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Waves
import com.example.ui.components.calm.AmbientSoundMiniPlayer
import com.example.ui.components.calm.AmbientSoundsTool
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CalmToolsScreen(
    modifier: Modifier = Modifier
) {
    var selectedToolIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TabRow(
            selectedTabIndex = selectedToolIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedToolIndex == 0,
                onClick = { selectedToolIndex = 0 },
                text = { Text("Respiración", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Air, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedToolIndex == 1,
                onClick = { selectedToolIndex = 1 },
                text = { Text("Sonidos", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedToolIndex == 2,
                onClick = { selectedToolIndex = 2 },
                text = { Text("5-4-3-2-1", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedToolIndex == 3,
                onClick = { selectedToolIndex = 3 },
                text = { Text("Líneas SOS", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                icon = { Icon(Icons.Default.MedicalServices, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedToolIndex) {
                0 -> GuidedBreathingTool()
                1 -> AmbientSoundsTool()
                2 -> GroundingTechniqueTool()
                3 -> EmergencyResourcesTool()
            }
        }

        // Mini player sits cleanly at bottom of column when sound is active on other tabs
        if (selectedToolIndex != 1) {
            AmbientSoundMiniPlayer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

enum class BreathPhase(val label: String, val instruction: String) {
    INHALE("Inhala...", "Llena tus pulmones por la nariz con suavidad"),
    HOLD("Sostén...", "Mantén el aire y siente la calma en tu pecho"),
    EXHALE("Exhala...", "Suelta todo el aire por la boca lentamente")
}

@Composable
fun GuidedBreathingTool() {
    val context = LocalContext.current
    var isRunning by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var secondsRemaining by remember { mutableIntStateOf(4) }
    var cycleCount by remember { mutableIntStateOf(0) }
    var is478Mode by remember { mutableStateOf(true) } // true: 4-7-8, false: 4-4-4

    fun triggerVibration() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    // Breathing loop timer
    LaunchedEffect(isRunning, is478Mode) {
        if (!isRunning) return@LaunchedEffect
        while (isRunning) {
            // INHALE phase: 4 seconds
            currentPhase = BreathPhase.INHALE
            triggerVibration()
            for (sec in 4 downTo 1) {
                secondsRemaining = sec
                delay(1000)
                if (!isRunning) break
            }
            if (!isRunning) break

            // HOLD phase: 7 seconds for 4-7-8, or 4 seconds for square
            val holdDuration = if (is478Mode) 7 else 4
            currentPhase = BreathPhase.HOLD
            triggerVibration()
            for (sec in holdDuration downTo 1) {
                secondsRemaining = sec
                delay(1000)
                if (!isRunning) break
            }
            if (!isRunning) break

            // EXHALE phase: 8 seconds for 4-7-8, or 4 seconds for square
            val exhaleDuration = if (is478Mode) 8 else 4
            currentPhase = BreathPhase.EXHALE
            triggerVibration()
            for (sec in exhaleDuration downTo 1) {
                secondsRemaining = sec
                delay(1000)
                if (!isRunning) break
            }
            if (!isRunning) break

            cycleCount++
        }
    }

    val targetScale = when {
        !isRunning -> 1f
        currentPhase == BreathPhase.INHALE -> 1.45f
        currentPhase == BreathPhase.HOLD -> 1.45f
        else -> 0.85f
    }

    val animDuration = when {
        !isRunning -> 500
        currentPhase == BreathPhase.INHALE -> 4000
        currentPhase == BreathPhase.HOLD -> if (is478Mode) 7000 else 4000
        else -> if (is478Mode) 8000 else 4000
    }

    val circleScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = animDuration, easing = LinearEasing),
        label = "breath_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (is478Mode) "Respiración Relajante 4-7-8" else "Respiración Cuadrada 4-4-4",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (is478Mode) "Inhala 4s • Sostén 7s • Exhala 8s" else "Inhala 4s • Sostén 4s • Exhala 4s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row {
                OutlinedButton(
                    onClick = {
                        is478Mode = !is478Mode
                        isRunning = false
                        currentPhase = BreathPhase.INHALE
                        secondsRemaining = 4
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (is478Mode) "Cambiar a Respiración Cuadrada" else "Cambiar a Técnica 4-7-8",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Animated Breathing Bubble
        Box(
            modifier = Modifier
                .size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer soft glow aura
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(circleScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Inner breathing circle
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .scale(circleScale)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (isRunning) currentPhase.label else "Listo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (isRunning) {
                        Text(
                            text = "$secondsRemaining",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Subtext and instructions
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isRunning) currentPhase.instruction else "Presiona Iniciar para comenzar a respirar en calma.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )

            if (cycleCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ciclos completados: $cycleCount 🌱",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Play / Pause / Reset Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("toggle_breathing_button")
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isRunning) "Pausar" else "Iniciar respiración",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                IconButton(
                    onClick = {
                        isRunning = false
                        currentPhase = BreathPhase.INHALE
                        secondsRemaining = 4
                        cycleCount = 0
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reiniciar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun GroundingTechniqueTool() {
    val items54321 = listOf(
        Pair("👁️ 5 Cosas que puedes VER", "Observa a tu alrededor: una planta, la luz, un cuadro, tus manos, una textura."),
        Pair("✋ 4 Cosas que puedes TOCAR", "Siente la textura de tu ropa, la mesa fría, tus pies sobre el suelo, tu cabello."),
        Pair("👂 3 Sonidos que puedes ESCUCHAR", "Escucha el viento, los pájaros, el zumbido de un aparato o tu propia respiración."),
        Pair("👃 2 Olores que puedes PERCIBIR", "Huele tu piel, un jabón, un café o simplemente el aire fresco."),
        Pair("👅 1 Sabor en tu BOCA", "Reconoce el sabor en tu boca, toma un sorbo de agua o nota tu respiración.")
    )

    val checkedStates = remember { mutableStateListOf(false, false, false, false, false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Técnica de Conexión 5-4-3-2-1",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cuando sientas que tu mente se acelera o la ansiedad sube, usa tus 5 sentidos para volver al momento presente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(items54321.size) { index ->
            val (title, description) = items54321[index]
            val isChecked = checkedStates[index]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { checkedStates[index] = !checkedStates[index] },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checkedStates[index] = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    for (i in 0 until checkedStates.size) checkedStates[i] = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Reiniciar ejercicio")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EmergencyResourcesTool() {
    val context = LocalContext.current

    val hotlines = listOf(
        Triple("México: Línea de la Vida", "800 911 2000", "Atención psicológica gratuita 24/7"),
        Triple("EE.UU. y Puerto Rico", "988", "Línea de Prevención del Suicidio y Crisis (Español disponible)"),
        Triple("España: Atención a la conducta suicida", "024", "Servicio público, confidencial y gratuito 24/7"),
        Triple("Colombia: Línea 106", "106", "Orientación en salud mental gratuita"),
        Triple("Argentina: Centro de Asistencia", "135", "Línea de ayuda confidencial"),
        Triple("Chile: Fono Salud Responde", "600 360 7777", "Apoyo en salud mental"),
        Triple("Emergencias Generales", "911", "Atención inmediata de urgencias médicas")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "No estás solo/a",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Si estás atravesando una crisis, sientes deseos de hacerte daño o sientes que no puedes más, por favor recurre a estas líneas de ayuda inmediata con profesionales cálidos y empáticos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        items(items = hotlines) { hotline ->
            val country = hotline.first
            val phone = hotline.second
            val description = hotline.third
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = country,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("call_button_$phone")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Llamar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Llamar", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
