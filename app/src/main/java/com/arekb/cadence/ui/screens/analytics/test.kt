package com.arekb.cadence.ui.screens.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arekb.cadence.ui.theme.CadenceTheme

// A simple data class to hold the audio feature data
data class SonicProfile(
    val energy: Float,
    val danceability: Float,
    val valence: Float, // How "happy" a track sounds
    val acousticness: Float
)

/**
 * A card that displays the user's sonic profile using a series of gauges.
 */
@Composable
fun SonicProfileCard(profile: SonicProfile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Your Sonic Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FeatureGauge(
                    label = "Energy",
                    value = profile.energy,
                    color = MaterialTheme.colorScheme.primary,
                    length = 20.dp
                )
                FeatureGauge(
                    label = "Danceability",
                    value = profile.danceability,
                    color = MaterialTheme.colorScheme.secondary,
                    length = 30.dp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                FeatureGauge(
                    label = "Happiness", // "Valence" is a bit technical for users
                    value = profile.valence,
                    color = MaterialTheme.colorScheme.tertiary,
                    length = 40.dp
                )
                FeatureGauge(
                    label = "Acoustic",
                    value = profile.acousticness,
                    color = MaterialTheme.colorScheme.error,
                    length = 50.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FeatureGauge(label: String, value: Float, color: Color, length: Dp) {
    var isAnimated by remember { mutableStateOf(false) }

    // Animate the progress value when the composable first appears
    val animatedValue by animateFloatAsState(
        targetValue = if (isAnimated) value else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "GaugeAnimation"
    )

    LaunchedEffect(Unit) {
        isAnimated = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularWavyProgressIndicator(
                progress = { animatedValue },
                modifier = Modifier.size(100.dp),
                color = color,
                waveSpeed = 5.dp,
                wavelength = length
            )
            // Display the percentage value in the center
            Text(
                text = "${(animatedValue * 100).toInt()}%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )
    }
}


// --- Preview ---

@Preview(showBackground = true, name = "Sonic Profile Card")
@Composable
fun SonicProfileCardPreview() {
    // Hardcoded sample data representing a high-energy, danceable profile
    val sampleProfile = SonicProfile(
        energy = 0.82f,
        danceability = 0.75f,
        valence = 0.65f,
        acousticness = 0.15f
    )

    CadenceTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            SonicProfileCard(profile = sampleProfile)
        }
    }
}