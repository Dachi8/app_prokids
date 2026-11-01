package com.example.ui.screens

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun FamilyDashboardScreen(
    viewModel: WellnessViewModel,
    onLogout: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = rememberClayDimens()
    val currentUser by viewModel.currentUser.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    val moods by viewModel.moodEntries.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val sharedJournal by viewModel.sharedJournalEntries.collectAsState()
    val tasks by viewModel.prescribedTasks.collectAsState()
    val patientPoints by viewModel.patientPoints.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val linkedPatients by viewModel.linkedPatients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val todayDateString = viewModel.getTodayDateString()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Tareas Afectivas, 1: Premios & Festejos, 2: Cariño & Recordatorios, 3: Libreta
    var showAssignTaskDialog by remember { mutableStateOf(false) }
    var showLinkPatientDialog by remember { mutableStateOf(false) }
    var notificationFeedback by remember { mutableStateOf<String?>(null) }
    var taskStatusFilter by remember { mutableStateOf("TODAS") } // "TODAS", "PENDIENTES", "COMPLETADAS"

    // Recordatorios periódicos
    var isPeriodicActive by remember { mutableStateOf(viewModel.isPeriodicReminderActive(context)) }
    var selectedIntervalHours by remember { mutableIntStateOf(viewModel.getSavedIntervalHours(context)) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.schedulePeriodicReminders(context, selectedIntervalHours)
            isPeriodicActive = true
            notificationFeedback = "Recordatorios programados cada $selectedIntervalHours horas ❤️"
        } else {
            notificationFeedback = "Permiso de notificaciones denegado"
        }
    }

    val completedRoutinesToday = routines.count { it.isCompletedOn(todayDateString) }
    val totalRoutines = routines.size
    val familyTasks = tasks.filter { it.assignedByRole == "FAMILIAR" }
    val completedFamilyTasks = familyTasks.count { it.isCompleted }

    val filteredFamilyTasks = when (taskStatusFilter) {
        "PENDIENTES" -> familyTasks.filter { !it.isCompleted }
        "COMPLETADAS" -> familyTasks.filter { it.isCompleted }
        else -> familyTasks
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9F6FD))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = dimens.screenPadding),
        verticalArrangement = Arrangement.spacedBy(dimens.gap)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))

            // 1. Family Loving Header
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
                        color = Color(0xFFFDE8F1),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(modifier = Modifier.size(dimens.buttonHeightLarge), contentAlignment = Alignment.Center) {
                            Text(text = "👨‍👩‍👧", fontSize = dimens.emojiSize)
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Familia",
                            fontSize = dimens.titleSize,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "Red de Apoyo y Cariño para ${selectedPatient?.name ?: "tu ser querido"}",
                            fontSize = dimens.labelSize,
                            color = Color(0xFFD81B60),
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
                            .testTag("family_refresh_button"),
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
                            .testTag("family_settings_button"),
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
                                contentDescription = "Cambiar de usuario",
                                tint = Color(0xFF5E4F77),
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                        }
                    }
                }
            }
        }

        // 2. Semáforo Emocional: ¿Cómo está Alex hoy?
        item {
            ClayElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                containerColor = Color.White,
                elevation = 8.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Cómo amanece ${selectedPatient?.name ?: "tu ser querido"} hoy?",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (todayMood != null) Color(0xFFE8F5E9) else Color(0xFFEDE7F6)
                        ) {
                            Text(
                                text = if (todayMood != null) "Registrado ✓" else "Sin registro aún",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (todayMood != null) Color(0xFF2E7D32) else ClayDeepPurple,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (todayMood != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x30FF7796))
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFFFDE8F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = todayMood!!.moodEmoji, fontSize = 32.sp)
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = todayMood!!.moodLabel,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Nivel de energía: ${todayMood!!.energyLevel} / 5 ⚡",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5D4E76)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Consejo de apoyo familiar
                        val patientName = selectedPatient?.name ?: "tu ser querido"
                        val consejo = when (todayMood!!.moodScore) {
                            5, 4 -> "✨ $patientName se siente bien y positivo/a. ¡Es un momento ideal para salir a caminar, jugar juntos o celebrar sus metas!"
                            3 -> "🌱 $patientName tiene un día neutral y tranquilo. Un rato de plática afectiva o merienda en familia le vendrá genial."
                            else -> "❤️ $patientName se siente cansado/a o abrumado/a hoy. Bríndale un abrazo suave, acompáñale sin juzgar y dale espacio para descansar."
                        }

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "💡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = consejo,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE65100),
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    } else {
                        val pName = selectedPatient?.name ?: "Tu paciente"
                        Text(
                            text = "$pName aún no ha registrado su ánimo de hoy. Puedes enviarle un recordatorio cariñoso abajo para motivarlo 🌱",
                            fontSize = 13.sp,
                            color = Color(0xFF6B5D83)
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
                                color = Color(0xFFD81B60)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(linkedPatients, key = { it.id }) { pat ->
                                    val isSel = pat.id == selectedPatient?.id
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) Color(0xFFD81B60) else Color(0xFFFDE8F1),
                                        border = BorderStroke(1.dp, if (isSel) Color(0xFFD81B60) else Color(0xFFDDD2ED)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { viewModel.selectPatient(pat) }
                                    ) {
                                        Text(
                                            text = pat.name,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else Color(0xFF381430),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                // Botón "+" para vincular otro paciente
                                item {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFFFFFF),
                                        border = BorderStroke(1.dp, Color(0xFFDDD2ED)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { showLinkPatientDialog = true }
                                            .testTag("family_link_patient_chip")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Vincular otro paciente",
                                                tint = Color(0xFFD81B60),
                                                modifier = Modifier.size(dimens.iconTiny)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = "Vincular",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFD81B60)
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
                            containerColor = Color(0xFFD81B60),
                            elevation = 4.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = dimens.buttonHeight)
                                .testTag("family_link_patient_button")
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.iconTiny + 2.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Vincular paciente por código",
                                fontSize = dimens.labelSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 3. Navigation Tabs (Pills)
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEDE7F6),
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    listOf("Tareas Afectivas", "Premios & Festejos", "Cariño Directo", "Libreta").forEachIndexed { index, title ->
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
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFFD81B60) else Color(0xFF5D4E76),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. TAB CONTENT
        when (selectedTab) {
            0 -> {
                // --- TAB 0: TAREAS AFECTIVAS ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tareas Asignadas por la Familia",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )

                        ClayPuffyButton(
                            onClick = {
                                if (selectedPatient == null && linkedPatients.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        "Vincula un paciente antes de asignar tareas",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showAssignTaskDialog = true
                                }
                            },
                            shape = RoundedCornerShape(dimens.chipRadius),
                            containerColor = Color(0xFFFF4081),
                            modifier = Modifier
                                .heightIn(min = dimens.buttonHeight)
                                .testTag("family_assign_task_button")
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Asignar con Amor",
                                fontSize = dimens.labelSize,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Filter chips
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "TODAS" to "Todas (${familyTasks.size})",
                            "PENDIENTES" to "Pendientes (${familyTasks.size - completedFamilyTasks})",
                            "COMPLETADAS" to "Cumplidas ($completedFamilyTasks)"
                        ).forEach { (key, label) ->
                            val isSelected = taskStatusFilter == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFD81B60) else Color.White,
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFD81B60) else Color(0xFFDDD2ED)),
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

                if (filteredFamilyTasks.isEmpty()) {
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
                                Text(text = "💌", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No hay tareas en esta categoría",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Asigna una tarea afectiva como merendar juntos, pasear o apagar el móvil 30 min antes de dormir.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B5D83),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(filteredFamilyTasks, key = { it.id }) { task ->
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
                                    Text(
                                        text = task.title,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF1E1333),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (task.isCompleted) Color(0xFFE8F5E9) else Color(0xFFFDE8F1)
                                    ) {
                                        Text(
                                            text = if (task.isCompleted) "Cumplida ✓" else "Pendiente",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) Color(0xFF2E7D32) else Color(0xFFD81B60),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }

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
                                        text = "Premio para ${selectedPatient?.name ?: "tu paciente"}: +${task.pointsReward} ⭐",
                                        fontSize = dimens.labelSize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD81B60),
                                        modifier = Modifier.weight(1f)
                                    )
                                    // La familia solo puede retirar las tareas que ella asignó
                                    if (task.assignedByRole == "FAMILIAR") {
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
                                                contentDescription = "Retirar tarea",
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

            1 -> {
                // --- TAB 1: PREMIOS Y FESTEJOS EN FAMILIA ---
                item {
                    ClayElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color(0xFFFFF0F5),
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 34.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Puntos Acumulados de Alex",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4E76)
                                )
                                Text(
                                    text = "$patientPoints Estrellas",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD81B60)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Experiencias Familiares para Canjear",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                val familyExperiences = listOf(
                    Triple("Tarde de Helado y Parque", "Salida para festejar una semana de hábitos cumplidos.", 100 to "🍦"),
                    Triple("Noche de Películas con Palomitas", "Elegir juntos la película y disfrutar sin prisas.", 80 to "🎬"),
                    Triple("Taller de Dibujo y Mandalas", "Una tarde creativa dibujando juntos en el rincón de calma.", 60 to "🎨"),
                    Triple("Cena Especial de Fin de Semana", "Preparar juntos la comida favorita de Alex.", 120 to "🍕")
                )

                items(familyExperiences) { (title, desc, costAndIcon) ->
                    val (cost, emoji) = costAndIcon
                    val canAfford = patientPoints >= cost

                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        containerColor = Color.White,
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 32.sp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = desc,
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B5D83)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Costo: $cost ⭐",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD81B60)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (canAfford) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = canAfford) {
                                        notificationFeedback = "¡Experiencia '$title' concedida con éxito! A disfrutar juntos 🎉"
                                    }
                            ) {
                                Text(
                                    text = if (canAfford) "Conceder" else "Faltan pts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (canAfford) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // --- TAB 2: CARIÑO DIRECTO & RECORDATORIOS ---
                item {
                    Text(
                        text = "Enviar Mensaje de Cariño al Instante",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                item {
                    val messages = listOf(
                        "❤️ ¡Te quiero muchísimo! Eres muy valiente y hoy lo estás haciendo genial.",
                        "🌱 ¡Estoy muy orgullosa/o de ti! Recuerda tomar un vasito de agua fresca.",
                        "🧘 Tómate una pausita para respirar profundo y relajarte un ratito."
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        messages.forEach { msg ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFEDE7F6)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        viewModel.sendWellnessNotification(context)
                                        notificationFeedback = "¡Mensaje de cariño enviado al teléfono de Alex! ❤️"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = msg,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1E1333),
                                        modifier = Modifier.weight(1f),
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color(0xFFD81B60),
                                        modifier = Modifier.size(dimens.iconSmall)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Programador Automático de Recordatorios",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                }

                item {
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = Color.White,
                        elevation = 4.dp
                    ) {
                        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Recordatorios Periódicos",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Text(
                                        text = "Enviar pausas de bienestar a Alex automáticamente",
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B5D83)
                                    )
                                }

                                Switch(
                                    checked = isPeriodicActive,
                                    onCheckedChange = { enable ->
                                        if (enable) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                notificationLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            } else {
                                                viewModel.schedulePeriodicReminders(context, selectedIntervalHours)
                                                isPeriodicActive = true
                                                notificationFeedback = "Recordatorios programados cada $selectedIntervalHours horas ❤️"
                                            }
                                        } else {
                                            viewModel.cancelPeriodicReminders(context)
                                            isPeriodicActive = false
                                            notificationFeedback = "Recordatorios periódicos desactivados"
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD81B60), checkedTrackColor = Color(0xFFFCE4EC))
                                )
                            }

                            if (isPeriodicActive) {
                                Text(text = "Frecuencia de envío:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD81B60))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(2, 4, 6, 8).forEach { hrs ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    selectedIntervalHours = hrs
                                                    viewModel.schedulePeriodicReminders(context, hrs)
                                                    notificationFeedback = "Actualizado a cada $hrs horas"
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (selectedIntervalHours == hrs) Color(0xFFD81B60) else Color(0xFFFDE8F1)
                                        ) {
                                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = "Cada ${hrs}h",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selectedIntervalHours == hrs) Color.White else Color(0xFFD81B60)
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

            3 -> {
                // --- TAB 3: LIBRETA COMPARTIDA ---
                item {
                    Text(
                        text = "Reflexiones que Alex Quiso Compartir",
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
                                Text(text = "🌱", fontSize = 34.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Sin notas compartidas por el momento",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Alex puede decidir libremente qué pensamientos compartir con la familia en su libreta.",
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
                            shape = RoundedCornerShape(20.dp),
                            containerColor = Color.White,
                            elevation = 3.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${journal.emotionEmoji} Sentimiento: ${journal.emotion}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD81B60)
                                    )
                                    Text(text = journal.dateString, fontSize = 11.sp, color = Color(0xFF7A6D90))
                                }
                                Text(text = journal.title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E1333))
                                Text(text = journal.content, fontSize = 13.sp, color = Color(0xFF38294F), lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }
        }

        // Notification feedback message
        if (notificationFeedback != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9),
                    border = BorderStroke(1.dp, Color(0xFFC8E6C9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "✨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = notificationFeedback!!,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { notificationFeedback = null }) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
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
        FamilyAssignTaskDialog(
            onDismiss = { showAssignTaskDialog = false },
            onAssign = { title, desc, points ->
                viewModel.assignTask(
                    title = title,
                    description = desc,
                    category = "Familiar",
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
                    }
                }
            }
        )
    }
}

