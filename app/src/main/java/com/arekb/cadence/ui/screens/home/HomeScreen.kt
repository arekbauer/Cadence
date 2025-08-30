package com.arekb.cadence.ui.screens.home

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    onNavigateToAnalytics: () -> Unit
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
                            Button(onClick = onNavigateToAnalytics) {
                                Text("View Top Genres")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Collapsed shape with moving edges, on tap it will expand and show track details
@Composable
fun LastPlayedSongCard(
    item: PlayHistoryObject,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box {
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
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )

                // --- Text Details ---
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Last Played",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.track.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.track.artists.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Spotify Logo Placeholder ---
            // The real SVG logo would be used here.
            Icon(
                painter = painterResource(R.drawable.spotify_small_logo_black), // Placeholder icon
                contentDescription = "Powered by Spotify",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}