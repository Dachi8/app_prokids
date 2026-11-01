package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RoutineEntity
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun RoutinesScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val todayDateString = viewModel.getTodayDateString()
    val dimens = rememberClayDimens()

    var selectedFilterCategory by remember { mutableStateOf<String?>("TODAS") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredRoutines = if (selectedFilterCategory == null || selectedFilterCategory == "TODAS") {
        routines
    } else {
        routines.filter { it.category == selectedFilterCategory }
    }

    val completedTodayCount = routines.count { it.isCompletedOn(todayDateString) }
    val totalCount = routines.size
    val progress = if (totalCount > 0) completedTodayCount.toFloat() / totalCount else 0f
    val isAllDone = totalCount > 0 && completedTodayCount == totalCount

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F4FD),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(dimens.cardRadius),
                modifier = Modifier
                    .size(dimens.fabSize)
                    .shadow(8.dp, RoundedCornerShape(dimens.cardRadius), ambientColor = ClayShadowColor)
                    .testTag("add_routine_fab")
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar rutina",
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimens.screenPadding),
            verticalArrangement = Arrangement.spacedBy(dimens.gap)
        ) {
            // Header: User Representation & Title
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Rutinas de ${currentUser?.name ?: "Bienestar"}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "Hábitos saludables para cuerpo, mente y corazón",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )
                    }

                    // User Avatar Pill
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
                                text = "Activo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple
                            )
                        }
                    }
                }
            }

            // Tarjeta de sincronización con MySQL
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
                                text = "Rutinas Sincronizadas con MySQL ☁️💾",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Tus rutinas y rachas se guardan en la base de datos MySQL del servidor y se sincronizan entre todos tus dispositivos vinculados.",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            // Claymorphic Summary Progress Card
            item {
                ClayElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    containerColor = if (isAllDone) Color(0xFFECE4F8) else Color.White,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAllDone) "¡Día completado con éxito! 🎉" else "Progreso de Hoy",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "$completedTodayCount de $totalCount rutinas cumplidas",
                                    fontSize = 13.sp,
                                    color = Color(0xFF56476F)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEDE3FA),
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFFE9E1F7)
                        )

                        if (isAllDone) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "¡Excelente dedicación! Has completado todos tus hábitos hoy.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        "TODAS" to "Todas",
                        "MAÑANA" to "🌅 Mañana",
                        "BIENESTAR" to "🧘 Bienestar",
                        "NOCHE" to "🌙 Noche"
                    )

                    items(categories) { (catKey, label) ->
                        val isSelected = selectedFilterCategory == catKey
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedFilterCategory = catKey },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) ClayDeepPurple else Color.White,
                            border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color(0xFFDDD2ED))
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else Color(0xFF56476F),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Routines List
            if (filteredRoutines.isEmpty()) {
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
                                text = "No hay rutinas en esta categoría",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = "Pulsa el botón '+' para agregar un nuevo hábito saludable guardado en MySQL.",
                                fontSize = 12.sp,
                                color = Color(0xFF56476F),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredRoutines, key = { it.id }) { routine ->
                    val isDone = routine.isCompletedOn(todayDateString)
                    ClayRoutineItemCard(
                        routine = routine,
                        isDone = isDone,
                        onToggle = { viewModel.toggleRoutine(routine) },
                        onDelete = { viewModel.deleteRoutine(routine.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showAddDialog) {
        AddRoutineDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, desc, category ->
                viewModel.addRoutine(title, desc, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ClayRoutineItemCard(
    routine: RoutineEntity,
    isDone: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("routine_card_${routine.id}"),
        shape = RoundedCornerShape(22.dp),
        containerColor = if (isDone) Color(0xFFF9F7FC) else Color.White,
        elevation = if (isDone) 2.dp else 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isDone) "Completado" else "Pendiente",
                tint = if (isDone) MaterialTheme.colorScheme.primary else Color(0xFFB0A4C2),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = routine.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isDone) Color(0xFF86799C) else Color(0xFF1E1333)
                )

                if (routine.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = routine.description,
                        fontSize = 12.sp,
                        color = Color(0xFF56476F),
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryLabel = when (routine.category) {
                        "MAÑANA" -> "🌅 Mañana"
                        "BIENESTAR" -> "🧘 Bienestar"
                        else -> "🌙 Noche"
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE3FA),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text(
                            text = categoryLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ClayDeepPurple,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (routine.streak > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            border = BorderStroke(1.dp, Color(0xFFFFE0B2))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = "Racha",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${routine.streak} días",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE65100)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "☁️ Sincronizado",
                        fontSize = 10.sp,
                        color = Color(0xFF75678B)
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFA597B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, desc: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("BIENESTAR") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        titleContentColor = Color(0xFF1E1333),
        textContentColor = Color(0xFF1E1333),
        shape = RoundedCornerShape(26.dp),
        title = {
            Text("Nuevo Hábito Saludable", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF1E1333))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre del hábito") },
                    placeholder = { Text("Ej. Beber un vaso de agua") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = ClayDeepPurple,
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFAF8FE),
                        unfocusedContainerColor = Color(0xFFFAF8FE),
                        focusedLabelColor = ClayDeepPurple,
                        unfocusedLabelColor = Color(0xFF7E6F94)
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Descripción o motivación") },
                    placeholder = { Text("Ej. Para empezar el día con energía") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = ClayDeepPurple,
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFAF8FE),
                        unfocusedContainerColor = Color(0xFFFAF8FE),
                        focusedLabelColor = ClayDeepPurple,
                        unfocusedLabelColor = Color(0xFF7E6F94)
                    )
                )

                Text("Momento del día:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1333))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "MAÑANA" to "🌅 Mañana",
                        "BIENESTAR" to "🧘 Día",
                        "NOCHE" to "🌙 Noche"
                    ).forEach { (key, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedCategory = key }
                        ) {
                            RadioButton(
                                selected = selectedCategory == key,
                                onClick = { selectedCategory = key }
                            )
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title.trim(), desc.trim(), selectedCategory)
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Guardar en MySQL", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
