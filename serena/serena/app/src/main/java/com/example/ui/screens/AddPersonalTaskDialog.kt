package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ClayDeepPurple
import com.example.ui.theme.ClayPuffyButton
import com.example.ui.theme.clayContentWidth
import com.example.ui.theme.rememberClayDimens

/**
 * Diálogo para que el paciente añada sus propias metas personales.
 * Se usa tanto en el Inicio como en la pestaña de Tareas y Premios.
 */
@Composable
fun AddPersonalTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, description: String) -> Unit
) {
    val dimens = rememberClayDimens()
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clayContentWidth(dimens)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Añadir Mi Propia Meta 🌱",
                        fontSize = dimens.titleSize,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1333)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "+30 ⭐",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Crea una actividad que te haga sentir bien (ej. salir a caminar, ordenar tus juguetes, tomar agua).",
                    fontSize = dimens.labelSize,
                    color = Color(0xFF56476F),
                    lineHeight = 18.sp
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título de tu meta", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Leer 10 páginas de mi libro", color = Color(0xFF8E82A3)) },
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
                    label = { Text("Detalles o notas (opcional)", color = Color(0xFF1E1333)) },
                    placeholder = { Text("Ej. Con música suave antes de merendar", color = Color(0xFF8E82A3)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = dimens.buttonHeight)
                    ) {
                        Text("Cancelar", color = Color(0xFF6B5D83), fontWeight = FontWeight.Bold)
                    }

                    ClayPuffyButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                onAdd(title.trim(), desc.trim())
                            }
                        },
                        enabled = title.isNotBlank(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = dimens.buttonHeight)
                    ) {
                        Text("Guardar Meta", fontWeight = FontWeight.Bold, fontSize = dimens.bodySize)
                    }
                }
            }
        }
    }
}
