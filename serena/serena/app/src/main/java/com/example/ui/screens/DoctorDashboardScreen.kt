package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WeeklyMoodChart
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel
import java.util.Locale

@Composable
fun DoctorDashboardScreen(
    viewModel: WellnessViewModel,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = rememberClayDimens()
    val currentUser by viewModel.currentUser.collectAsState()
    val moods by viewModel.moodEntries.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val sharedJournal by viewModel.sharedJournalEntries.collectAsState()
    val tasks by viewModel.prescribedTasks.collectAsState()
    val patientPoints by viewModel.patientPoints.collectAsState()
    val linkedPatients by viewModel.linkedPatients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val todayDateString = viewModel.getTodayDateString()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tareas Terapéuticas, 1: Libreta & Evolución, 2: Plan Clínico
    var taskStatusFilter by remember { mutableStateOf("TODAS") } // "TODAS", "PENDIENTES", "COMPLETADAS"
    var showAssignTaskDialog by remember { mutableStateOf(false) }
    var showLinkPatientDialog by remember { mutableStateOf(false) }
    var linkCodeInput by remember { mutableStateOf("") }

    // Métricas clínicas
    val completedRoutinesToday = routines.count { it.isCompletedOn(todayDateString) }
    val totalRoutines = routines.size
    val routineRate = if (totalRoutines > 0) (completedRoutinesToday * 100 / totalRoutines) else 0

    val doctorTasks = tasks.filter { it.assignedByRole == "MEDICO" }
    val filteredDoctorTasks = when (taskStatusFilter) {
        "PENDIENTES" -> doctorTasks.filter { !it.isCompleted }
        "COMPLETADAS" -> doctorTasks.filter { it.isCompleted }
        else -> doctorTasks
    }

    val completedTasksCount = doctorTasks.count { it.isCompleted }
    val avgMood = if (moods.isNotEmpty()) moods.map { it.moodScore }.average() else 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F5FC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.gap)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Clinical Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.shadow(6.dp, CircleShape, ambientColor = ClayShadowColor),
                        shape = CircleShape,
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(modifier = Modifier.size(dimens.buttonHeightLarge), contentAlignment = Alignment.Center) {
                            Text(text = "🩺", fontSize = dimens.emojiSize)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.name ?: "Dr. Terapeuta",
                                fontSize = dimens.titleSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2F1)
                            ) {
                                Text(
                                    text = "Activo",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00796B),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Psicoterapia & Acompañamiento TCC",
                            fontSize = dimens.labelSize,
                            color = ClayDeepPurple,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón de actualizar: descarga del servidor los datos del paciente
                    Surface(
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { viewModel.refrescarDatosDelPaciente() }
                            .testTag("doctor_refresh_button"),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8E0F5))
                    ) {
                        Box(modifier = Modifier.size(dimens.buttonHeight), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Actualizar datos del paciente",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Botón de configuración (código de vinculación, pacientes, cuenta)
                    Surface(
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { onOpenSettings() }
                            .testTag("doctor_settings_button"),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8E0F5))
                    ) {
                        Box(modifier = Modifier.size(dimens.buttonHeight), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { onLogout() },
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE8E0F5))
                    ) {
                        Box(modifier = Modifier.size(dimens.buttonHeight), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Cerrar sesión",
                                tint = Color(0xFF5E4F77),
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                        }
                    }
                }
            }
        }

        // 2. Expediente Activo del Paciente (Claymorphic)
        item {
            ClayElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                containerColor = Color.White,
                elevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEDE7F6),
                                    Color(0xFFF3E5F5)
                                )
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    shadowElevation = 3.dp
                                ) {
                                    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                                        Text(text = "🌱", fontSize = 24.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = selectedPatient?.name ?: "Sin Paciente Vinculado",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Text(
                                        text = "Expediente: ${selectedPatient?.patientCode ?: currentUser?.patientCode ?: "—"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ClayDeepPurple
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF42D088)
                            ) {
                                Text(
                                    text = "Estado: Estable",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Enfoque: TCC para regulación y rutinas",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF4C3E63)
                            )
                            Text(
                                text = "Estrellas: $patientPoints ⭐",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = ClayDeepPurple
                            )
                        }

                        if (linkedPatients.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Pacientes:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(linkedPatients, key = { it.id }) { pat ->
                                        val isSel = pat.id == selectedPatient?.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) ClayDeepPurple else Color.White,
                                            border = BorderStroke(1.dp, if (isSel) ClayDeepPurple else Color(0xFFDED5EB)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { viewModel.selectPatient(pat) }
                                        ) {
                                            Text(
                                                text = pat.name,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else Color(0xFF1E1333),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    // Botón "+" para vincular otro paciente
                                    item {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFEDE7F6),
                                            border = BorderStroke(1.dp, Color(0xFFDED5EB)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .clickable { showLinkPatientDialog = true }
                                                .testTag("doctor_link_patient_chip")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = "Vincular otro paciente",
                                                    tint = ClayDeepPurple,
                                                    modifier = Modifier.size(dimens.iconTiny)
                                                )
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text(
                                                    text = "Vincular",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ClayDeepPurple
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            ClayPuffyButton(
                                onClick = { showLinkPatientDialog = true },
                                shape = RoundedCornerShape(dimens.chipRadius),
                                elevation = 4.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = dimens.buttonHeight)
                                    .testTag("doctor_link_patient_button")
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.iconTiny + 2.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vincular paciente por código", fontSize = dimens.labelSize, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Clinical Metrics (3 KPI Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // KPI 1: Adherencia a rutinas
                ClayCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Adherencia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B5D83))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$routineRate%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = ClayDeepPurple)
                        Text(text = "$completedRoutinesToday / $totalRoutines hoy", fontSize = 10.sp, color = Color(0xFF8B7E9F))
                    }
                }

                // KPI 2: Ánimo promedio
                ClayCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Ánimo 7D", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B5D83))
                        Spacer(modifier = Modifier.height(4.dp))
                        val avgStr = if (avgMood > 0) String.format(Locale.getDefault(), "%.1f", avgMood) else "—"
                        Text(text = "$avgStr / 5", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF00897B))
                        Text(text = "${moods.size} registros", fontSize = 10.sp, color = Color(0xFF8B7E9F))
                    }
                }

                // KPI 3: Tareas médicas
                ClayCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.White,
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Tareas TCC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6B5D83))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$completedTasksCount/${doctorTasks.size}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE91E63))
                        Text(text = "Cumplidas", fontSize = 10.sp, color = Color(0xFF8B7E9F))
                    }
                }
            }
        }

        // 4. Clinical Navigation Tabs (Modern Pill Selector)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFECE6F6),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    listOf("Tareas TCC", "Libreta & Ánimo", "Plan Clínico").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = dimens.buttonHeight)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedTab = index },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color.White else Color.Transparent,
                            shadowElevation = if (isSelected) 3.dp else 0.dp
                        ) {
                            Text(
                                text = title,
                                fontSize = dimens.labelSize,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) ClayDeepPurple else Color(0xFF5D4E76),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5. TAB CONTENT
        when (selectedTab) {
            0 -> {
                // --- TAB 0: TAREAS TERAPÉUTICAS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prescripciones Activas",
                            fontSize = dimens.titleSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )

                        ClayPuffyButton(
                            onClick = {
                                if (selectedPatient == null && linkedPatients.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Vincula un paciente antes de prescribir tareas",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showAssignTaskDialog = true
                                }
                            },
                            shape = RoundedCornerShape(dimens.chipRadius),
                            modifier = Modifier
                                .heightIn(min = dimens.buttonHeight)
                                .testTag("doctor_assign_task_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Prescribir Tarea",
                                fontSize = dimens.labelSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Filter chips: Todas / Pendientes / Cumplidas
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("TODAS" to "Todas (${doctorTasks.size})", "PENDIENTES" to "Pendientes (${doctorTasks.size - completedTasksCount})", "COMPLETADAS" to "Cumplidas ($completedTasksCount)").forEach { (key, label) ->
                            val isSelected = taskStatusFilter == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ClayDeepPurple else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color(0xFFDED5EB)),
                                modifier = Modifier
                                    .heightIn(min = dimens.buttonHeightSmall)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { taskStatusFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = dimens.labelSize,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF5D4E76),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (filteredDoctorTasks.isEmpty()) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📋", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay tareas en esta categoría",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Pulsa 'Prescribir Tarea' para asignar ejercicios terapéuticos adaptados a ${selectedPatient?.name ?: "tu paciente"}.",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6B5D83),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredDoctorTasks, key = { it.id }) { task ->
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEDE7F6)
                                    ) {
                                        Text(
                                            text = task.category,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ClayDeepPurple,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (task.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {
                                        Text(
                                            text = if (task.isCompleted) "Cumplida ✓" else "Pendiente",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = task.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )

                                Text(
                                    text = task.description,
                                    fontSize = 13.sp,
                                    color = Color(0xFF4C3E63),
                                    lineHeight = 18.sp
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Incentivo paciente: +${task.pointsReward} ⭐",
                                        fontSize = dimens.labelSize,
                                        color = ClayDeepPurple,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (!task.isCompleted) {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFFEDE6F8),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .clickable { viewModel.completeTask(task) }
                                                    .heightIn(min = dimens.buttonHeightSmall)
                                            ) {
                                                Text(
                                                    text = "Validar",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ClayDeepPurple,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        // El médico solo puede retirar las tareas que él asignó
                                        if (task.assignedByRole == "MEDICO") {
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteTaskWithPermission(task) { allowed ->
                                                        if (!allowed) {
                                                            Toast.makeText(
                                                                context,
                                                                "No puedes retirar tareas que no asignaste",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.size(dimens.buttonHeightSmall)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Retirar prescripción",
                                                    tint = Color(0xFFB8A9C9),
                                                    modifier = Modifier.size(dimens.iconSmall)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // --- TAB 1: LIBRETA & EVOLUCIÓN PSICOEMOCIONAL ---
                item {
                    Text(
                        text = "Evolución Psicoemocional (7 Días)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    WeeklyMoodChart(moodEntries = moods)
                }

                item {
                    Text(
                        text = "Libreta de Sentimientos del Paciente (TCC)",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                if (sharedJournal.isEmpty()) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = Color.White
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📖", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sin registros compartidos aún",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Cuando el paciente escriba en su libreta y elija compartirla con su equipo de cuidado, aparecerá aquí.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B5D83),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(sharedJournal, key = { it.id }) { journal ->
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = journal.emotionEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${journal.emotion} (Intensidad: ${journal.intensity}/5)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ClayDeepPurple
                                        )
                                    }
                                    Text(
                                        text = journal.dateString,
                                        fontSize = 11.sp,
                                        color = Color(0xFF7A6D90)
                                    )
                                }

                                Text(
                                    text = journal.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )

                                Text(
                                    text = journal.content,
                                    fontSize = 13.sp,
                                    color = Color(0xFF38294F),
                                    lineHeight = 19.sp
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // --- TAB 2: PLAN CLÍNICO & GUÍA PARA LA FAMILIA ---
                item {
                    Text(
                        text = "Plan Clínico de Acompañamiento",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                item {
                    val clinicalSteps = listOf(
                        Triple("1. Activación Matutina", "Rutina de vaso de agua consciente y gratitud para reducir la rumiación matutina.", "🌅"),
                        Triple("2. Regulación de Ansiedad", "En momentos de tensión, activar la respiración 4-7-8 guiada por Serena en la pestaña 'Calma'.", "🧘"),
                        Triple("3. Refuerzo Positivo", "Celebrar cada tarea cumplida otorgando puntos canjeables por experiencias y juguetes antiestrés.", "⭐"),
                        Triple("4. Higiene Circadiana", "Desconexión de pantallas 30 minutos antes de dormir para favorecer descanso reparador.", "🌙")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        clinicalSteps.forEach { (title, desc, icon) ->
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                containerColor = Color.White,
                                elevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(text = icon, fontSize = 26.sp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E1333)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = desc,
                                            fontSize = 12.sp,
                                            color = Color(0xFF5D4E76),
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showAssignTaskDialog) {
        DoctorAssignTaskDialog(
            onDismiss = { showAssignTaskDialog = false },
            onAssign = { title, desc, category, points ->
                viewModel.assignTask(
                    title = title,
                    description = desc,
                    category = category,
                    pointsReward = points,
                    onSuccess = { showAssignTaskDialog = false }
                )
            }
        )
    }

    if (showLinkPatientDialog) {
        LinkPatientDialog(
            onDismiss = { showLinkPatientDialog = false },
            onLink = { code ->
                viewModel.linkPatient(code) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) {
                        showLinkPatientDialog = false
                        linkCodeInput = ""
                    }
                }
            }
        )
    }
}

@Composable
fun DoctorAssignTaskDialog(
    onDismiss: () -> Unit,
    onAssign: (title: String, desc: String, category: String, points: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Terapia") }
    var points by remember { mutableIntStateOf(80) }

    // Plantillas rápidas TCC
    val quickTemplates = listOf(
        Pair("Registro de 3 Pensamientos", "Identifica 3 pensamientos automáticos y redacta una alternativa compasiva."),
        Pair("Caminata sin Teléfono", "Sal 15 min al aire libre prestando atención a los sonidos y colores a tu alrededor."),
        Pair("Respiración 4-7-8 al Acostarse", "Realiza 4 ciclos de respiración consciente antes de cerrar los ojos.")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Prescribir Tarea TCC al Paciente",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E1333)
                )

                Text(
                    text = "Plantillas clínicas rápidas:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayDeepPurple
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    quickTemplates.forEach { (tmplTitle, tmplDesc) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEDE7F6),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    title = tmplTitle
                                    desc = tmplDesc
                                }
                        ) {
                            Text(
                                text = tmplTitle.take(16) + "...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nombre de la indicación o tarea", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Registro de reestructuración cognitiva", color = Color(0xFF8E82A3)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = ClayDeepPurple,
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFAF8FE),
                        unfocusedContainerColor = Color(0xFFFAF8FE)
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Indicaciones clínicas para el paciente", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Describe qué estabas haciendo y cómo te sentiste...", color = Color(0xFF8E82A3)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = ClayDeepPurple,
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFAF8FE),
                        unfocusedContainerColor = Color(0xFFFAF8FE)
                    )
                )

                Text(
                    text = "Puntos de recompensa: $points ⭐",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayDeepPurple
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 80, 100, 150).forEach { pt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { points = pt },
                            shape = RoundedCornerShape(10.dp),
                            color = if (points == pt) ClayDeepPurple else Color(0xFFEDE6F8)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+$pt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (points == pt) Color.White else ClayDeepPurple
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Cancelar", color = Color(0xFF56476F), fontWeight = FontWeight.Bold)
                    }

                    ClayPuffyButton(
                        onClick = {
                            if (title.isNotBlank() && desc.isNotBlank()) {
                                onAssign(title, desc, category, points)
                            }
                        },
                        enabled = title.isNotBlank() && desc.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Prescribir", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
