package com.arekb.cadence.ui.screens.stats

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarDefaults.ScreenOffset
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arekb.cadence.data.local.database.entity.TopTracksEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    var selectedTimeRange by remember { mutableStateOf("short_term") }
    val timeRanges = mapOf(
        "4 Weeks" to "short_term",
        "6 Months" to "medium_term",
        "12 Months" to "long_term"
    )

    // This LaunchedEffect will run once initially, and then again whenever
    // the selectedIndex changes, triggering a new data fetch.
    LaunchedEffect(selectedTimeRange) {
        viewModel.fetchTopTracks(selectedTimeRange)
    }
    val exitAlwaysScrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(exitAlwaysScrollBehavior),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Your Top Tracks", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                subtitle = {
                    val displayLabel = timeRanges.entries.find { it.value == selectedTimeRange }?.key
                    Text(text = "Last ${displayLabel?: "Time Length"}", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        content = { innerPadding ->
            Box(Modifier.fillMaxSize()
            ) {
                when {
                    uiState.isLoading -> {
                        //TODO: Create skeletons of page instead of showing loading animation
                        StatsScreenSkeleton(innerPadding = innerPadding)
                    }
                    uiState.error != null -> {
                        Text(text = uiState.error!!, modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            state = lazyListState,
                            contentPadding = PaddingValues(
                                top = innerPadding.calculateTopPadding(),
                                start = 16.dp,
                                end = 16.dp,
                                bottom = 16.dp
                            )
                        ) {
                            if (uiState.topTracks.isNotEmpty()) {
                                item {
                                    TopTrackHeroCard(track = uiState.topTracks.first())
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                item{
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
                                    {
                                        ThreeTwoCard(
                                            track = uiState.topTracks[1],
                                            shape = MaterialShapes.Cookie7Sided.toShape(),
                                            Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        ThreeTwoCard(
                                            track = uiState.topTracks[2],
                                            MaterialShapes.Sunny.toShape(),
                                            Modifier.weight(1f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                            itemsIndexed(uiState.topTracks.drop(3)) { index, track ->
                                TrackRow(rank = index + 4, track = track)
                            }
                        }
                    }
                }
                HorizontalFloatingToolbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -ScreenOffset - 16.dp),
                    expanded = true,
                    scrollBehavior = exitAlwaysScrollBehavior,
                    content = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            timeRanges.forEach { (label, value) ->
                                ToggleButton(
                                    shapes = ToggleButtonShapes(
                                        shape = ToggleButtonDefaults.shape,
                                        pressedShape = ToggleButtonDefaults.shape,
                                        checkedShape = ToggleButtonDefaults.shape
                                    ),
                                    checked = selectedTimeRange == value,
                                    onCheckedChange = {
                                        if (it) { // ToggleButton can be unchecked, we only act on check
                                            selectedTimeRange = value
                                        }
                                    }
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                )

            }
        }
    )
}

// TODO: Add animations?
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopTrackHeroCard(track: TopTracksEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Text Content ---
            Column(modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialShapes.Flower.toShape()
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Rank 1
                    Text(
                        text = track.rank.toString(),
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = track.trackName,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track.artistNames,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            // --- Album Art ---
            AsyncImage(
                model = track.imageUrl,
                contentDescription = "Album art for ${track.trackName}",
                modifier = Modifier
                    .padding(8.dp)
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThreeTwoCard(track: TopTracksEntity, shape: Shape, modifier: Modifier) {

        // 2nd and 3rd Track
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = "Album art for ${track.trackName}",
                    modifier = Modifier
                        .size(125.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = track.trackName,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artistNames,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = shape
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = track.rank.toString(),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackRow(rank: Int, track: TopTracksEntity) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Handle track click if needed */ },
        // --- Leading Content: The Rank ---
        leadingContent = {
            Text(
                text = rank.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        // --- Headline Content: The Track Name ---
        headlineContent = {
            Text(
                text = track.trackName,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        },
        // --- Supporting Content: The Artist Names ---
        supportingContent = {
            Text(
                text = track.artistNames,
                maxLines = 1,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        // --- Trailing Content: The Album Art ---
        trailingContent = {
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = "Album art for ${track.trackName}",
                )
            }
        }
    )
}
