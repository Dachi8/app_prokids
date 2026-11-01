package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.MoodEntryEntity
import com.example.data.model.MoodType
import com.example.ui.components.WeeklyMoodChart
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.viewmodel.WellnessViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val moods by viewModel.moodEntries.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Form states
    var selectedMoodType by remember(todayMood) {
        mutableStateOf(todayMood?.let { MoodType.fromScore(it.moodScore) } ?: MoodType.GOOD)
    }
    var energyLevel by remember(todayMood) {
        mutableFloatStateOf(todayMood?.energyLevel?.toFloat() ?: 3f)
    }
    val selectedEmotions = remember(todayMood) {
        mutableStateListOf<String>().apply {
            todayMood?.emotionTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { addAll(it) }
        }
    }
    val selectedContexts = remember(todayMood) {
        mutableStateListOf<String>().apply {
            todayMood?.contextTags?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { addAll(it) }
        }
    }
    var noteText by remember(todayMood) {
        mutableStateOf(todayMood?.note ?: "")
    }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }

    val emotionOptions = listOf(
        "Calma", "Gratitud", "Esperanza", "Alegría", "Paz", "Amor",
        "Cansancio", "Estrés", "Tristeza", "Ansiedad", "Preocupación", "Soledad", "Motivación"
    )

    val contextOptions = listOf(
        "Familia", "Escuela", "Juegos", "Salud", "Sueño", "Amigos", "Música", "Tiempo libre"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FD))
    ) {
        // Tab Row: Registrar vs Historial
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Registrar Hoy", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.Mood, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Historial (${moods.size})", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (selectedTabIndex == 0) {
            // Form to log mood
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with User Representation
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "¿Cómo te sientes hoy, ${currentUser?.name ?: "Amiguito/a"}?",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Elige tu emoción y energía. Se guardará sin internet.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF56476F)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFECE4F8),
                            border = BorderStroke(1.dp, Color.White),
                            modifier = Modifier.shadow(2.dp, RoundedCornerShape(14.dp), ambientColor = ClayShadowColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = currentUser?.avatarEmoji ?: "🌱", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Guardado en la nube",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple
                                )
                            }
                        }
                    }
                }

                // Insignia de almacenamiento en MySQL
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color(0xFFE8F5E9),
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFC8E6C9),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDone,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Historial Sincronizado en MySQL ☁️💾",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1B5E20)
                                )
                                Text(
                                    text = "Cada registro se guarda en la base de datos MySQL del servidor. Podrás consultarlo desde cualquier dispositivo vinculado.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E7D32),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                // Mood faces row
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(26.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Tu emoción principal:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MoodType.entries.forEach { mood ->
                                    val isSelected = selectedMoodType == mood
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(
                                                if (isSelected) Color(0xFFECE4F8)
                                                else Color.Transparent
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) ClayDeepPurple else Color(0xFFE2DCEB),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable {
                                                selectedMoodType = mood
                                                saveSuccessMessage = null
                                            }
                                            .padding(vertical = 8.dp, horizontal = 2.dp)
                                            .testTag("mood_option_${mood.score}")
                                    ) {
                                        Text(text = mood.emoji, fontSize = 26.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = mood.labelEs,
                                            fontSize = 9.5.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) ClayDeepPurple else Color(0xFF56476F),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Energy level slider
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
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
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Nivel de Energía",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1333)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF8E1),
                                    border = BorderStroke(1.dp, Color(0xFFFFE082))
                                ) {
                                    Text(
                                        text = "${energyLevel.toInt()} / 5",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Slider(
                                value = energyLevel,
                                onValueChange = { energyLevel = it },
                                valueRange = 1f..5f,
                                steps = 3,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary,
                                    inactiveTrackColor = Color(0xFFE9E1F7)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Poco activo 🥱", fontSize = 11.sp, color = Color(0xFF75678B))
                                Text("Lleno de energía ⚡", fontSize = 11.sp, color = Color(0xFF75678B))
                            }
                        }
                    }
                }

                // Emotion tags
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "¿Qué palabras describen lo que sientes?",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                emotionOptions.forEach { emotion ->
                                    val isSelected = selectedEmotions.contains(emotion)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            if (isSelected) selectedEmotions.remove(emotion)
                                            else selectedEmotions.add(emotion)
                                        },
                                        label = { Text(emotion, fontSize = 12.sp) },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                        } else null,
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFFECE4F8),
                                            selectedLabelColor = ClayDeepPurple
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes input
                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "Nota o pensamiento libre (opcional):",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                placeholder = { Text("Escribe algo que quieras recordar...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(16.dp)
                            )
                        }
                    }
                }

                // Success Message
                if (saveSuccessMessage != null) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            containerColor = Color(0xFFE8F5E9),
                            elevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "✅", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = saveSuccessMessage!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Save Button
                item {
                    ClayPuffyButton(
                        onClick = {
                            viewModel.saveMood(
                                moodScore = selectedMoodType.score,
                                moodLabel = selectedMoodType.labelEs,
                                moodEmoji = selectedMoodType.emoji,
                                energyLevel = energyLevel.toInt(),
                                emotionTags = selectedEmotions.toList(),
                                contextTags = selectedContexts.toList(),
                                note = noteText
                            )
                            saveSuccessMessage = "¡Ánimo guardado en MySQL! +15 estrellas ⭐"
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("save_mood_button"),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Text("Guardar Ánimo (+15 ⭐)", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // Tab 1: Historial Detallado y Gráfica Semanal (MySQL)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Historial de Ánimo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                    Text(
                        text = "Tus registros se guardan y leen directamente de la base de datos MySQL del servidor.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF56476F)
                    )
                }

                // Weekly Chart Card
                item {
                    WeeklyMoodChart(moodEntries = moods)
                }

                // Summary Stats Cards
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ClayCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Días registrados",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF56476F)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${moods.size} 📴",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        ClayCard(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Ánimo frecuente",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF56476F)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val mostCommonEmoji = moods.groupBy { it.moodEmoji }.maxByOrNull { it.value.size }?.key ?: "—"
                                Text(
                                    text = mostCommonEmoji,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Registros Almacenados Localmente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                if (moods.isEmpty()) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = Color.White,
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(26.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🌱", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Aún no tienes registros guardados.",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Comienza registrando cómo te sientes hoy en la primera pestaña.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF56476F)
                                )
                            }
                        }
                    }
                } else {
                    items(moods, key = { it.id }) { moodItem ->
                        ClayMoodHistoryCard(
                            entry = moodItem,
                            onDelete = { viewModel.deleteMood(moodItem.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClayMoodHistoryCard(
    entry: MoodEntryEntity,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-ES"))
    val displayDate = try {
        val sdfParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val d = sdfParse.parse(entry.dateString)
        if (d != null) dateFormat.format(d) else entry.dateString
    } catch (e: Exception) {
        entry.dateString
    }

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = entry.moodEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = entry.moodLabel,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "$displayDate • 🌱 Sincronizado",
                            fontSize = 11.sp,
                            color = Color(0xFF75678B)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFA597B8),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (entry.emotionTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.emotionTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(5).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFECE4F8),
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            if (entry.note.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"${entry.note}\"",
                    fontSize = 12.sp,
                    color = Color(0xFF4C3D65),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
