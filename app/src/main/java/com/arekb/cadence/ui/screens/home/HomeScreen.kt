package com.arekb.cadence.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMyTopTracks: () -> Unit,
    onNavigateToMyTopArtists: () -> Unit,
    onNavigateToAnalytics: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.fetchUserProfile()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularWavyProgressIndicator()
            }
            uiState.error != null -> {
                Column {
                    Text(text = "Error loading profile: " + uiState.error!!)
                    Button(onClick = { viewModel.fetchUserProfile() }) {
                        Text("Retry")
                    }
                }
            }
            uiState.userProfile != null -> {
                val user = uiState.userProfile
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = user?.imageUrl,
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Welcome back", style = MaterialTheme.typography.headlineLarge)
                    Text(text = user?.displayName ?: "Default", style = MaterialTheme.typography.headlineLarge)

                    Button(onClick = onNavigateToMyTopTracks) {
                        Text("View My Top Tracks")
                    }
                    Button(onClick = onNavigateToMyTopArtists) {
                        Text("View My Top Artists")
                    }
                    Button(onClick = onNavigateToAnalytics) {
                        Text("View Analytics")
                    }
                }
            }
        }
    }
}