package com.example.ui.components.rewards

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor

data class EmotionQuestion(
    val situation: String,
    val iconEmoji: String,
    val correctEmotion: String,
    val explanation: String,
    val options: List<Pair<String, String>> // Label, Emoji
)

@Composable
fun EmotionPuzzleGameDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val questions = remember {
        listOf(
            EmotionQuestion(
                situation = "Tu hermano o amigo te invita a jugar su videojuego favorito y te sonríe con cariño.",
                iconEmoji = "🎮",
                correctEmotion = "Alegría",
                explanation = "¡Exacto! Compartir y sentirnos acompañados nos llena de luz y felicidad.",
                options = listOf(
                    Pair("Alegría", "😊"),
                    Pair("Enojo", "😡"),
                    Pair("Miedo", "😨"),
                    Pair("Tristeza", "😢")
                )
            ),
            EmotionQuestion(
                situation = "Se apagaron las luces en una tormenta con relámpagos fuertes y ruidos extraños.",
                iconEmoji = "⛈️",
                correctEmotion = "Miedo",
                explanation = "¡Muy bien! El miedo nos avisa de un peligro, pero respirar hondo nos ayuda a calmarnos.",
                options = listOf(
                    Pair("Calma", "🕊️"),
                    Pair("Miedo", "😨"),
                    Pair("Alegría", "😄"),
                    Pair("Sorpresa", "😲")
                )
            ),
            EmotionQuestion(
                situation = "Se rompió el dibujo que hiciste con tanto esfuerzo después de toda una tarde.",
                iconEmoji = "🎨",
                correctEmotion = "Tristeza",
                explanation = "¡Correcto! Es normal estar triste cuando algo valioso se arruina. Llorar o pedir un abrazo sana el corazón.",
                options = listOf(
                    Pair("Tristeza", "😢"),
                    Pair("Alegría", "🤩"),
                    Pair("Miedo", "😨"),
                    Pair("Calma", "🌿")
                )
            ),
            EmotionQuestion(
                situation = "Alguien tomó tus colores sin pedirte permiso y no te los quiere devolver.",
                iconEmoji = "✏️",
                correctEmotion = "Enojo",
                explanation = "¡Eso es! El enojo surge cuando algo parece injusto. Hablar con calma nos ayuda a resolverlo.",
                options = listOf(
                    Pair("Enojo", "😠"),
                    Pair("Alegría", "😊"),
                    Pair("Tristeza", "🥺"),
                    Pair("Calma", "🕊️")
                )
            ),
            EmotionQuestion(
                situation = "Estás acostado/a en tu cama suavecita después de un baño tibio escuchando música bajita.",
                iconEmoji = "🌙",
                correctEmotion = "Calma",
                explanation = "¡Perfecto! La calma es ese calorcito dulce en el pecho donde todo se siente seguro y en paz.",
                options = listOf(
                    Pair("Calma", "🕊️"),
                    Pair("Enojo", "😤"),
                    Pair("Miedo", "😨"),
                    Pair("Tristeza", "😢")
                )
            )
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedEmotion by remember { mutableStateOf<String?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var hasAnswered by remember { mutableStateOf(false) }

    val currentQ = questions[currentIndex]
    val isFinished = currentIndex >= questions.size - 1 && hasAnswered

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF9F5FE)
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
                        Text(text = "🧩", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Puzzle Didáctico de Emociones",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Aprende a reconocer tus sentimientos jugando",
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

                // Progress & Stars Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pregunta ${currentIndex + 1} de ${questions.size}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E4F77)
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF8E1),
                        border = BorderStroke(1.dp, Color(0xFFFFD54F))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$score ⭐",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                // Question Card (Claymorphic)
                ClayCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    containerColor = Color.White,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .shadow(6.dp, CircleShape, ambientColor = Color(0x306B4FA0))
                                .clip(CircleShape)
                                .background(Color(0xFFEDE3FA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentQ.iconEmoji, fontSize = 36.sp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "¿Qué emoción se siente aquí?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayDeepPurple
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "\"${currentQ.situation}\"",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }

                // 4 Emotion Options Grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentQ.options.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { (label, emoji) ->
                                val isSelected = selectedEmotion == label
                                val isCorrect = label == currentQ.correctEmotion
                                val showResult = hasAnswered

                                val containerColor = when {
                                    showResult && isCorrect -> Color(0xFFE8F5E9)
                                    showResult && isSelected && !isCorrect -> Color(0xFFFFEBEE)
                                    isSelected -> Color(0xFFEDE3FA)
                                    else -> Color.White
                                }

                                val borderColor = when {
                                    showResult && isCorrect -> Color(0xFF4CAF50)
                                    showResult && isSelected && !isCorrect -> Color(0xFFE57373)
                                    isSelected -> ClayDeepPurple
                                    else -> Color(0xFFDDD2ED)
                                }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable(enabled = !hasAnswered) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedEmotion = label
                                            hasAnswered = true
                                            if (label == currentQ.correctEmotion) {
                                                score += 20
                                            }
                                        },
                                    shape = RoundedCornerShape(18.dp),
                                    color = containerColor,
                                    border = BorderStroke(1.5.dp, borderColor)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = label,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF1E1333)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Explanation & Next Button
                AnimatedVisibility(visible = hasAnswered) {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        containerColor = if (selectedEmotion == currentQ.correctEmotion) Color(0xFFE8F8EE) else Color(0xFFFFF3E0),
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (selectedEmotion == currentQ.correctEmotion) "¡Acertaste! 🌟" else "¡Buen intento! Aprendamos juntos 🌿",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (selectedEmotion == currentQ.correctEmotion) Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = currentQ.explanation,
                                fontSize = 12.sp,
                                color = Color(0xFF37474F),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                // Next or Finish Button
                ClayPuffyButton(
                    onClick = {
                        if (isFinished) {
                            onDismiss()
                        } else if (hasAnswered) {
                            currentIndex++
                            selectedEmotion = null
                            hasAnswered = false
                        } else {
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isFinished) "¡Completaste el Puzzle! 🏆 Salir" else if (hasAnswered) "Siguiente Emoción ➡️" else "Salir del Juego",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
