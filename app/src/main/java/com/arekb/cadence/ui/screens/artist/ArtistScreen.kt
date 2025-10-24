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
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val exitAlwaysScrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    Scaffold(modifier = Modifier
        .nestedScroll(scrollBehavior.nestedScrollConnection)
        .nestedScroll(exitAlwaysScrollBehavior),
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
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
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
                        selectedIndex = selectedIndex,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
            ArtistViewToggleToolbar(
                selectedIndex = selectedIndex,
                onSelectionChanged = { newIndex -> selectedIndex = newIndex },
                scrollBehavior = exitAlwaysScrollBehavior,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = -FloatingToolbarDefaults.ScreenOffset -16.dp)
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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

        // CONDITIONAL CONTENT
        when (selectedIndex) {
            0 -> {
                item {
                    Column {
                        Text(
                            text = "Popular",
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                        )

                        topTracks.forEachIndexed { index, track ->
                            val itemShape = when (index) {
                                0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                                9 -> RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                else -> RoundedCornerShape(0.dp)
                            }
                            TrackListItem(
                                track = track,
                                rank = index + 1,
                                itemShape = itemShape
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
private fun TrackListItem(track: TrackObject, rank: Int, itemShape: RoundedCornerShape) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(itemShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        // Rank Number
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(24.dp)
        )

        // Album Art
        AsyncImage(
            model = track.album.images.firstOrNull()?.url,
            contentDescription = "Album art for ${track.name}",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Track Title and Artists
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = track.artists.joinToString(", ") { it.name },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

//TODO: REVAMP!
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
private fun ArtistViewToggleToolbar(
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
                .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically)
            {
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                PopularityBadge(popularity = artist.popularity, modifier = Modifier.padding(top = 2.dp))
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
        Spacer(modifier = Modifier.height(16.dp))

        val size = ButtonDefaults.MediumContainerHeight
        Button(
            onClick = { onClick(artist.id) },
            modifier = Modifier.heightIn(size).offset(y = -size / 2),
            contentPadding = ButtonDefaults.contentPaddingFor(size)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.spotify_small_logo_black),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.MediumIconSize),
            )
            Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
            Text("Open in Spotify", style = ButtonDefaults.textStyleFor(size))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PopularityBadge(
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
