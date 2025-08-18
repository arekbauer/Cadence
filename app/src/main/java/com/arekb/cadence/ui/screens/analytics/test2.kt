package com.arekb.cadence.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arekb.cadence.ui.theme.CadenceTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// A data class to hold the audio feature data
data class SonicProfile2(
    val energy: Float,
    val danceability: Float,
    val valence: Float, // Happiness
    val acousticness: Float
)

/**
 * A minimal card that displays the user's sonic profile using a spider/radar chart.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun SonicProfileRadarChart(profile: SonicProfile2) {
    val features = listOf(
        profile.energy,
        profile.danceability,
        profile.valence,
        profile.acousticness
    )
    val featureLabels = listOf("Energy", "Dance", "Happy", "Acoustic")
    val textMeasurer = rememberTextMeasurer()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Sonic Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            var isAnimated by remember { mutableStateOf(false) }
            val animationProgress by animateFloatAsState(
                targetValue = if (isAnimated) 1f else 0f,
                animationSpec = tween(durationMillis = 1000, delayMillis = 200),
                label = "RadarChartAnimation"
            )

            LaunchedEffect(Unit) {
                isAnimated = true
            }

            Canvas(
                modifier = Modifier.size(300.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = size.minDimension / 2.5f // Smaller radius to leave space for labels
                val angleStep = 2 * PI / features.size

                // 1. Draw the minimal background grid (just the axes)
                drawRadarGrid(centerX, centerY, radius, features.size, angleStep)

                // 2. Draw the data shape
                drawDataShape(centerX, centerY, radius, features, angleStep, animationProgress)

                // 3. Draw the labels
                drawAxisLabels(centerX, centerY, radius, featureLabels, angleStep, textMeasurer)
            }
        }
    }
}

private fun DrawScope.drawRadarGrid(centerX: Float, centerY: Float, radius: Float, sides: Int, angleStep: Double) {
    val gridColor = Color.Gray.copy(alpha = 0.5f)

    // Draw lines from center to vertices
    for (i in 0 until sides) {
        val angle = i * angleStep - (PI / 2) // Offset by -90 degrees to start at the top
        val x = centerX + radius * cos(angle).toFloat()
        val y = centerY + radius * sin(angle).toFloat()
        drawLine(gridColor, start = Offset(centerX, centerY), end = Offset(x, y), strokeWidth = 1.dp.toPx())
    }
}

private fun DrawScope.drawDataShape(centerX: Float, centerY: Float, radius: Float, data: List<Float>, angleStep: Double, progress: Float) {
    val path = Path()
    val primaryColor = Color.Blue

    data.forEachIndexed { i, value ->
        val angle = i * angleStep - (PI / 2)
        val currentRadius = radius * value * progress // Animate the radius
        val x = centerX + currentRadius * cos(angle).toFloat()
        val y = centerY + currentRadius * sin(angle).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(path, color = primaryColor.copy(alpha = 0.4f))
    drawPath(path, color = primaryColor, style = Stroke(width = 2.dp.toPx()))
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawAxisLabels(centerX: Float, centerY: Float, radius: Float, labels: List<String>, angleStep: Double, textMeasurer: TextMeasurer) {
    val labelRadius = radius * 1.15f // Position labels just outside the chart
    val textStyle = TextStyle(
        fontWeight = FontWeight.SemiBold
    )

    labels.forEachIndexed { i, label ->
        val angle = i * angleStep - (PI / 2)
        val x = centerX + labelRadius * cos(angle).toFloat()
        val y = centerY + labelRadius * sin(angle).toFloat()
        val textLayoutResult = textMeasurer.measure(text = AnnotatedString(label), style = textStyle)

        // Adjust text position based on its measured size to center it
        val textX = x - textLayoutResult.size.width / 2
        val textY = y - textLayoutResult.size.height / 2

        drawText(textLayoutResult, topLeft = Offset(textX, textY))
    }
}


// --- Preview ---

@Preview(showBackground = true, name = "Minimal Sonic Profile Radar Chart")
@Composable
fun SonicProfileRadarChartPreview() {
    // Hardcoded sample data
    val sampleProfile = SonicProfile2(
        energy = 0.82f,
        danceability = 0.75f,
        valence = 0.65f,
        acousticness = 0.15f
    )

    CadenceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SonicProfileRadarChart(profile = sampleProfile)
        }
    }
}
