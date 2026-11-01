package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.shadow
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
 * Diálogo reutilizable para vincular uno o varios pacientes a un perfil de
 * médico o familiar mediante el código único del paciente.
 */
@Composable
fun LinkPatientDialog(
    onDismiss: () -> Unit,
    onLink: (String) -> Unit
) {
    val dimens = rememberClayDimens()
    var code by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
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
                        text = "Ingresa el código único que aparece en la configuración del paciente (ej. SERENA-1234). Puedes vincular todos los pacientes que necesites.",
                        fontSize = dimens.labelSize,
                        color = Color(0xFF56476F),
                        lineHeight = 18.sp
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = { code = it.uppercase() },
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
                            onClick = onDismiss,
                            modifier = Modifier.heightIn(min = dimens.buttonHeight)
                        ) {
                            Text("Cancelar", color = Color(0xFF6B5C85))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        ClayPuffyButton(
                            onClick = { onLink(code.trim()) },
                            enabled = code.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.heightIn(min = dimens.buttonHeight)
                        ) {
                            Text("Vincular", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
