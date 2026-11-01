package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.JournalEntryEntity
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.viewmodel.WellnessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun JournalScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.allJournalEntries.collectAsState()
    var showNewEntryDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewEntryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(8.dp, CircleShape, ambientColor = ClayShadowColor)
                    .testTag("new_journal_entry_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Escribir en libreta", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F4FD))
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = ClayShadowColor),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFECE4F8),
                        border = BorderStroke(1.5.dp, Color.White)
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Libreta de Sentimientos",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "Un espacio seguro para plasmar lo que sientes sin filtros.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )
                    }
                }
            }

            // Quick Banner with points reward info
            item {
                ClayElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color(0xFFECE4F8),
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⭐", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Expresar tus emociones sana y premia",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF221142)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Por cada entrada ganas +25 Puntos de Bienestar para canjear por premios de autocuidado.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4C3E63),
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            if (entries.isEmpty()) {
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.White,
                        shape = RoundedCornerShape(26.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📖", fontSize = 44.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tu libreta está lista",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca el botón + abajo para desahogarte y escribir tu primera reflexión.",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF56476F)
                            )
                        }
                    }
                }
            } else {
                items(entries, key = { it.id }) { entry ->
                    ClayJournalEntryCard(
                        entry = entry,
                        onDelete = { viewModel.deleteJournalEntry(entry.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showNewEntryDialog) {
        WriteJournalDialog(
            onDismiss = { showNewEntryDialog = false },
            onSave = { title, content, emotion, emotionEmoji, intensity, isShared ->
                viewModel.addJournalEntry(
                    title = title,
                    content = content,
                    emotion = emotion,
                    emotionEmoji = emotionEmoji,
                    intensity = intensity,
                    isSharedWithCareTeam = isShared,
                    onSuccess = { showNewEntryDialog = false }
                )
            }
        )
    }
}

@Composable
fun ClayJournalEntryCard(
    entry: JournalEntryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d 'de' MMMM, HH:mm", Locale.forLanguageTag("es-ES"))
    val displayDate = dateFormat.format(Date(entry.timestamp))

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("journal_card_${entry.id}"),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        elevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = entry.emotionEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.emotion,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (entry.isSharedWithCareTeam) Color(0xFFEFE8FB) else Color(0xFFF2EFF6),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (entry.isSharedWithCareTeam) Icons.Default.Public else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (entry.isSharedWithCareTeam) MaterialTheme.colorScheme.primary else Color(0xFF6B5E83)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (entry.isSharedWithCareTeam) "Visible a médico" else "Privado",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (entry.isSharedWithCareTeam) MaterialTheme.colorScheme.primary else Color(0xFF6B5E83)
                            )
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFA59AB9),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = entry.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF38294F),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Intensity dots
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Intensidad: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6E5F86)
                    )
                    repeat(entry.intensity) {
                        Text(text = "●", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    repeat(5 - entry.intensity) {
                        Text(text = "○", fontSize = 11.sp, color = Color(0xFFCEC4DD))
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }

                Text(
                    text = displayDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF7A6D90),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteJournalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, emotion: String, emotionEmoji: String, intensity: Int, isShared: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedEmotion by remember { mutableStateOf("Esperanza") }
    var selectedEmoji by remember { mutableStateOf("🌱") }
    var intensity by remember { mutableFloatStateOf(3f) }
    var isShared by remember { mutableStateOf(true) }

    val emotions = listOf(
        Pair("Esperanza", "🌱"),
        Pair("Calma", "🕊️"),
        Pair("Gratitud", "🙏"),
        Pair("Ansiedad", "😰"),
        Pair("Tristeza", "🌧️"),
        Pair("Enojo", "⚡"),
        Pair("Cansancio", "💤"),
        Pair("Alegría", "✨")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x35654BE2)),
                shape = RoundedCornerShape(28.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFEFE8F8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Título principal
                    Text(
                        text = "Escribir en Libreta Emocional",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1E1333)
                    )

                    // Campo de Título
                    Column {
                        Text(
                            text = "Título de esta entrada:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = {
                                Text("Ej. Una pequeña victoria hoy", color = Color(0xFF8E81A5), fontSize = 14.sp)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1E1333),
                                unfocusedTextColor = Color(0xFF1E1333),
                                focusedBorderColor = ClayDeepPurple,
                                unfocusedBorderColor = Color(0xFFDDD2ED),
                                focusedContainerColor = Color(0xFFF9F7FD),
                                unfocusedContainerColor = Color(0xFFF9F7FD)
                            )
                        )
                    }

                    // Emoción predominante
                    Column {
                        Text(
                            text = "Emoción predominante:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            emotions.forEach { (name, emoji) ->
                                val isSelected = selectedEmotion == name
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            selectedEmotion = name
                                            selectedEmoji = emoji
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ClayDeepPurple else Color(0xFFF4EFFB),
                                    border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color(0xFFE2D7F2))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF382954)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Intensidad emocional
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intensidad emocional:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1333)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFECE4F8)
                            ) {
                                Text(
                                    text = "${intensity.toInt()}/5",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ClayDeepPurple,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Slider(
                            value = intensity,
                            onValueChange = { intensity = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = ClayDeepPurple,
                                activeTrackColor = ClayDeepPurple,
                                inactiveTrackColor = Color(0xFFE7DDF6)
                            )
                        )
                    }

                    // Contenido / Reflexión
                    Column {
                        Text(
                            text = "¿Qué sientes en este momento?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = {
                                Text(
                                    "Escribe libremente todo lo que está pasando en tu mente y tu corazón...",
                                    color = Color(0xFF8E81A5),
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            maxLines = 8,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1E1333),
                                unfocusedTextColor = Color(0xFF1E1333),
                                focusedBorderColor = ClayDeepPurple,
                                unfocusedBorderColor = Color(0xFFDDD2ED),
                                focusedContainerColor = Color(0xFFF9F7FD),
                                unfocusedContainerColor = Color(0xFFF9F7FD)
                            )
                        )
                    }

                    // Compartir Switch
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8F5FD),
                        border = BorderStroke(1.dp, Color(0xFFECE4F8)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isShared = !isShared }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = "Compartir con mi médico y familiar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Permitirá que vean tu reflexión en sus paneles para brindarte apoyo.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF6B5C85),
                                    lineHeight = 15.sp
                                )
                            }
                            Switch(
                                checked = isShared,
                                onCheckedChange = { isShared = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ClayDeepPurple,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFDED5EB)
                                )
                            )
                        }
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Cancelar",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B5C85)
                            )
                        }
                        ClayPuffyButton(
                            onClick = {
                                if (title.isNotBlank() && content.isNotBlank()) {
                                    onSave(title, content, selectedEmotion, selectedEmoji, intensity.toInt(), isShared)
                                }
                            },
                            enabled = title.isNotBlank() && content.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = ClayDeepPurple
                        ) {
                            Text(
                                text = "Guardar (+25 pts)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
