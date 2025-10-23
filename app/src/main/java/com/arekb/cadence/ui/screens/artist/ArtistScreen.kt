package com.arekb.cadence.ui.screens.artist

import androidx.compose.foundation.background
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
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
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
    val uriHandler = LocalUriHandler.current
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Artist Header + Open in Spotify Button
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ArtistHeader(artist)
                OpenInSpotifyButton(
                    artist = artist,
                    onClick = { artistId ->
                        // Construct the Spotify artist URL and open it
                        val spotifyUrl = "https://open.spotify.com/artist/$artistId"
                        uriHandler.openUri(spotifyUrl)
                    }
                )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ArtistHeader(artist: TopArtistObject) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        AsyncImage(
            model = artist.images.firstOrNull()?.url,
            contentDescription = "Image for ${artist.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )
            Row(modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(8.dp),
                verticalAlignment = Alignment.Bottom)
            {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    color = MaterialTheme.colorScheme.surface,
                )
                Spacer(modifier = Modifier.weight(1f))
                PopularityBadge(popularity = artist.popularity)
            }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun OpenInSpotifyButton(
    artist: TopArtistObject,
    onClick : (String) -> Unit
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        val size = ButtonDefaults.LargeContainerHeight
        Button(
            onClick = { onClick(artist.id) },
            modifier = Modifier.heightIn(size),
            contentPadding = ButtonDefaults.contentPaddingFor(size)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spotify_small_logo_black),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.LargeIconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
            Text("Open in Spotify", style = ButtonDefaults.textStyleFor(size))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularityBadge(
    popularity: Int,
    modifier: Modifier = Modifier,
    plainTooltipText: String = "Popularity Score"
) {
    TooltipBox(
        modifier = modifier,
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = {
            PlainTooltip { Text(plainTooltipText) }
        },
        state = rememberTooltipState()
    ) {
        Surface(
            shape = RoundedCornerShape(50),
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
}
