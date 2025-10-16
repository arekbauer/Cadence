package com.arekb.cadence.ui.screens.artist

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.FloatingToolbarScrollBehavior
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.arekb.cadence.R
import com.arekb.cadence.data.remote.dto.SimpleAlbumObject
import com.arekb.cadence.data.remote.dto.TopArtistObject
import com.arekb.cadence.data.remote.dto.TrackObject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistScreen(
    viewModel: ArtistViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedIndex by remember { mutableIntStateOf(0) }

    val exitAlwaysScrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(
                    text = uiState.artistDetails?.name ?: "Artist",
                    style = MaterialTheme.typography.titleLargeEmphasized)
                        },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.error != null -> {
                    Text("Error: ${uiState.error}")
                }
                uiState.artistDetails != null -> {
                    ArtistDetailsContent(
                        artist = uiState.artistDetails!!,
                        topTracks = uiState.topTracks,
                        albums = uiState.albums,
                        selectedIndex = selectedIndex
                    )
                }
            }
            ArtistViewToggleToolbar(
                selectedIndex = selectedIndex,
                onSelectionChanged = { newIndex -> selectedIndex = newIndex },
                scrollBehavior = exitAlwaysScrollBehavior,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset - 16.dp)
            )
        }
    }
}


@Composable
fun ArtistDetailsContent(
    artist: TopArtistObject,
    topTracks: List<TrackObject>,
    albums: List<SimpleAlbumObject>,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist Details
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                //ArtistHeader(artist)
                ArtistInfoSection(artist)
            }
        }

        // CONDITIONAL CONTENT ---
        when (selectedIndex) {
            0 -> {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Top Tracks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        topTracks.forEach { track ->
                            ListItem(
                                headlineContent = {
                                    Text(
                                        track.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                leadingContent = {
                                    AsyncImage(
                                        model = track.album.images.firstOrNull()?.url,
                                        contentDescription = track.name,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            )
                        }
                    }
                }
            }

            // --- ALBUMS CONTENT ---
            1 -> {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                        // Using LazyRow inside an item is fine for horizontal lists.
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(albums, key = { it.id }) { album ->
                                AlbumCard(album)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Optional: Extracted the Album item into its own composable for cleanliness
@Composable
private fun AlbumCard(album: SimpleAlbumObject) {
    Column(
        modifier = Modifier.width(150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card {
            AsyncImage(
                model = album.images.firstOrNull()?.url,
                contentDescription = album.name,
                modifier = Modifier.aspectRatio(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = album.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistViewToggleToolbar(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    scrollBehavior: FloatingToolbarScrollBehavior,
    modifier: Modifier = Modifier
) {
    val options = listOf("Overview", "Albums")

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        scrollBehavior = scrollBehavior,
        content = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, label ->
                ToggleButton(
                    shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                    checked = selectedIndex == index,
                    onCheckedChange = { onSelectionChanged(index) }
                ){
                    Text(label)
                }
            }
        }
    })
}


/**
 * Creates a visually expressive header with a decorative curve and the artist image.
 */
@Composable
private fun ArtistHeader(artist: TopArtistObject) {
    val headerColor = MaterialTheme.colorScheme.primaryContainer

    Box(
        // This Box is the foundation for layering the curve and the image
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp) // Gives space for the curve and image
    ) {
        // 1. The Decorative Curve (drawn on a Canvas)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = Path().apply {
                moveTo(0f, size.height * 0.5f) // Start on the left, halfway down
                // This creates the large curve
                quadraticTo(
                    x1 = size.width / 2,
                    y1 = size.height,
                    x2 = size.width,
                    y2 = size.height * 0.5f
                )
                lineTo(size.width, 0f) // Line to top-right
                lineTo(0f, 0f)        // Line to top-left
                close()
            }
            drawPath(
                path = path,
                color = headerColor.copy(alpha = 0.4f) // Semi-opaque accent color
            )
        }

        // 2. The Artist Image (placed on top of the Canvas)
        AsyncImage(
            model = artist.images.firstOrNull()?.url,
            contentDescription = "Image for ${artist.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .align(Alignment.Center)
                .size(220.dp)
                .clip(CircleShape)
        )
    }
}

/**
 * Displays the popularity badge, and Spotify button.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArtistInfoSection(artist: TopArtistObject) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {

        PopularityBadge(popularity = artist.popularity)

        val size = ButtonDefaults.MediumContainerHeight
        Button(
            onClick = { /* TODO: Handle Spotify link */ },
            modifier = Modifier.heightIn(size),
            contentPadding = ButtonDefaults.contentPaddingFor(size)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spotify_small_logo_black),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
            Text("Open in Spotify")
        }
    }
}

@Composable
private fun PopularityBadge(popularity: Int) {
    Surface(
        shape = RoundedCornerShape(50), // Pill shape
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Popularity",
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$popularity",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}