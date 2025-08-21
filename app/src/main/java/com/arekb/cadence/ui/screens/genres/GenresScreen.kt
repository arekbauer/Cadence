package com.arekb.cadence.ui.screens.genres

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GenresScreen(
    viewModel: GenresViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Your Top Genres") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularWavyProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = uiState.error!!)
                }
                uiState.topGenres.isNotEmpty() -> {
                    // We only want to show the top 5 genres in the bar chart
                    GenreBarChart(genres = uiState.topGenres.take(5))
                }
                else -> {
                    Text("Not enough listening data to generate analytics.")
                }
            }
        }
    }
}

/**
 * A composable that displays a horizontal bar chart for a list of genres.
 */
@Composable
private fun GenreBarChart(genres: List<Pair<String, Int>>) {
    // Find the maximum count to normalize the bar widths
    val maxCount = remember(genres) { genres.firstOrNull()?.second ?: 1 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Your Top Genres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            genres.forEach { (name, count) ->
                GenreBar(
                    genreName = name,
                    // Calculate the bar's width as a fraction of the max count
                    fraction = count.toFloat() / maxCount
                )
            }
        }
    }
}

@Composable
private fun GenreBar(genreName: String, fraction: Float) {
    var isAnimated by remember { mutableStateOf(false) }

    // Animate the bar's width when it first appears
    val animatedFraction by animateFloatAsState(
        targetValue = if (isAnimated) fraction else 0f,
        animationSpec = tween(durationMillis = 1000, delayMillis = 200),
        label = "BarAnimation"
    )

    // Trigger the animation when the composable enters the screen
    LaunchedEffect(Unit) {
        isAnimated = true
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = genreName,
            modifier = Modifier.weight(0.4f), // Give 40% of the space to the label
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        // The bar itself
        Box(
            modifier = Modifier
                .weight(0.6f) // Give 60% of the space to the bar
                .height(24.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction) // Animate the width
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
