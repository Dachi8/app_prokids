package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ClayCard
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.ClayShadowColor
import com.example.ui.viewmodel.WellnessViewModel

@Composable
fun LoginScreen(
    viewModel: WellnessViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val existingUsers by viewModel.allUsers.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Iniciar Sesión, 1: Registro
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Register fields
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("PACIENTE") }
    var patientCode by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF654BE2),
                        Color(0xFF755AF6)
                    )
                )
            )
    ) {
        // Deep purple curved claymorphic header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Puffy Clay Serena Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(12.dp, CircleShape, ambientColor = Color(0x60000000))
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_serena_mascot),
                        contentDescription = "Mascota Serena",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Serena",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = "Tu red de bienestar emocional y acompañamiento",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFDDD2FF),
                    textAlign = TextAlign.Center
                )
            }
        }

        // White Clay Sheet with large 36dp rounded top corners
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .shadow(16.dp, RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp), ambientColor = Color(0x40000000)),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            color = Color(0xFFFAF8FE)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Conventional Standard Tabs: Iniciar Sesión / Crear Cuenta
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFFEFE9FA),
                        border = BorderStroke(1.dp, Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            // Tab 0: Iniciar Sesión
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedTabIndex = 0 },
                                shape = RoundedCornerShape(18.dp),
                                color = if (selectedTabIndex == 0) Color.White else Color.Transparent,
                                shadowElevation = if (selectedTabIndex == 0) 4.dp else 0.dp
                            ) {
                                Text(
                                    text = "Iniciar Sesión",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTabIndex == 0) ClayDeepPurple else Color(0xFF6B5C85),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }

                            // Tab 1: Registrarse
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { selectedTabIndex = 1 },
                                shape = RoundedCornerShape(18.dp),
                                color = if (selectedTabIndex == 1) Color.White else Color.Transparent,
                                shadowElevation = if (selectedTabIndex == 1) 4.dp else 0.dp
                            ) {
                                Text(
                                    text = "Crear Cuenta",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTabIndex == 1) ClayDeepPurple else Color(0xFF6B5C85),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                )
                            }
                        }
                    }
                }

                // Show existing accounts for quick login (only if user has registered before)
                if (selectedTabIndex == 0 && existingUsers.isNotEmpty()) {
                    item {
                        ClayCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp),
                            containerColor = Color.White,
                            elevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Continuar con mi cuenta:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF53456D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                existingUsers.forEach { user ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                viewModel.selectUser(user)
                                                onLoginSuccess()
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFECE4F8),
                                        border = BorderStroke(1.dp, Color.White)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = user.avatarEmoji, fontSize = 24.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = user.name,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E1333)
                                                )
                                                Text(
                                                    text = user.role.lowercase().replaceFirstChar { it.uppercase() },
                                                    fontSize = 12.sp,
                                                    color = ClayDeepPurple
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = ClayDeepPurple,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Register name field
                if (selectedTabIndex == 1) {
                    item {
                        Text(
                            text = "Nombre completo:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = { Text("Tu nombre y apellido", fontSize = 15.sp) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ClayDeepPurple)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .testTag("login_name_input"),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClayDeepPurple,
                                unfocusedBorderColor = Color(0xFFDED5EB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E1333),
                                unfocusedTextColor = Color(0xFF1E1333)
                            )
                        )
                    }
                }

                // Email field
                item {
                    Text(
                        text = "Correo electrónico o usuario:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1333)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("ejemplo@correo.com", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = ClayDeepPurple)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayDeepPurple,
                            unfocusedBorderColor = Color(0xFFDED5EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1E1333),
                            unfocusedTextColor = Color(0xFF1E1333)
                        )
                    )
                }

                // Password field
                item {
                    Text(
                        text = "Contraseña:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1333)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••", fontSize = 15.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ClayDeepPurple)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Ver contraseña",
                                    tint = Color(0xFF7A6C91)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ClayDeepPurple,
                            unfocusedBorderColor = Color(0xFFDED5EB),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedTextColor = Color(0xFF1E1333),
                            unfocusedTextColor = Color(0xFF1E1333)
                        )
                    )
                }

                // Role selection for Register OR Login role designation
                item {
                    Text(
                        text = "Tu rol en Serena:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1333)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val roles = listOf(
                            Triple("PACIENTE", "🌱", "Paciente"),
                            Triple("MEDICO", "🩺", "Médico"),
                            Triple("FAMILIAR", "👨‍👩‍👧", "Familiar")
                        )

                        roles.forEach { (roleKey, emoji, label) ->
                            val isSelected = selectedRole == roleKey
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .shadow(
                                        elevation = if (isSelected) 6.dp else 2.dp,
                                        shape = RoundedCornerShape(20.dp),
                                        ambientColor = ClayShadowColor
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedRole = roleKey },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFFECE4F8) else Color.White,
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ClayDeepPurple else Color(0xFFE2D9EE)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = emoji, fontSize = 28.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) ClayDeepPurple else Color(0xFF56476F)
                                    )
                                }
                            }
                        }
                    }
                }

                if (selectedRole != "PACIENTE") {
                    item {
                        Text(
                            text = "Código de vinculación del paciente:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E1333)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = patientCode,
                            onValueChange = { patientCode = it },
                            placeholder = { Text("Ej. SERENA-7492", fontSize = 15.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ClayDeepPurple,
                                unfocusedBorderColor = Color(0xFFDED5EB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedTextColor = Color(0xFF1E1333),
                                unfocusedTextColor = Color(0xFF1E1333)
                            )
                        )
                    }
                }

                // Submit Button: Large, Puffy 3D Clay
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    ClayPuffyButton(
                        onClick = {
                            val effectiveName = if (name.isNotBlank()) name else (email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Usuario")
                            val effectiveEmail = if (email.isNotBlank()) email else "usuario@serena.app"

                            viewModel.loginOrRegister(
                                name = effectiveName,
                                email = effectiveEmail,
                                password = password,
                                role = selectedRole,
                                patientCode = patientCode,
                                isLogin = selectedTabIndex == 0,
                                onComplete = onLoginSuccess,
                                onMessage = { message ->
                                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = if (selectedTabIndex == 0) {
                            email.isNotBlank() && password.isNotBlank()
                        } else {
                            name.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("enter_app_button"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text(
                            text = if (selectedTabIndex == 0) "Iniciar Sesión" else "Crear mi Cuenta",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // Footer toggle
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedTabIndex == 0) "¿No tienes una cuenta?" else "¿Ya tienes una cuenta?",
                            fontSize = 14.sp,
                            color = Color(0xFF56476F)
                        )
                        TextButton(
                            onClick = {
                                selectedTabIndex = if (selectedTabIndex == 0) 1 else 0
                            }
                        ) {
                            Text(
                                text = if (selectedTabIndex == 0) "Regístrate aquí" else "Inicia sesión",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ClayDeepPurple
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
