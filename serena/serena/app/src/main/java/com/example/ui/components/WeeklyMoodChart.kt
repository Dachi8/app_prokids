package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MoodEntryEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyMoodChart(
    moodEntries: List<MoodEntryEntity>,
    modifier: Modifier = Modifier
) {
    // Generate last 7 days keys
    val calendar = Calendar.getInstance()
    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val sdfDisplay = SimpleDateFormat("EEE", Locale.forLanguageTag("es-ES"))

    val last7Days = (6 downTo 0).map { offset ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -offset)
        Pair(sdfKey.format(cal.time), sdfDisplay.format(cal.time).uppercase().take(3))
    }

    // Map entries to days
    val moodByDate = moodEntries.associateBy { it.dateString }
    val scores = last7Days.map { (dateKey, _) ->
        moodByDate[dateKey]?.moodScore ?: 0
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = Color(0xFFECE4F8)
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0x286B4FA0),
                spotColor = Color(0x306B4FA0)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFEDE6F8))
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Evolución de la Semana",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E1333)
                )
                val avg = scores.filter { it > 0 }.let {
                    if (it.isNotEmpty()) String.format(Locale.getDefault(), "%.1f / 5", it.average()) else "Sin datos"
                }
                Text(
                    text = "Promedio: $avg",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 12.dp.toPx()
                    val horizontalInset = 16.dp.toPx()
                    val availableHeight = height - bottomPadding
                    val availableWidth = width - 2 * horizontalInset
                    val stepX = availableWidth / (last7Days.size - 1).coerceAtLeast(1)

                    // Draw horizontal guide lines (1 to 5)
                    for (i in 1..5) {
                        val y = availableHeight - (i - 1) * (availableHeight / 4)
                        drawLine(
                            color = Color(0xFFEFE8FB),
                            start = Offset(horizontalInset, y),
                            end = Offset(width - horizontalInset, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Plot points
                    val points = mutableListOf<Offset>()
                    val validIndices = mutableListOf<Int>()

                    scores.forEachIndexed { index, score ->
                        if (score > 0) {
                            val x = horizontalInset + index * stepX
                            val y = availableHeight - (score - 1) * (availableHeight / 4)
                            points.add(Offset(x, y))
                            validIndices.add(index)
                        }
                    }

                    // Draw filled gradient area and curve line if at least 2 points
                    if (points.size >= 2) {
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val midX = (prev.x + curr.x) / 2
                                cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                            }
                        }

                        // Fill under path first
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, availableHeight)
                            lineTo(points.first().x, availableHeight)
                            close()
                        }
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(containerColor, Color.Transparent),
                                startY = 0f,
                                endY = availableHeight
                            )
                        )

                        // Draw stroke on top of fill
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }

                    // Draw circles for recorded points
                    points.forEach { pt ->
                        drawCircle(
                            color = Color.White,
                            radius = 6.5.dp.toPx(),
                            center = pt
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = pt
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Days labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                last7Days.forEachIndexed { index, (_, label) ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val hasScore = scores[index] > 0
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (hasScore) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (hasScore) MaterialTheme.colorScheme.primary else Color(0xFF7A6D90)
                        )
                    }
                }
            }
        }
    }
}
