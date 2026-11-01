package com.example.ui.components.rewards

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MemoryCard(
    val id: Int,
    val key: String,
    val emoji: String,
    val label: String,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
)

@Composable
fun MemoryCalmGameDialog(
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val rawItems = listOf(
        Pair("🕊️", "Respirar"),
        Pair("💧", "Agua"),
        Pair("🍎", "Fruta"),
        Pair("🧸", "Abrazo"),
        Pair("🌙", "Dormir"),
        Pair("🎨", "Crear")
    )

    fun createShuffledDeck(): List<MemoryCard> {
        val list = mutableListOf<MemoryCard>()
        var idCounter = 1
        rawItems.forEach { (emoji, label) ->
            list.add(MemoryCard(idCounter++, label, emoji, label))
            list.add(MemoryCard(idCounter++, label, emoji, label))
        }
        return list.shuffled()
    }

    val cards = remember { mutableStateListOf<MemoryCard>().apply { addAll(createShuffledDeck()) } }
    var selectedFirstIndex by remember { mutableStateOf<Int?>(null) }
    var isEvaluating by remember { mutableStateOf(false) }
    var movesCount by remember { mutableIntStateOf(0) }
    var matchesCount by remember { mutableIntStateOf(0) }

    val allMatched = matchesCount == rawItems.size

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
                        Text(text = "🧠", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Memorama Mundo Calma",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Encuentra las parejas de hábitos felices",
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

                // Stats Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Parejas: $matchesCount / ${rawItems.size}  •  Intentos: $movesCount",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E4F77)
                    )

                    IconButton(
                        onClick = {
                            cards.clear()
                            cards.addAll(createShuffledDeck())
                            selectedFirstIndex = null
                            isEvaluating = false
                            movesCount = 0
                            matchesCount = 0
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEDE3FA))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = ClayDeepPurple)
                    }
                }

                // Cards Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(cards) { index, card ->
                        val isRevealed = card.isFaceUp || card.isMatched

                        Surface(
                            modifier = Modifier
                                .height(95.dp)
                                .shadow(
                                    elevation = if (card.isMatched) 2.dp else 6.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = if (card.isMatched) Color(0xFF81C784) else ClayShadowColor
                                )
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(enabled = !isEvaluating && !card.isMatched && !card.isFaceUp) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    card.isFaceUp = true

                                    if (selectedFirstIndex == null) {
                                        selectedFirstIndex = index
                                    } else {
                                        movesCount++
                                        val firstIndex = selectedFirstIndex!!
                                        val firstCard = cards[firstIndex]

                                        if (firstCard.key == card.key && firstCard.id != card.id) {
                                            // Match!
                                            firstCard.isMatched = true
                                            card.isMatched = true
                                            matchesCount++
                                            selectedFirstIndex = null
                                        } else {
                                            // No match
                                            isEvaluating = true
                                            scope.launch {
                                                delay(900)
                                                firstCard.isFaceUp = false
                                                card.isFaceUp = false
                                                selectedFirstIndex = null
                                                isEvaluating = false
                                            }
                                        }
                                    }
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = when {
                                card.isMatched -> Color(0xFFE8F5E9)
                                isRevealed -> Color.White
                                else -> Color(0xFFEDE3FA)
                            },
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    card.isMatched -> Color(0xFF4CAF50)
                                    isRevealed -> ClayDeepPurple
                                    else -> Color.White
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isRevealed) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = card.emoji, fontSize = 34.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = card.label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (card.isMatched) Color(0xFF2E7D32) else Color(0xFF281C3F)
                                        )
                                    }
                                } else {
                                    Text(text = "✨", fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }

                if (allMatched) {
                    ClayCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        shape = RoundedCornerShape(18.dp),
                        containerColor = Color(0xFFE8F8EE),
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🏆", fontSize = 28.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "¡Felicitaciones, Campeón/a!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "¡Memoria y concentración fantásticas! Lo lograste en $movesCount intentos.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }

                ClayPuffyButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("¡Terminar Juego! 🧠", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}
