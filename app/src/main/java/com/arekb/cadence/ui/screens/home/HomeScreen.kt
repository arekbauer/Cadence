package com.arekb.cadence.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
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
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.arekb.cadence.R
import com.arekb.cadence.data.local.database.entity.NewReleasesEntity
import com.arekb.cadence.data.remote.dto.PlayHistoryObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToMyTopTracks: () -> Unit,
    onNavigateToMyTopArtists: () -> Unit,
    onNavigateToTopGenres: () -> Unit,
    onLogout: () -> Unit
){
    val uiState by viewModel.uiState.collectAsState()

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
                    CircularWavyProgressIndicator()
                }

                uiState.error != null -> {
                    Column {
                        Text(text = "Error loading profile: " + uiState.error!!)
                        Button(onClick = { viewModel.onRetry() }) {
                            Text("Retry")
                        }
                    }
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
                                    onSearchClicked = { /* Implement at a later date */ },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WelcomeRow(
    displayName: String,
    avatarUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(0.1f))
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(avatarUrl)
                .crossfade(true)
                .build(),
            placeholder = painterResource(R.drawable.spotify_small_logo_black),
            contentDescription = "User Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnalyticsHubCard(
    modifier: Modifier = Modifier,
    onNavigateToTopTracks: () -> Unit,
    onNavigateToTopArtists: () -> Unit,
    onNavigateToTopGenres: () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card Header
            Text(
                text = "Analytics Hub",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = "Tap to explore your listening stats",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LastPlayedSongCard(
    item: PlayHistoryObject,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Album Art
                AsyncImage(
                    model = item.track.album.images.firstOrNull()?.url,
                    contentDescription = "Album art for ${item.track.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(8.dp))
                )

                Spacer(Modifier.width(16.dp))

                // Text content with clear hierarchy
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Last Played",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.track.name,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.track.artists.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(16.dp))

                // Spotify Logo
                Icon(
                    painter = painterResource(id = R.drawable.spotify_small_logo_black), // Replace with your drawable
                    contentDescription = "Spotify Logo",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EmptyLastPlayedCard(
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Placeholder
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "No history icon",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            // Informational Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nothing played recently",
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Your listening history will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PopularityScoreCard(
    score: Int?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialShapes.Slanted.toShape(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        val progress = (score ?: 0) / 100f

        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Popularity Score",
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularWavyProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.matchParentSize(),
                    wavelength = 30.dp,
                    waveSpeed = 8.dp,
                )
                Text(
                    text = score?.toString() ?: "N/A",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistSearchCard(
    onSearchClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSearchClicked,
        shape = MaterialShapes.Bun.toShape(),
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spotify_small_logo_black),
                contentDescription = "Spotify Logo",
                modifier = Modifier.size(40.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Find Artists",
                style = MaterialTheme.typography.titleMediumEmphasized,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun NewReleasesCarousel(
    releases: List<NewReleasesEntity>,
    modifier: Modifier = Modifier,
    onAlbumClick: (String) -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.tertiaryContainer
                    )
                )
            ).padding(top = 20.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "New Releases",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalMultiBrowseCarousel(
                state = rememberCarouselState { releases.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                preferredItemWidth = 150.dp,
                itemSpacing = 8.dp,
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) { index ->
                val release = releases[index]

                Card(
                    onClick = { onAlbumClick(release.id) },
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Box(modifier = Modifier.fillMaxSize().maskClip(MaterialTheme.shapes.large)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(release.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Album art for ${release.name}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                        startY = 150f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Text(
                                text = release.name,
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = release.artistName,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.spotify_small_logo_black),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Open on Spotify",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }

        }
    }
}