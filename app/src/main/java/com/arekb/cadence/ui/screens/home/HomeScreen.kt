package com.arekb.cadence.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.arekb.cadence.R
import com.arekb.cadence.data.remote.dto.PlayHistoryObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMyTopTracks: () -> Unit,
    onNavigateToMyTopArtists: () -> Unit,
    onNavigateToTopGenres: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchInitialData()
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ){
            when {
                uiState.isLoading -> {
                    CircularWavyProgressIndicator()
                }

                uiState.error != null -> {
                    Column {
                        Text(text = "Error loading profile: " + uiState.error!!)
                        Button(onClick = { viewModel.fetchInitialData() }) {
                            Text("Retry")
                        }
                    }
                }

                uiState.userProfile != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = "Welcome Back",
                                style = MaterialTheme.typography.displayMediumEmphasized,
                                modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            )
                            Text(
                                text = uiState.userProfile!!.displayName,
                                style = MaterialTheme.typography.displayMediumEmphasized,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            if (uiState.recentlyPlayed.isNotEmpty()) {
                                LastPlayedSongCard(item = uiState.recentlyPlayed.first())
                            } else {
                                Text("Skeleton Would be showing")
                            }
                        }
                        item {
                            AnalyticsHubCard(
                                modifier = Modifier.padding(16.dp),
                                onNavigateToTopTracks = onNavigateToMyTopTracks,
                                onNavigateToTopArtists = onNavigateToMyTopArtists,
                                onNavigateToTopGenres = onNavigateToTopGenres
                            )
                        }
                        // Temporary old buttons
                        item {
                            Spacer(modifier = Modifier.height(900.dp))
                            Text(text = "HomeScreenTemp")
                            Button(onClick = onNavigateToMyTopTracks) {
                                Text("View My Top Tracks")
                            }
                            Button(onClick = onNavigateToMyTopArtists) {
                                Text("View My Top Artists")
                            }
                            Button(onClick = onNavigateToTopGenres) {
                                Text("View Top Genres")
                            }
                        }
                    }
                }
            }
        }
    }
}

// TODO: Temporary solution for now, looks decent though
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnalyticsHubCard(
    modifier: Modifier = Modifier,
    onNavigateToTopTracks: () -> Unit,
    onNavigateToTopArtists: () -> Unit,
    onNavigateToTopGenres: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.tertiaryContainer,
                            MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                )
                .padding(16.dp), // Extra bottom padding for the wave
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
            Text(
                text = "Analytics Hub",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Tap to explore your listening stats",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Horizontal row for the shaped navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnalyticsNavButton(
                    label = "Tracks",
                    icon = Icons.Default.MusicNote,
                    shape = MaterialShapes.Pill.toShape(),
                    onClick = onNavigateToTopTracks
                )
                AnalyticsNavButton(
                    label = "Artists",
                    icon = Icons.Default.Person,
                    shape = MaterialShapes.Ghostish.toShape(),
                    onClick = onNavigateToTopArtists
                )
                AnalyticsNavButton(
                    label = "Genres",
                    icon = Icons.Default.GraphicEq,
                    shape = MaterialShapes.Gem.toShape(),
                    onClick = onNavigateToTopGenres
                )
            }
        }
    }
}

@Composable
private fun AnalyticsNavButton(
    label: String,
    icon: ImageVector,
    shape: Shape,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // The visible button surface
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(MaterialTheme.colorScheme.surface, shape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

// TODO: Very generic for now, needs tweaking
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LastPlayedSongCard(
    item: PlayHistoryObject,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Album Art ---
                AsyncImage(
                    model = item.track.album.images.firstOrNull()?.url,
                    contentDescription = "Album art for ${item.track.name}",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(MaterialShapes.VerySunny.toShape()),
                    contentScale = ContentScale.Crop
                )

                // --- Text Details ---
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Last Played",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.track.name,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.track.artists.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Spotify Logo
            Icon(
                painter = painterResource(R.drawable.spotify_small_logo_black),
                contentDescription = "Powered by Spotify",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(30.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

