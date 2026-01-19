package com.arekb.cadence.feature.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.cadence.core.ui.component.CadenceErrorState
import com.arekb.cadence.core.ui.component.CadenceLoader

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMyTopTracks: () -> Unit,
    onNavigateToMyTopArtists: () -> Unit,
    onNavigateToTopGenres: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onLogout: () -> Unit
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is HomeViewEvent.NavigateToLogin -> onLogout()
            }
        }
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
                    CadenceLoader()
                }

                uiState.error != null -> {
                    CadenceErrorState(
                        message = "Error loading profile",
                        onRetry = viewModel::onRetry,
                    )
                }

                uiState.userProfile != null -> {
                    val uriHandler = LocalUriHandler.current
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            WelcomeRow(
                                displayName = uiState.userProfile!!.displayName,
                                avatarUrl = uiState.userProfile!!.imageUrl,
                            )
                        }
                        item {
                            if (uiState.recentlyPlayed.isNotEmpty()) {
                                LastPlayedSongCard(item = uiState.recentlyPlayed.first())
                            } else {
                                EmptyLastPlayedCard()
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
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                PopularityScoreCard(
                                    score = uiState.popularityScore,
                                    modifier = Modifier.weight(1f)
                                )
                                ArtistSearchCard(
                                    onSearchClicked = onNavigateToSearch,
                                    modifier = Modifier.padding(16.dp).weight(1f),
                                )
                            }
                        }
                        if (uiState.newReleases.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                NewReleasesCarousel(
                                    releases = uiState.newReleases,
                                    onAlbumClick = { albumId ->
                                        // Construct the Spotify album URL and open it
                                        val spotifyUrl = "https://open.spotify.com/album/$albumId"
                                        uriHandler.openUri(spotifyUrl)
                                    }
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}