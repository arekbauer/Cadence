package com.arekb.cadence.ui.screens.search

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.arekb.cadence.R
import com.arekb.cadence.core.model.SearchResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateToArtist: (String) -> Unit,
    onNavigateToAlbum: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search", style = MaterialTheme.typography.titleLargeEmphasized) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            ExpressiveSearchTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                value = query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text("Search artists and albums...") },
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                })
            )
            val filters = SearchFilter.entries.toTypedArray()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                filters.forEachIndexed { index, filter ->
                    ToggleButton(
                        checked = filter == selectedFilter,
                        onCheckedChange = { viewModel.onFilterSelected(filter) },
                        modifier = Modifier.weight(1f),
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        }
                    ) {
                        Icon(
                            imageVector = filter.icon,
                            contentDescription = filter.name,
                            modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                        Text(filter.name)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (query.isBlank()) {
                IdleSearchPrompt(modifier = Modifier.weight(1f))
            } else {
                SearchResultsGrid(
                    pagingItems = searchResults,
                    onArtistClick = onNavigateToArtist,
                    onAlbumClick = onNavigateToAlbum
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IdleSearchPrompt(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_animation")

    // Animate a slow, gentle bobbing motion
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical_offset"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer { translationY = verticalOffset * 1.2f }
                        .size(32.dp)
                        .alpha(0.8f),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer { translationY = -verticalOffset * 1.2f }
                        .size(32.dp)
                        .alpha(0.8f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(id = R.drawable.spotify_small_logo_black),
                    contentDescription = "Spotify Logo",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // The main text content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Deep Dive",
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Find any artist or album",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // "Powered by Spotify" footer
            Text(
                text = "Powered by Spotify",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}

@Composable
fun ExpressiveSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            // Show a clear button only when there is text
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchResultsGrid(pagingItems: LazyPagingItems<SearchResult>,
                      onArtistClick: (String) -> Unit,
                      onAlbumClick: (String) -> Unit
){
    // Handle the initial loading state for the whole screen
    when (val state = pagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            Box(Modifier.fillMaxSize()) {
                LoadingIndicator(Modifier.align(Alignment.Center))
            }
        }
        is LoadState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error.message}")
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = { index ->
                        when (val item = pagingItems.peek(index)) {
                            is SearchResult.ArtistItem -> "artist_${item.artist.id}"
                            is SearchResult.AlbumItem -> "album_${item.album.id}"
                            is SearchResult.TrackItem -> "track_${item.track.id}"
                            null -> index
                        }
                    }
                ) { index ->
                    val result = pagingItems[index]

                    if (result != null) {
                        // FIX 2: Unwrap the Sealed Interface
                        when (result) {
                            is SearchResult.ArtistItem -> {
                                GridSearchResultItem(
                                    title = result.artist.name,
                                    subtitle = "Artist",
                                    imageUrl = result.artist.imageUrl,
                                    onClick = { onArtistClick(result.artist.id) }
                                )
                            }
                            is SearchResult.AlbumItem -> {
                                GridSearchResultItem(
                                    title = result.album.name,
                                    subtitle = "Album",
                                    imageUrl = result.album.imageUrl,
                                    onClick = { onAlbumClick(result.album.id) }
                                )
                            }
                            is SearchResult.TrackItem -> {
                                // If you choose to show tracks here later
                                GridSearchResultItem(
                                    title = result.track.name,
                                    subtitle = "Track",
                                    imageUrl = result.track.albumImageUrl,
                                    onClick = { /* Handle track click */ }
                                )
                            }
                        }
                    }
                }

                // Handle loading/error state for the next page (append)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    when (val appendState = pagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            Box(Modifier.fillMaxWidth().padding(32.dp)) {
                                LoadingIndicator(Modifier.align(Alignment.Center))
                            }
                        }
                        is LoadState.Error -> {
                            Text("Error loading more: ${appendState.error.message}",
                                modifier = Modifier.padding(16.dp))
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GridSearchResultItem(
    title: String,
    subtitle: String,
    imageUrl: String?,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Image for $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.large)
            )
            Column(
                modifier = Modifier.padding(4.dp).defaultMinSize(minHeight = 64.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}