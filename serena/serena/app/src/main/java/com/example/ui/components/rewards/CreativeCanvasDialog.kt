package com.example.ui.components.rewards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor

data class DrawPoint(val offset: Offset, val color: Color, val strokeWidth: Float)

data class MagicStamp(val offset: Offset, val emoji: String)

@Composable
fun CreativeCanvasDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val colors = listOf(
        Pair(Color(0xFF6B4FA0), "Morado Serena"),
        Pair(Color(0xFFFF5252), "Fresa"),
        Pair(Color(0xFFFFB300), "Sol"),
        Pair(Color(0xFF4CAF50), "Menta"),
        Pair(Color(0xFF00BCD4), "Cielo"),
        Pair(Color(0xFFE91E63), "Rosa Mágico")
    )

    val stamps = listOf("⭐", "💖", "🌸", "🦋", "🌈")

    var selectedColor by remember { mutableStateOf(colors[0].first) }
    var selectedStamp by remember { mutableStateOf<String?>(null) }

    val points = remember { mutableStateListOf<DrawPoint>() }
    val placedStamps = remember { mutableStateListOf<MagicStamp>() }

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
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎨", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Lienzo Mágico de Arte",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Dibuja, estampa sellos y relájate con colores",
                                fontSize = 12.sp,
                                color = ClayDeepPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = {
                                points.clear()
                                placedStamps.clear()
                            },
                            modifier = Modifier
                                .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Limpiar", tint = Color(0xFFE53935))
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
                }

                // Color & Stamp Tools
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Pintura:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E4F77))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            colors.forEach { (color, _) ->
                                val isSelected = selectedColor == color && selectedStamp == null
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .shadow(if (isSelected) 6.dp else 2.dp, CircleShape, ambientColor = color)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable {
                                            selectedColor = color
                                            selectedStamp = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Stamps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sellos mágicos:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5E4F77))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            stamps.forEach { stamp ->
                                val isSelected = selectedStamp == stamp
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            selectedStamp = stamp
                                        },
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFFEDE3FA) else Color.White,
                                    border = BorderStroke(1.5.dp, if (isSelected) ClayDeepPurple else Color(0xFFDDD2ED))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = stamp, fontSize = 18.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Drawing Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp)
                        .shadow(8.dp, RoundedCornerShape(26.dp), ambientColor = ClayShadowColor)
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color.White)
                        .pointerInput(selectedColor, selectedStamp) {
                            detectTapGestures { offset ->
                                if (selectedStamp != null) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    placedStamps.add(MagicStamp(offset, selectedStamp!!))
                                } else {
                                    points.add(DrawPoint(offset, selectedColor, 12f))
                                }
                            }
                        }
                        .pointerInput(selectedColor, selectedStamp) {
                            detectDragGestures { change, _ ->
                                if (selectedStamp == null) {
                                    points.add(DrawPoint(change.position, selectedColor, 12f))
                                }
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        for (i in 0 until points.size) {
                            drawCircle(
                                color = points[i].color,
                                radius = points[i].strokeWidth,
                                center = points[i].offset
                            )
                        }
                    }

                    // Render placed stamps
                    placedStamps.forEach { stamp ->
                        Text(
                            text = stamp.emoji,
                            fontSize = 32.sp,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(
                                    start = (stamp.offset.x / 2.8f).dp,
                                    top = (stamp.offset.y / 2.8f).dp
                                )
                        )
                    }

                    if (points.isEmpty() && placedStamps.isEmpty()) {
                        Text(
                            text = "¡Desliza tu dedito aquí para dibujar o toca con un sello mágico! 🎨✨",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA89BBF),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 24.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                ClayPuffyButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("¡Guardar mi Arte! 🎨", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}
