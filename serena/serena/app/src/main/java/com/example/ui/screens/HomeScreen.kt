package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.ClaySquircleCyan
import com.example.ui.theme.ClaySquircleGreen
import com.example.ui.theme.ClaySquirclePink
import com.example.ui.theme.ClaySquirclePurple
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: WellnessViewModel,
    onNavigateToChat: () -> Unit,
    onNavigateToMood: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToCalm: () -> Unit,
    onNavigateToJournal: () -> Unit,
    onNavigateToRewards: () -> Unit,
    onSwitchProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = rememberClayDimens()
    val currentUser by viewModel.currentUser.collectAsState()
    val greeting by viewModel.greetingMessage.collectAsState()
    val affirmation by viewModel.dailyAffirmation.collectAsState()
    val todayMood by viewModel.todayMood.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val pointsBalance by viewModel.patientPoints.collectAsState()
    val tasks by viewModel.prescribedTasks.collectAsState()
    val todayDateString = viewModel.getTodayDateString()

    var notificationFeedback by remember { mutableStateOf<String?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendWellnessNotification(context)
            notificationFeedback = "¡Recordatorio de bienestar activado con éxito! 🌱"
        } else {
            notificationFeedback = "Permiso de notificaciones no habilitado."
        }
    }

    val completedRoutinesToday = routines.filter { it.isCompletedOn(todayDateString) }
    val totalRoutines = routines.size
    val progress = if (totalRoutines > 0) completedRoutinesToday.size.toFloat() / totalRoutines else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress_anim")
    val pendingTasksCount = tasks.count { !it.isCompleted }

    val dateFormat = SimpleDateFormat("EEEE, d 'de' MMMM", Locale.forLanguageTag("es-ES"))
    val friendlyDate = dateFormat.format(Date()).replaceFirstChar { it.uppercase() }

    // Calculate level info for the wellness card
    val currentLevel = when {
        pointsBalance >= 500 -> "Árbol"
        pointsBalance >= 200 -> "Flor"
        pointsBalance >= 80 -> "Brote"
        else -> "Semilla"
    }
    val currentLevelEmoji = when (currentLevel) {
        "Árbol" -> "🌳"
        "Flor" -> "🌸"
        "Brote" -> "🌿"
        else -> "🌱"
    }
    val nextLevelPoints = when (currentLevel) {
        "Árbol" -> 999
        "Flor" -> 500
        "Brote" -> 200
        else -> 80
    }
    val levelProgress = if (nextLevelPoints > 0) (pointsBalance.toFloat() / nextLevelPoints).coerceIn(0f, 1f) else 1f
    val animatedLevelProgress by animateFloatAsState(
        targetValue = levelProgress,
        animationSpec = tween(durationMillis = 800),
        label = "level_progress_anim"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF6F3FC))
            .padding(horizontal = dimens.screenPadding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(dimens.gap)
    ) {
        // ─── Top Bar: Greeting + Avatar ───
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    val firstName = currentUser?.name?.split(" ")?.firstOrNull()?.let { ", $it" } ?: ""
                    Text(
                        text = "$greeting$firstName",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333),
                        maxLines = 2,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = friendlyDate,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF56476F)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier
                            .shadow(4.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { onSwitchProfile() },
                        color = Color.White,
                        border = BorderStroke(1.5.dp, Color(0xFFE8E0F5))
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración",
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Surface(
                        modifier = Modifier
                            .shadow(6.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { onNavigateToChat() },
                        color = Color.White,
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Box(
                            modifier = Modifier.size(46.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_serena_mascot),
                                contentDescription = "Mascota Serena",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // ─── NEW: Wellness Passport — Glass-style Organic Card ───
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("serena_hero_card"),
                shape = RoundedCornerShape(28.dp),
                containerColor = Color.Transparent,
                elevation = 0.dp,
                borderStroke = null
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(14.dp, RoundedCornerShape(28.dp), ambientColor = Color(0x40654BE2))
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF7B5EF7),
                                    Color(0xFF654BE2),
                                    Color(0xFF5038C9)
                                )
                            )
                        )
                ) {
                    // Decorative floating circles (organic feel)
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .offset(x = (-20).dp, y = (-20).dp)
                            .clip(CircleShape)
                            .background(Color(0x15FFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = 15.dp, y = (-10).dp)
                            .clip(CircleShape)
                            .background(Color(0x10FFFFFF))
                    )
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 30.dp, y = 20.dp)
                            .clip(CircleShape)
                            .background(Color(0x12FFFFFF))
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp)
                    ) {
                        // Top row: Label + Level badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PASAPORTE DE BIENESTAR",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xB0FFFFFF),
                                letterSpacing = 1.5.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0x28FFFFFF),
                                border = BorderStroke(1.dp, Color(0x30FFFFFF))
                            ) {
                                Text(
                                    text = "$currentLevelEmoji $currentLevel",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Center: Points display with decorative star
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Glowing star orb
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .shadow(10.dp, CircleShape, ambientColor = Color(0x60FFD700))
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFFFFE082),
                                                Color(0xFFFFC107),
                                                Color(0xFFFF9800)
                                            )
                                        )
                                    )
                                    .border(2.dp, Color(0x50FFFFFF), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$pointsBalance",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    lineHeight = 38.sp
                                )
                                Text(
                                    text = "puntos de bienestar",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xB0FFFFFF)
                                )
                            }

                            // Right: Chat button
                            ClayPuffyButton(
                                onClick = onNavigateToChat,
                                shape = RoundedCornerShape(16.dp),
                                containerColor = Color.White,
                                contentColor = ClayDeepPurple,
                                elevation = 4.dp,
                                modifier = Modifier.height(dimens.buttonHeightSmall)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.iconTiny + 2.dp),
                                    tint = ClayDeepPurple
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hablar",
                                    fontSize = dimens.labelSize,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Progress bar towards next level
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentUser?.name ?: "Paciente",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "$pointsBalance / $nextLevelPoints pts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xB0FFFFFF)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom level progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x30FFFFFF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(animatedLevelProgress)
                                        .height(8.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color(0xFFFFE082),
                                                    Color(0xFFFFC107),
                                                    Color(0xFFFF9800)
                                                )
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── Quick Actions Grid (2x2 grid layout for better space) ───
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Respirar
                    QuickActionTile(
                        icon = Icons.Default.Air,
                        label = "Respirar",
                        subtitle = "Ejercicios de calma",
                        backgroundColor = ClaySquircleGreen,
                        onClick = onNavigateToCalm,
                        modifier = Modifier.weight(1f)
                    )
                    // Libreta
                    QuickActionTile(
                        icon = Icons.Default.Book,
                        label = "Libreta",
                        subtitle = "Mis reflexiones",
                        backgroundColor = ClaySquirclePink,
                        onClick = onNavigateToJournal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Premios
                    QuickActionTile(
                        icon = Icons.Default.EmojiEvents,
                        label = "Premios",
                        subtitle = "Juegos y juguetes",
                        backgroundColor = ClaySquirclePurple,
                        onClick = onNavigateToRewards,
                        modifier = Modifier.weight(1f)
                    )
                    // Serena AI
                    QuickActionTile(
                        icon = Icons.Default.SelfImprovement,
                        label = "Serena",
                        subtitle = "Tu compañera IA",
                        backgroundColor = ClaySquircleCyan,
                        onClick = onNavigateToChat,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ─── Section Title ───
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mi Bienestar Hoy",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1333)
                )
                Text(
                    text = "Ver todo",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = ClayDeepPurple,
                    modifier = Modifier.clickable { onNavigateToRoutines() }
                )
            }
        }

        // ─── Card 1: Today's Mood ───
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToMood() }
                    .testTag("mood_status_card"),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x30FF7796))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFDE8F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = todayMood?.moodEmoji ?: "💭",
                            fontSize = 26.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (todayMood != null) "Ánimo: ${todayMood!!.moodLabel}" else "¿Cómo te sientes hoy?",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (todayMood != null) todayMood!!.emotionTags.ifEmpty { "Registro completado" } else "Registra tu energía (+15 pts)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (todayMood != null) Color(0xFFECE4F8) else Color(0xFFFDE8F1),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text(
                            text = if (todayMood != null) "+15 ⭐" else "Registrar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (todayMood != null) ClayDeepPurple else Color(0xFFD62B57),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ─── Card 2: Tasks & Rewards ───
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToRewards() }
                    .testTag("home_rewards_card"),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), ambientColor = Color(0x30866EFF))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFEFE9FA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = ClayDeepPurple,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Juguetes y Juegos 🧸",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1E1333)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (pendingTasksCount > 0) "$pendingTasksCount tareas pendientes • ¡Gana estrellas!" else "¡Canjea estrellas por premios!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF56476F)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECE4F8),
                        border = BorderStroke(1.dp, Color.White)
                    ) {
                        Text(
                            text = "Premios",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ClayDeepPurple,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // ─── Card 2.5: Mis Tareas (con botón '+' para añadir las propias) ───
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_tasks_card"),
                shape = RoundedCornerShape(dimens.cardRadius),
                containerColor = Color.White,
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mis Tareas y Metas",
                                fontSize = dimens.bodySize,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                            Text(
                                text = if (pendingTasksCount > 0) {
                                    "$pendingTasksCount pendientes • de tu médico, tu familia y tú"
                                } else {
                                    "Todo al día. ¡Añade tu propia meta con el botón +!"
                                },
                                fontSize = dimens.labelSize,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF56476F)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        // Botón '+' para añadir una meta personal.
                        // El rótulo inferior deja claro qué hace y dónde está.
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .testTag("patient_add_task_button")
                                .clickable { showAddTaskDialog = true }
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

                    val pendingTasks = tasks.filter { !it.isCompleted }.take(3)
                    if (pendingTasks.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        pendingTasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.completeTask(task) }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Cumplir ${task.title}",
                                    tint = Color(0xFFB5A9C9),
                                    modifier = Modifier.size(dimens.iconSmall)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = task.title,
                                    fontSize = dimens.labelSize,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E1333),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "+${task.pointsReward} ⭐",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── Card 3: Routines Progress ───
        item {
            ClayCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("routines_progress_card"),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rutinas de Bienestar",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )
                        }
                        Text(
                            text = "${completedRoutinesToday.size} de $totalRoutines",
                            fontSize = 12.sp,
                            color = ClayDeepPurple,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = ClayDeepPurple,
                        trackColor = Color(0xFFEFE8FB)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    routines.take(3).forEach { routine ->
                        val isDone = routine.isCompletedOn(todayDateString)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.toggleRoutine(routine) }
                                .padding(vertical = 7.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = if (isDone) "Completado" else "No completado",
                                tint = if (isDone) ClayDeepPurple else Color(0xFFB5A9C9),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = routine.title,
                                fontSize = 14.sp,
                                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Medium,
                                color = if (isDone) Color(0xFF85779D) else Color(0xFF1E1333)
                            )
                        }
                    }
                }
            }
        }

        // ─── Card 4: Daily Affirmation ───
        item {
            ClayCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                containerColor = Color(0xFFFBF8FE),
                elevation = 4.dp,
                borderStroke = BorderStroke(1.5.dp, Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = ClaySquirclePink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pensamiento del Día",
                            fontSize = 12.sp,
                            color = ClaySquirclePink,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = affirmation,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF28193E),
                            lineHeight = 21.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Diálogo para añadir una meta personal
    if (showAddTaskDialog) {
        AddPersonalTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAdd = { title, description ->
                viewModel.addPatientPersonalTask(
                    title = title,
                    description = description
                ) {
                    showAddTaskDialog = false
                    Toast.makeText(
                        context,
                        "¡Meta añadida! Complétala para ganar estrellas ⭐",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }
}

// ─── NEW: Quick Action Tile composable (2x2 grid item) ───
@Composable
fun QuickActionTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = rememberClayDimens()
    ClayCard(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        containerColor = Color.White,
        elevation = 5.dp,
        borderStroke = BorderStroke(1.dp, Color(0xFFF0EBF7))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(dimens.buttonHeightLarge)
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(15.dp),
                        ambientColor = backgroundColor.copy(alpha = 0.5f),
                        spotColor = backgroundColor.copy(alpha = 0.5f)
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = 0.9f),
                                backgroundColor
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0x60FFFFFF), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(dimens.iconMedium)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label,
                fontSize = dimens.bodySize,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E1333)
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B7E9F)
            )
        }
    }
}
