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
import androidx.compose.material3.CircularWavyProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.arekb.cadence.data.local.database.entity.TopTracksEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
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
                        CircularWavyProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                                    ThreeTwoCard(track2 = uiState.topTracks[1], track3 = uiState.topTracks[2])
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopTrackHeroCard(track: TopTracksEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .padding(end = 16.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = MaterialShapes.Cookie7Sided.toShape()
                    ),
                contentAlignment = Alignment.Center,
                ) {
                Text(
                    text = track.rank.toString(),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            // --- Album Art ---
            AsyncImage(
                model = track.imageUrl,
                contentDescription = "Album art for ${track.trackName}",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(20.dp))

            // --- Text Content ---
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = track.trackName,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = track.artistNames,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

//TODO: Finish creating!
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThreeTwoCard(track2: TopTracksEntity, track3: TopTracksEntity) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween)
    {
        // 2nd Track
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                AsyncImage(
                    model = track2.imageUrl,
                    contentDescription = "Album art for ${track2.trackName}",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = track2.trackName)
                Text(text = track2.artistNames)
                Text(text = track2.rank.toString())
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        // 3rd Track
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                AsyncImage(
                    model = track3.imageUrl,
                    contentDescription = "Album art for ${track3.trackName}",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = track3.trackName)
                Text(text = track3.artistNames)
                Text(text = track3.rank.toString())
            }
        }
    }
}


@Composable
fun TrackRow(rank: Int, track: TopTracksEntity) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)) // Add rounded corners to the whole row
            .clickable { /* Handle track click if needed */ },
        // --- Leading Content: The Album Art ---
        leadingContent = {
            // Use a Card for a subtle, clean boundary around the image
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = track.imageUrl,
                    contentDescription = "Album art for ${track.trackName}",
                )
            }
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
        // --- Trailing Content: The Rank ---
        trailingContent = {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    )
}
