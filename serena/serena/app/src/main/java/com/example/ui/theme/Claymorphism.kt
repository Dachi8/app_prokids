package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Vibrant Claymorphic Palette matching Dribbble inspiration
val ClayDeepPurple = Color(0xFF654BE2)
val ClayCardWhite = Color(0xFFFFFFFF)
val ClayPuffyLilac = Color(0xFFECE4F8)
val ClayShadowColor = Color(0x35654BE2)

// Squircle Action Palette from reference image
val ClaySquircleGreen = Color(0xFF42D088)
val ClaySquirclePink = Color(0xFFFF7796)
val ClaySquirclePurple = Color(0xFF866EFF)
val ClaySquircleCyan = Color(0xFF42C3DF)

val ClayDefaultShape = RoundedCornerShape(26.dp)
val ClayButtonShape = RoundedCornerShape(22.dp)

@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    shape: Shape = ClayDefaultShape,
    containerColor: Color = Color.White,
    elevation: Dp = 8.dp,
    borderStroke: BorderStroke? = BorderStroke(1.5.dp, Color(0xFFFFFFFF)),
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = ClayShadowColor,
                spotColor = ClayShadowColor
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke
    ) {
        content()
    }
}

@Composable
fun ClayElevatedCard(
    modifier: Modifier = Modifier,
    shape: Shape = ClayDefaultShape,
    containerColor: Color = ClayPuffyLilac,
    elevation: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color(0x40654BE2),
                spotColor = Color(0x45654BE2)
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0x60FFFFFF))
                ),
                shape = shape
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        content()
    }
}

@Composable
fun ClayPuffyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ClayButtonShape,
    containerColor: Color = ClayDeepPurple,
    contentColor: Color = Color.White,
    elevation: Dp = 8.dp,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color(0xFFDED8EB),
            disabledContentColor = Color(0xFF8D82A2)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = elevation,
            pressedElevation = 2.dp
        ),
        modifier = modifier.border(
            width = 1.5.dp,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x70FFFFFF), Color.Transparent)
            ),
            shape = shape
        ),
        contentPadding = contentPadding
    ) {
        content()
    }
}
