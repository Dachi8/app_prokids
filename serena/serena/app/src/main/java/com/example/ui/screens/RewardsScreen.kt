package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrescribedTaskEntity
import com.example.data.model.RewardItemEntity
import com.example.ui.components.rewards.BubbleSensoryToyDialog
import com.example.ui.components.rewards.CreativeCanvasDialog
import com.example.ui.components.rewards.EmotionPuzzleGameDialog
import com.example.ui.components.rewards.MemoryCalmGameDialog
import com.example.ui.components.rewards.PlushToyDialog
import com.example.ui.components.rewards.XylophoneToyDialog
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayElevatedCard
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.clayContentWidth
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun RewardsScreen(
    viewModel: WellnessViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = rememberClayDimens()
    val pointsBalance by viewModel.patientPoints.collectAsState()
    val tasks by viewModel.prescribedTasks.collectAsState()
    val rewards by viewModel.rewards.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("Todos") }
    var celebrationMessage by remember { mutableStateOf<String?>(null) }

    // Active interactive toy/game dialog
    var activeGameType by remember { mutableStateOf<String?>(null) }

    // Patient personal task dialog state
    var showAddPersonalTaskDialog by remember { mutableStateOf(false) }

    val pendingTasks = tasks.filter { !it.isCompleted }
    val claimedRewards = rewards.filter { it.isClaimed }

    val filteredRewards = remember(rewards, selectedCategoryFilter) {
        if (selectedCategoryFilter == "Todos") rewards
        else rewards.filter { it.category == selectedCategoryFilter }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF7F4FD)),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clayContentWidth(dimens)
        ) {
        // Claymorphic Points Balance Header Card
        ClayElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimens.screenPadding, vertical = 12.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = Color(0xFFECE4F8),
            elevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Tus Estrellas de Bienestar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4C3D65)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$pointsBalance",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "⭐", fontSize = 30.sp)
                    }
                    Text(
                        text = "¡Completa tareas terapéuticas y canjea juegos!",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF5A4C73)
                    )
                }

                // Clay 3D Trophy Box
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(6.dp, CircleShape, ambientColor = ClayShadowColor)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Trofeo",
                        tint = Color.White,
                        modifier = Modifier.size(38.dp)
                    )
                }
            }
        }

        // Celebration Toast/Banner
        AnimatedVisibility(visible = celebrationMessage != null) {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimens.screenPadding, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = Color(0xFFFDE8F1),
                elevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🎉", fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = celebrationMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF381430),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3 Claymorphic Tabs for Kids
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.White,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Tareas (${pendingTasks.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Canjear Premios", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Mis Juguetes (${claimedRewards.size})", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                // Tab 0: Prescribed Tasks List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Tareas y Metas Diarias",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = dimens.titleSize,
                                    color = Color(0xFF1E1333)
                                )
                                Text(
                                    text = "Metas de tu médico, tu familia y las que tú decidas agregar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = dimens.labelSize,
                                    color = Color(0xFF56476F)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Botón con el símbolo '+' para añadir una meta personal.
                            // El rótulo inferior deja claro dónde está y qué hace.
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .testTag("patient_add_task_button")
                                    .clickable {
                                        showAddPersonalTaskDialog = true
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dimens.fabSize)
                                        .shadow(8.dp, CircleShape, ambientColor = ClayShadowColor)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color(0xFF7B5EF7), ClayDeepPurple)
                                            )
                                        )
                                        .border(1.5.dp, Color(0x70FFFFFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Añadir nueva meta personal",
                                        tint = Color.White,
                                        modifier = Modifier.size(dimens.iconMedium)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Nueva meta",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple
                                )
                            }
                        }
                    }

                    if (tasks.isEmpty()) {
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
                                    Text(text = "✨", fontSize = 34.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No tienes tareas pendientes",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Text(
                                        text = "Puedes pulsar el botón '+' de arriba para añadir tus propias metas.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF56476F),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        items(tasks, key = { it.id }) { task ->
                            ClayPrescribedTaskCard(
                                task = task,
                                onComplete = {
                                    viewModel.completeTask(task)
                                    celebrationMessage = "¡Completaste: ${task.title}! Ganaste +${task.pointsReward} ⭐"
                                },
                                // El paciente solo puede eliminar sus propias metas:
                                // las tareas del médico y de la familia no se pueden borrar.
                                onDelete = if (task.assignedByRole == "PACIENTE") {
                                    {
                                        viewModel.deleteTaskWithPermission(task) { allowed ->
                                            if (!allowed) {
                                                Toast.makeText(
                                                    context,
                                                    "Solo puedes eliminar tus propias metas",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                } else null
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            1 -> {
                // Tab 1: Rewards Catalog to Redeem
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Tienda de Juguetes y Juegos Educativos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "Canjea tus estrellas por juguetes virtuales interactivos y juegos didácticos.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Category Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf(
                                "Todos",
                                "Juguetes Virtuales",
                                "Juegos de Enseñanza",
                                "Experiencias Familiares"
                            )
                            items(categories) { cat ->
                                val isSelected = selectedCategoryFilter == cat
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { selectedCategoryFilter = cat },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) ClayDeepPurple else Color.White,
                                    border = BorderStroke(1.dp, if (isSelected) ClayDeepPurple else Color(0xFFDDD2ED))
                                ) {
                                    Text(
                                        text = cat,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF56476F),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(filteredRewards, key = { it.id }) { reward ->
                        ClayRewardItemCard(
                            reward = reward,
                            userPoints = pointsBalance,
                            onClaim = {
                                viewModel.claimReward(reward) { success ->
                                    if (success) {
                                        celebrationMessage = "¡Felicidades! Canjeaste: ${reward.title} 🎉 ¡Ya puedes jugar!"
                                    } else {
                                        celebrationMessage = "Estrellas insuficientes para este premio."
                                    }
                                }
                            },
                            onPlay = {
                                activeGameType = reward.gameType.ifBlank {
                                    when {
                                        reward.title.contains("Peluche", ignoreCase = true) -> "PLUSH"
                                        reward.title.contains("Xilófono", ignoreCase = true) -> "XYLOPHONE"
                                        reward.title.contains("Burbuja", ignoreCase = true) -> "BUBBLES"
                                        reward.title.contains("Puzzle", ignoreCase = true) || reward.title.contains("Emociones", ignoreCase = true) -> "EMOTION_PUZZLE"
                                        reward.title.contains("Memorama", ignoreCase = true) || reward.title.contains("Mundo Calma", ignoreCase = true) -> "MEMORY"
                                        reward.title.contains("Lienzo", ignoreCase = true) || reward.title.contains("Dibujo", ignoreCase = true) -> "DRAWING"
                                        else -> "PLUSH"
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            2 -> {
                // Tab 2: "Mis Juguetes y Juegos Desbloqueados"
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = dimens.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Mi Ludoteca de Juguetes Desbloqueados",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "¡Toca cualquier juguete o juego para divertirte y aprender en cualquier momento!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )
                    }

                    if (claimedRewards.isEmpty()) {
                        item {
                            ClayCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                containerColor = Color.White
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(28.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = "🧸", fontSize = 44.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "Aún no tienes juguetes canjeados",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Completa tus tareas de la primera pestaña para ganar estrellas y desbloquear tu primer juguete o juego.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF56476F),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    ClayPuffyButton(
                                        onClick = { selectedTabIndex = 0 },
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Ir a ver Tareas ⭐", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    } else {
                        items(claimedRewards, key = { it.id }) { reward ->
                            ClayUnlockedToyCard(
                                reward = reward,
                                onPlay = {
                                    activeGameType = reward.gameType.ifBlank {
                                        when {
                                            reward.title.contains("Peluche", ignoreCase = true) -> "PLUSH"
                                            reward.title.contains("Xilófono", ignoreCase = true) -> "XYLOPHONE"
                                            reward.title.contains("Burbuja", ignoreCase = true) -> "BUBBLES"
                                            reward.title.contains("Puzzle", ignoreCase = true) || reward.title.contains("Emociones", ignoreCase = true) -> "EMOTION_PUZZLE"
                                            reward.title.contains("Memorama", ignoreCase = true) || reward.title.contains("Mundo Calma", ignoreCase = true) -> "MEMORY"
                                            reward.title.contains("Lienzo", ignoreCase = true) || reward.title.contains("Dibujo", ignoreCase = true) -> "DRAWING"
                                            else -> "PLUSH"
                                        }
                                    }
                                }
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
    }

    // Interactive Virtual Toys & Games Dialogs
    when (activeGameType) {
        "PLUSH" -> {
            PlushToyDialog(onDismiss = { activeGameType = null })
        }
        "XYLOPHONE" -> {
            XylophoneToyDialog(onDismiss = { activeGameType = null })
        }
        "BUBBLES" -> {
            BubbleSensoryToyDialog(onDismiss = { activeGameType = null })
        }
        "EMOTION_PUZZLE" -> {
            EmotionPuzzleGameDialog(onDismiss = { activeGameType = null })
        }
        "MEMORY" -> {
            MemoryCalmGameDialog(onDismiss = { activeGameType = null })
        }
        "DRAWING" -> {
            CreativeCanvasDialog(onDismiss = { activeGameType = null })
        }
        "EXPERIENCE" -> {
            CelebrationExperienceDialog(onDismiss = { activeGameType = null })
        }
    }

    if (showAddPersonalTaskDialog) {
        AddPersonalTaskDialog(
            onDismiss = { showAddPersonalTaskDialog = false },
            onAdd = { title, description ->
                viewModel.addPatientPersonalTask(
                    title = title,
                    description = description
                ) {
                    showAddPersonalTaskDialog = false
                    celebrationMessage = "¡Meta añadida! Complétala para ganar tus estrellas ⭐"
                }
            }
        )
    }
}

@Composable
fun ClayPrescribedTaskCard(
    task: PrescribedTaskEntity,
    onComplete: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val dimens = rememberClayDimens()
    val isDoctor = task.assignedByRole == "MEDICO"
    val isFamily = task.assignedByRole == "FAMILIAR"

    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        containerColor = if (task.isCompleted) Color(0xFFF8F6FD) else Color.White,
        elevation = if (task.isCompleted) 2.dp else 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isDoctor -> Color(0xFFECE4F8)
                        isFamily -> Color(0xFFFDE8F1)
                        else -> Color(0xFFE3F2FD)
                    },
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Text(
                        text = when {
                            isDoctor -> "🩺 Tarea de Médico"
                            isFamily -> "👨‍👩‍👧 Tarea Familiar"
                            else -> "🌱 Mi Meta Personal"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = when {
                            isDoctor -> Color(0xFF221142)
                            isFamily -> Color(0xFF381430)
                            else -> Color(0xFF1565C0)
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF2ECFB),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text(
                            text = "+${task.pointsReward} ⭐",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // Botón para eliminar tareas personales (NO permitido en tareas de médico o familiar)
                    if (onDelete != null && !task.isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Eliminar mi meta",
                                tint = Color(0xFFD32F2F),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isCompleted) Color(0xFF86789D) else Color(0xFF1E1333)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.description,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF56476F),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "De: ${task.assignedByName}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7A6C91),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!task.isCompleted) {
                    ClayPuffyButton(
                        onClick = onComplete,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("complete_task_button_${task.id}")
                            .heightIn(min = dimens.buttonHeight)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(dimens.iconSmall)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cumplir", fontSize = dimens.labelSize, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "¡Logro cumplido!",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClayRewardItemCard(
    reward: RewardItemEntity,
    userPoints: Int,
    onClaim: () -> Unit,
    onPlay: () -> Unit
) {
    val canAfford = userPoints >= reward.pointsCost

    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        containerColor = if (reward.isClaimed) Color(0xFFF7F5FC) else Color.White,
        elevation = if (reward.isClaimed) 2.dp else 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = reward.iconEmoji, fontSize = 42.sp)
            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reward.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1333)
                )
                Text(
                    text = reward.description,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF56476F),
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${reward.pointsCost} ⭐",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEDE6F8),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text(
                            text = reward.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF52446A),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (reward.isClaimed) {
                // Button to play right away
                ClayPuffyButton(
                    onClick = onPlay,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "¡Jugar!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            } else {
                ClayPuffyButton(
                    onClick = onClaim,
                    enabled = canAfford,
                    shape = RoundedCornerShape(14.dp),
                    containerColor = if (canAfford) MaterialTheme.colorScheme.primary else Color(0xFFE2DCEB),
                    contentColor = if (canAfford) Color.White else Color(0xFF867B9A)
                ) {
                    Text(
                        text = if (canAfford) "Canjear" else "Faltan ${reward.pointsCost - userPoints}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
fun ClayUnlockedToyCard(
    reward: RewardItemEntity,
    onPlay: () -> Unit
) {
    ClayCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.shadow(4.dp, CircleShape, ambientColor = ClayShadowColor),
                    shape = CircleShape,
                    color = Color(0xFFECE4F8),
                    border = BorderStroke(1.5.dp, Color.White)
                ) {
                    Box(modifier = Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                        Text(text = reward.iconEmoji, fontSize = 30.sp)
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = reward.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                    Text(
                        text = reward.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClayDeepPurple
                    )
                    Text(
                        text = "¡Listo para interactuar!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            ClayPuffyButton(
                onClick = onPlay,
                shape = RoundedCornerShape(14.dp),
                containerColor = ClayDeepPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Abrir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CelebrationExperienceDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        ClayCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(26.dp),
            containerColor = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🍦🎉", fontSize = 48.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "¡Momento Especial en Familia!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1333),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "¡Has ganado una salida especial para disfrutar de un helado, parque o paseo con tu familia por ser tan constante en tu bienestar!",
                    fontSize = 13.sp,
                    color = Color(0xFF56476F),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                ClayPuffyButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("¡Celebrar con mi familia! 💖", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