@Composable
fun FamilyAssignTaskDialog(
    onDismiss: () -> Unit,
    onAssign: (title: String, desc: String, points: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var points by remember { mutableIntStateOf(50) }

    val familyPresets = listOf(
        Pair("Merienda Saludable Juntos", "Sentarnos 15 min a comer una fruta rica y platicar de cosas bonitas."),
        Pair("Paseo al Aire Libre", "Salir a caminar al parque o jardín 20 min en familia sin teléfonos."),
        Pair("Ordenar el Rincón de Calma", "Organizar juntos el espacio de juego o estudio con música suave.")
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
                    text = "Asignar Tarea con Cariño 💕",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF1E1333)
                )

                Text(
                    text = "Ideas familiares rápidas:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD81B60)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    familyPresets.forEach { (tmplTitle, tmplDesc) ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFDE8F1),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    title = tmplTitle
                                    desc = tmplDesc
                                }
                        ) {
                            Text(
                                text = tmplTitle.take(15) + "...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD81B60),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de la tarea de apoyo", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Tomar una merienda juntos", color = Color(0xFF8E82A3)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = Color(0xFFD81B60),
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFFF9FB),
                        unfocusedContainerColor = Color(0xFFFFF9FB)
                    )
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Mensaje cariñoso con la indicación", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Hagamos una pausa para platicar un ratito...", color = Color(0xFF8E82A3)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF1E1333),
                        unfocusedTextColor = Color(0xFF1E1333),
                        focusedBorderColor = Color(0xFFD81B60),
                        unfocusedBorderColor = Color(0xFFDDD2ED),
                        focusedContainerColor = Color(0xFFFFF9FB),
                        unfocusedContainerColor = Color(0xFFFFF9FB)
                    )
                )

                Text(
                    text = "Estrellas para el paciente: $points ⭐",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD81B60)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(40, 50, 70, 100).forEach { pt ->
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { points = pt },
                            shape = RoundedCornerShape(10.dp),
                            color = if (points == pt) Color(0xFFD81B60) else Color(0xFFFDE8F1)
                        ) {
                            Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+$pt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (points == pt) Color.White else Color(0xFFD81B60)
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
                                onAssign(title, desc, points)
                            }
                        },
                        enabled = title.isNotBlank() && desc.isNotBlank(),
                        containerColor = Color(0xFFD81B60),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text("Asignar con Amor", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
