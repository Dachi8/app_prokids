package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.theme.clayContentWidth
import com.example.ui.theme.rememberClayDimens
import com.example.ui.viewmodel.WellnessViewModel

data class ContactoEmergencia(val nombre: String, val telefono: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: WellnessViewModel,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dimens = rememberClayDimens()
    val currentUser by viewModel.currentUser.collectAsState()
    val patientPoints by viewModel.patientPoints.collectAsState()
    val linkedPatients by viewModel.linkedPatients.collectAsState()
    val selectedPatient by viewModel.selectedPatient.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val serverOnline by viewModel.serverOnline.collectAsState()

    var serverUrlInput by remember { mutableStateOf(viewModel.urlServidorActual()) }

    val backgroundColor = Color(0xFFF6F3FC)

    // Estados de configuración
    var selectedTheme by remember { mutableStateOf("Automático") }
    var selectedLanguage by remember { mutableStateOf("Español") }

    // Diálogos de vinculación y desactivación
    var mostrandoDialogoVincular by remember { mutableStateOf(false) }
    var codigoVincularInput by remember { mutableStateOf("") }
    var mostrandoDialogoDesactivar by remember { mutableStateOf(false) }

    // PIN de seguridad (vacío hasta que el usuario lo configure)
    var pinGuardado by remember { mutableStateOf("") }
    var pinTemporal by remember { mutableStateOf("") }
    var estaConfigurandoPin by remember { mutableStateOf(false) }

    // Números de emergencia personalizados
    val contactosEmergencia = remember {
        mutableStateListOf(
            ContactoEmergencia("Línea Nacional de Apoyo Emocional", "106"),
            ContactoEmergencia("Emergencias Médicas", "911")
        )
    }
    var nuevoNombreContacto by remember { mutableStateOf("") }
    var nuevoTelefonoContacto by remember { mutableStateOf("") }
    var mostrandoAgregarContacto by remember { mutableStateOf(false) }

    val esPaciente = currentUser?.role == "PACIENTE"
    val esMedicoOrFamiliar = currentUser?.role == "MEDICO" || currentUser?.role == "FAMILIAR"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configuración",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = dimens.titleSize,
                        color = Color(0xFF1E1333)
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(dimens.buttonHeightSmall)
                            .shadow(2.dp, CircleShape, ambientColor = ClayShadowColor)
                            .clip(CircleShape)
                            .clickable { onNavigateBack() },
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Regresar",
                                tint = ClayDeepPurple,
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .clayContentWidth(dimens)
                    .padding(horizontal = dimens.screenPadding),
                contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(dimens.gap)
            ) {
                // 1. INFORMACIÓN COMPLETA DEL PERFIL
                item {
                    SectionTitle("Perfil y Cuenta")
                    ClayCard(
                        shape = RoundedCornerShape(26.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar grande con relieve
                            Box(
                                modifier = Modifier
                                    .size(dimens.iconLarge * 2.5f)
                                    .shadow(6.dp, CircleShape, ambientColor = ClayShadowColor)
                                    .clip(CircleShape)
                                    .background(Color(0xFFECE4F8))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser?.avatarEmoji ?: "🌱",
                                    fontSize = dimens.heroSize
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = currentUser?.name ?: "Usuario",
                                fontSize = dimens.titleSize,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1E1333)
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFECE4F8),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "ROL: ${currentUser?.role ?: "PACIENTE"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ClayDeepPurple,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))
                            HorizontalDivider(color = Color(0xFFF1EDF8))
                            Spacer(modifier = Modifier.height(14.dp))

                            // Datos detallados
                            InfoRow(label = "Correo Electrónico", valor = currentUser?.email ?: "No configurado")
                            InfoRow(label = "Puntos de Bienestar", valor = "$patientPoints ⭐")

                            // CÓDIGO DE VINCULACIÓN ÚNICO — visible para todos los perfiles
                            Spacer(modifier = Modifier.height(10.dp))
                            LinkingCodeCard(
                                code = currentUser?.patientCode ?: "SERENA-0000",
                                isPatient = esPaciente,
                                onCopy = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                        as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText(
                                        "Código de vinculación Serena",
                                        currentUser?.patientCode ?: ""
                                    )
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "¡Código copiado al portapapeles!", Toast.LENGTH_SHORT).show()
                                },
                                onRegenerate = {
                                    viewModel.regenerateLinkingCode { success, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            InfoRow(label = "Estado de Cuenta", valor = "Activo")
                        }
                    }
                }

                // SECCIÓN MULTI-PACIENTE PARA MÉDICO Y FAMILIAR
                if (esMedicoOrFamiliar) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SectionTitle("Pacientes Vinculados (${linkedPatients.size})")
                            ClayPuffyButton(
                                onClick = { mostrandoDialogoVincular = true },
                                shape = RoundedCornerShape(dimens.chipRadius),
                                elevation = 4.dp,
                                modifier = Modifier
                                    .heightIn(min = dimens.buttonHeightSmall)
                                    .testTag("link_patient_button")
                            ) {
                                Icon(
                                    Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(dimens.iconTiny + 2.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Vincular", fontSize = dimens.labelSize, fontWeight = FontWeight.Bold)
                            }
                        }

                        ClayCard(
                            shape = RoundedCornerShape(24.dp),
                            containerColor = Color.White,
                            elevation = 6.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                if (linkedPatients.isEmpty()) {
                                    Text(
                                        text = "Aún no tienes pacientes vinculados. Pulsa 'Vincular' e introduce el código que aparece en la configuración de tu paciente.",
                                        fontSize = dimens.labelSize,
                                        color = Color(0xFF7E6F94),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                } else {
                                    linkedPatients.forEachIndexed { index, p ->
                                        val isSelected = selectedPatient?.id == p.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    viewModel.selectPatient(p)
                                                    Toast.makeText(context, "Supervisando a: ${p.name}", Toast.LENGTH_SHORT).show()
                                                }
                                                .background(if (isSelected) Color(0xFFF3EDFA) else Color.Transparent)
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = p.avatarEmoji, fontSize = dimens.emojiSize)
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = p.name,
                                                    fontSize = dimens.bodySize,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E1333)
                                                )
                                                Text(
                                                    text = "Código: ${p.patientCode} • ${p.pointsBalance} ⭐",
                                                    fontSize = dimens.labelSize,
                                                    color = Color(0xFF6B5C85)
                                                )
                                            }
                                            if (isSelected) {
                                                Surface(shape = RoundedCornerShape(8.dp), color = ClayDeepPurple) {
                                                    Text(
                                                        "Activo",
                                                        fontSize = 11.sp,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                            } else {
                                                TextButton(
                                                    onClick = {
                                                        viewModel.selectPatient(p)
                                                        Toast.makeText(context, "Supervisando a: ${p.name}", Toast.LENGTH_SHORT).show()
                                                    },
                                                    modifier = Modifier.heightIn(min = dimens.buttonHeightSmall)
                                                ) {
                                                    Text(
                                                        "Seleccionar",
                                                        fontSize = dimens.labelSize,
                                                        color = ClayDeepPurple,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.unlinkPatient(p) { _, message ->
                                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier.size(dimens.buttonHeightSmall)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Desvincular paciente",
                                                    tint = Color(0xFFD62B57),
                                                    modifier = Modifier.size(dimens.iconSmall)
                                                )
                                            }
                                        }
                                        if (index < linkedPatients.size - 1) {
                                            HorizontalDivider(color = Color(0xFFF4EFFB))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECCIÓN DE SINCRONIZACIÓN CON EL SERVIDOR PROKIDS
                item {
                    SectionTitle("Sincronización con ProKids")
                    ClayCard(
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Estado del servidor",
                                        fontSize = dimens.bodySize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Text(
                                        text = when {
                                            isSyncing -> "Sincronizando…"
                                            serverOnline -> "Conectado a ProKids"
                                            else -> "Sin conexión (la app funciona igual en local)"
                                        },
                                        fontSize = dimens.labelSize,
                                        color = if (serverOnline) Color(0xFF2E7D32) else Color(0xFF8B7E9F)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (serverOnline) Color(0xFFE8F5E9) else Color(0xFFF1EBFB)
                                ) {
                                    Text(
                                        text = if (serverOnline) "Conectado ✓" else "Local",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (serverOnline) Color(0xFF2E7D32) else ClayDeepPurple,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = serverUrlInput,
                                onValueChange = { serverUrlInput = it },
                                label = { Text("Dirección del servidor") },
                                placeholder = { Text("http://192.168.1.50:4000/") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF1E1333),
                                    unfocusedTextColor = Color(0xFF1E1333),
                                    focusedBorderColor = ClayDeepPurple,
                                    unfocusedBorderColor = Color(0xFFDDD2ED),
                                    focusedContainerColor = Color(0xFFFAF8FE),
                                    unfocusedContainerColor = Color(0xFFFAF8FE)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.comprobarServidor { online ->
                                            Toast.makeText(
                                                context,
                                                if (online) "¡Servidor ProKids accesible!"
                                                else "No se pudo conectar con el servidor",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                ) {
                                    Text("Probar", color = ClayDeepPurple, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                ClayPuffyButton(
                                    onClick = {
                                        viewModel.configurarServidor(serverUrlInput)
                                        Toast.makeText(
                                            context,
                                            "Dirección del servidor guardada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                ) {
                                    Text("Guardar")
                                }
                            }

                            Text(
                                text = "💡 En el emulador usa http://10.0.2.2:4000/. En un teléfono físico, la IP local de tu computadora (por ejemplo http://192.168.1.50:4000/).",
                                fontSize = 11.sp,
                                color = Color(0xFF7E6F94),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // 2. TEMAS PARA LA APP
                item {
                    SectionTitle("Tema Visual")
                    ClayCard(
                        shape = RoundedCornerShape(22.dp),
                        containerColor = Color.White,
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val temas = listOf("Claro", "Oscuro", "Automático")
                            temas.forEach { tema ->
                                val seleccionado = selectedTheme == tema
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = dimens.buttonHeight)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedTheme = tema
                                            Toast.makeText(context, "Tema cambiado a: $tema", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (seleccionado) ClayDeepPurple else Color(0xFFF8F6FC),
                                    border = if (seleccionado) null else BorderStroke(1.dp, Color(0xFFEFE8F8))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tema,
                                            fontSize = dimens.labelSize,
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                            color = if (seleccionado) Color.White else Color(0xFF56476F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. PIN DE SEGURIDAD (Vacío hasta que se configure)
                item {
                    SectionTitle("PIN de Seguridad")
                    ClayCard(
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
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(dimens.buttonHeight)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF1EBFB)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = ClayDeepPurple,
                                        modifier = Modifier.size(dimens.iconSmall)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (pinGuardado.isEmpty()) "Sin PIN configurado" else "PIN activo (4 dígitos)",
                                        fontSize = dimens.bodySize,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E1333)
                                    )
                                    Text(
                                        text = if (pinGuardado.isEmpty()) "Protege el acceso a tus notas y tareas" else "Acceso protegido con clave personal",
                                        fontSize = dimens.labelSize,
                                        color = Color(0xFF7E6F94)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (estaConfigurandoPin) {
                                // Campo para ingresar los 4 dígitos
                                OutlinedTextField(
                                    value = pinTemporal,
                                    onValueChange = { if (it.length <= 4 && it.all { char -> char.isDigit() }) pinTemporal = it },
                                    label = { Text("Ingresa tu PIN de 4 dígitos") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ClayDeepPurple,
                                        focusedContainerColor = Color(0xFFFAF8FE)
                                    )
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            estaConfigurandoPin = false
                                            pinTemporal = ""
                                        },
                                        modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                    ) {
                                        Text("Cancelar", color = Color(0xFF6B5C85))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClayPuffyButton(
                                        onClick = {
                                            if (pinTemporal.length == 4) {
                                                pinGuardado = pinTemporal
                                                estaConfigurandoPin = false
                                                pinTemporal = ""
                                                Toast.makeText(context, "¡PIN de seguridad guardado con éxito!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        enabled = pinTemporal.length == 4,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                    ) {
                                        Text("Guardar PIN")
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (i in 0 until 4) {
                                            val activo = i < pinGuardado.length
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(if (activo) ClayDeepPurple else Color(0xFFDED6EC))
                                            )
                                        }
                                    }

                                    TextButton(
                                        onClick = { estaConfigurandoPin = true },
                                        modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                    ) {
                                        Text(
                                            text = if (pinGuardado.isEmpty()) "Crear PIN" else "Cambiar PIN",
                                            fontWeight = FontWeight.Bold,
                                            color = ClayDeepPurple
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. IDIOMA
                item {
                    SectionTitle("Idioma")
                    ClayCard(
                        shape = RoundedCornerShape(22.dp),
                        containerColor = Color.White,
                        elevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val idiomas = listOf("Español", "English", "Português")
                            idiomas.forEach { idioma ->
                                val seleccionado = selectedLanguage == idioma
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = dimens.buttonHeight)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedLanguage = idioma
                                            Toast.makeText(context, "Idioma seleccionado: $idioma", Toast.LENGTH_SHORT).show()
                                        },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (seleccionado) ClayDeepPurple else Color(0xFFF8F6FC),
                                    border = if (seleccionado) null else BorderStroke(1.dp, Color(0xFFEFE8F8))
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = idioma,
                                            fontSize = dimens.labelSize,
                                            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium,
                                            color = if (seleccionado) Color.White else Color(0xFF56476F)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. TELÉFONOS DE EMERGENCIA PERSONALIZABLES
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle("Teléfonos de Emergencia")
                        TextButton(
                            onClick = { mostrandoAgregarContacto = true },
                            modifier = Modifier.heightIn(min = dimens.buttonHeightSmall)
                        ) {
                            Text("+ Agregar", color = ClayDeepPurple, fontWeight = FontWeight.Bold)
                        }
                    }

                    ClayCard(
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            if (contactosEmergencia.isEmpty()) {
                                Text(
                                    text = "No tienes números de emergencia configurados.",
                                    fontSize = dimens.labelSize,
                                    color = Color(0xFF7E6F94),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                contactosEmergencia.forEachIndexed { index, contacto ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(dimens.buttonHeightSmall)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFEE8EE)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = Color(0xFFD62B57),
                                                modifier = Modifier.size(dimens.iconSmall)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contacto.nombre,
                                                fontSize = dimens.bodySize,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E1333)
                                            )
                                            Text(
                                                text = contacto.telefono,
                                                fontSize = dimens.labelSize,
                                                color = ClayDeepPurple,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(
                                            onClick = { contactosEmergencia.removeAt(index) },
                                            modifier = Modifier.size(dimens.buttonHeightSmall)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                tint = Color(0xFFB8A9C9),
                                                modifier = Modifier.size(dimens.iconSmall)
                                            )
                                        }
                                    }
                                    if (index < contactosEmergencia.size - 1) {
                                        HorizontalDivider(color = Color(0xFFF4EFFB))
                                    }
                                }
                            }

                            // Formulario para agregar nuevo contacto
                            if (mostrandoAgregarContacto) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = ClayDeepPurple)
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = nuevoNombreContacto,
                                    onValueChange = { nuevoNombreContacto = it },
                                    label = { Text("Nombre / Descripción (ej. Mamá, Doctor)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = nuevoTelefonoContacto,
                                    onValueChange = { nuevoTelefonoContacto = it },
                                    label = { Text("Número telefónico") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            mostrandoAgregarContacto = false
                                            nuevoNombreContacto = ""
                                            nuevoTelefonoContacto = ""
                                        },
                                        modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                    ) {
                                        Text("Cancelar", color = Color(0xFF6B5C85))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    ClayPuffyButton(
                                        onClick = {
                                            if (nuevoNombreContacto.isNotBlank() && nuevoTelefonoContacto.isNotBlank()) {
                                                contactosEmergencia.add(
                                                    ContactoEmergencia(nuevoNombreContacto.trim(), nuevoTelefonoContacto.trim())
                                                )
                                                nuevoNombreContacto = ""
                                                nuevoTelefonoContacto = ""
                                                mostrandoAgregarContacto = false
                                                Toast.makeText(context, "Contacto agregado", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.heightIn(min = dimens.buttonHeight)
                                    ) {
                                        Text("Guardar Contacto")
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. IDENTIFICATIVO DE SOPORTE
                item {
                    SectionTitle("Soporte y Legal")
                    ClayCard(
                        shape = RoundedCornerShape(24.dp),
                        containerColor = Color.White,
                        elevation = 6.dp
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            SupportItem(
                                icon = Icons.Default.BugReport,
                                titulo = "Reportar falla técnica",
                                subtitulo = "Infórmanos sobre errores o problemas en la app",
                                onClick = {
                                    Toast.makeText(context, "Soporte ProKids: Registro de ticket de falla técnica abierto", Toast.LENGTH_LONG).show()
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF4EFFB))
                            SupportItem(
                                icon = Icons.Default.Feedback,
                                titulo = "Enviar retroalimentación",
                                subtitulo = "Comparte ideas o sugerencias para mejorar Serena",
                                onClick = {
                                    Toast.makeText(context, "¡Gracias! Tu sugerencia fue enviada a nuestro equipo.", Toast.LENGTH_SHORT).show()
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF4EFFB))
                            SupportItem(
                                icon = Icons.Default.Policy,
                                titulo = "Aviso de privacidad",
                                subtitulo = "Tus datos médicos y de ánimo están cifrados localmente",
                                onClick = {
                                    Toast.makeText(context, "Aviso de Privacidad: Datos personales y de salud protegidos por la Ley de Protección de Datos.", Toast.LENGTH_LONG).show()
                                }
                            )
                            HorizontalDivider(color = Color(0xFFF4EFFB))
                            SupportItem(
                                icon = Icons.Default.Description,
                                titulo = "Términos y condiciones de uso",
                                subtitulo = "Lineamientos de la plataforma ProKids & Serena",
                                onClick = {
                                    Toast.makeText(context, "Términos y Condiciones: Uso exclusivo para asistencia terapéutica y de bienestar emocional.", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                }

                // 7. DESACTIVAR CUENTA (elimina el usuario y todos sus datos)
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    ClayCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        containerColor = Color(0xFFFFF5F7),
                        elevation = 3.dp,
                        borderStroke = BorderStroke(1.5.dp, Color(0xFFFFD9E2))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mostrandoDialogoDesactivar = true }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(dimens.buttonHeight)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE0E8)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFD62B57),
                                        modifier = Modifier.size(dimens.iconSmall)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Desactivar cuenta",
                                        fontSize = dimens.bodySize,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFD62B57)
                                    )
                                    Text(
                                        text = "Elimina tu usuario, tu código de vinculación y todos tus datos",
                                        fontSize = dimens.labelSize,
                                        color = Color(0xFF8B6C77)
                                    )
                                }
                            }
                        }
                    }
                }

                // 8. BOTÓN CERRAR SESIÓN
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    ClayPuffyButton(
                        onClick = onLogout,
                        containerColor = Color(0xFFFF4B6E),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimens.buttonHeightLarge)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(dimens.iconSmall)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cerrar Sesión",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = dimens.bodySize,
                                color = Color.White
                            )
                        }
                    }
                }

                // 9. PIE DE PÁGINA
                item {
                    Text(
                        text = "Serena v1.4.0 • ProKids MySQL Backend (XAMPP)",
                        fontSize = 12.sp,
                        color = Color(0xFF9E8FB3),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }
        }
    }

    // DIÁLOGO PARA VINCULAR NUEVO PACIENTE
    if (mostrandoDialogoVincular) {
        Dialog(
            onDismissRequest = { mostrandoDialogoVincular = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clayContentWidth(dimens)
                        .shadow(12.dp, RoundedCornerShape(26.dp)),
                    border = BorderStroke(1.dp, Color(0xFFEFE8F8))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Vincular Nuevo Paciente",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = dimens.titleSize,
                            color = Color(0xFF1E1333)
                        )
                        Text(
                            text = "Ingresa el código único del paciente que deseas supervisar (ej. SERENA-1234). Puedes vincular tantos pacientes como quieras:",
                            fontSize = dimens.labelSize,
                            color = Color(0xFF56476F)
                        )

                        OutlinedTextField(
                            value = codigoVincularInput,
                            onValueChange = { codigoVincularInput = it.uppercase() },
                            label = { Text("Código de Vinculación") },
                            placeholder = { Text("SERENA-XXXX") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1E1333),
                                unfocusedTextColor = Color(0xFF1E1333),
                                focusedBorderColor = ClayDeepPurple,
                                unfocusedBorderColor = Color(0xFFDDD2ED),
                                focusedContainerColor = Color(0xFFFAF8FE),
                                unfocusedContainerColor = Color(0xFFFAF8FE)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    mostrandoDialogoVincular = false
                                    codigoVincularInput = ""
                                },
                                modifier = Modifier.heightIn(min = dimens.buttonHeight)
                            ) {
                                Text("Cancelar", color = Color(0xFF6B5C85))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ClayPuffyButton(
                                onClick = {
                                    if (codigoVincularInput.isNotBlank()) {
                                        viewModel.linkPatient(codigoVincularInput) { success, msg ->
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            if (success) {
                                                mostrandoDialogoVincular = false
                                                codigoVincularInput = ""
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.heightIn(min = dimens.buttonHeight)
                            ) {
                                Text("Vincular")
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO PARA DESACTIVAR / ELIMINAR CUENTA
    if (mostrandoDialogoDesactivar) {
        Dialog(
            onDismissRequest = { mostrandoDialogoDesactivar = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clayContentWidth(dimens)
                        .shadow(14.dp, RoundedCornerShape(26.dp)),
                    border = BorderStroke(1.dp, Color(0xFFFFDDE3))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFD62B57),
                                modifier = Modifier.size(dimens.iconMedium)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Desactivar cuenta?",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = dimens.titleSize,
                                color = Color(0xFFD62B57)
                            )
                        }

                        Text(
                            text = "Esta acción eliminará permanentemente tu usuario, tu código de vinculación y desvinculará todos tus datos en este dispositivo. No podrás recuperarlos.",
                            fontSize = dimens.labelSize,
                            color = Color(0xFF4A3E5C),
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { mostrandoDialogoDesactivar = false },
                                modifier = Modifier.heightIn(min = dimens.buttonHeight)
                            ) {
                                Text("Cancelar", color = Color(0xFF6B5C85))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            ClayPuffyButton(
                                onClick = {
                                    mostrandoDialogoDesactivar = false
                                    viewModel.deactivateAccount {
                                        Toast.makeText(context, "Cuenta desactivada correctamente", Toast.LENGTH_LONG).show()
                                        onLogout()
                                    }
                                },
                                containerColor = Color(0xFFD62B57),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.heightIn(min = dimens.buttonHeight)
                            ) {
                                Text("Sí, Desactivar", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta con el código de vinculación único del perfil.
 * Visible para paciente, médico y familiar.
 */
@Composable
private fun LinkingCodeCard(
    code: String,
    isPatient: Boolean,
    onCopy: () -> Unit,
    onRegenerate: () -> Unit
) {
    val dimens = rememberClayDimens()
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF3ECFA),
        border = BorderStroke(1.dp, Color(0xFFDDD2ED)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = ClayDeepPurple,
                            modifier = Modifier.size(dimens.iconTiny)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Código de Vinculación Único",
                            fontSize = 11.sp,
                            color = Color(0xFF6B5C85),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = code,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = ClayDeepPurple
                    )
                }
                TextButton(
                    onClick = onCopy,
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(dimens.iconTiny),
                        tint = ClayDeepPurple
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copiar", color = ClayDeepPurple, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isPatient) {
                    "💡 Comparte este código único con tu médico o familiar para que se vinculen a tu perfil."
                } else {
                    "💡 Tu código identifica tu perfil. Usa el código de cada paciente (Configuración → su perfil) para vincularlo aquí."
                },
                fontSize = 11.sp,
                color = Color(0xFF7E6F94),
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            TextButton(
                onClick = onRegenerate,
                modifier = Modifier.heightIn(min = 36.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(dimens.iconTiny),
                    tint = Color(0xFF7E6F94)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Generar un código nuevo",
                    color = Color(0xFF7E6F94),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 16.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF1E1333),
        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun InfoRow(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF6B5C85),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = valor,
            fontSize = 13.sp,
            color = Color(0xFF1E1333),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SupportItem(
    icon: ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0xFFECE4F8)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ClayDeepPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1333)
            )
            Text(
                text = subtitulo,
                fontSize = 12.sp,
                color = Color(0xFF7E6F94)
            )
        }
    }
}
