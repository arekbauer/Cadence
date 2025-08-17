package com.arekb.cadence.ui.screens.stats.artists

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.arekb.cadence.data.local.database.entity.TopArtistsEntity
import com.arekb.cadence.ui.screens.stats.StatsScreenSkeleton
import com.arekb.cadence.ui.screens.stats.tracks.StatsTimeRangeToolbar
import kotlinx.coroutines.launch

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopArtistsScreen(
    viewModel: ArtistsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val state = rememberPullToRefreshState()

    // API fetching logic
    var selectedIndex by remember { mutableIntStateOf(0) }
    val timeRanges = listOf("short_term", "medium_term", "long_term")
    val options = listOf("4 Weeks", "6 Months", "12 Months")


    // Pull to refresh logic
    var isRefreshing by remember { mutableStateOf(false) }
    val onRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            viewModel.onRefresh(timeRanges[selectedIndex])
            isRefreshing = false
        }
    }

    // This LaunchedEffect will run once initially, and then again whenever
    // the selectedIndex changes, triggering a new data fetch.
    LaunchedEffect(selectedIndex) {
        viewModel.fetchTopArtists(timeRanges[selectedIndex])
    }
    val exitAlwaysScrollBehavior =
        FloatingToolbarDefaults.exitAlwaysScrollBehavior(exitDirection = Bottom)

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(exitAlwaysScrollBehavior),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Your Top Artists", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                subtitle = {
                    val displayLabel = options[selectedIndex]
                    Text(text = "Last $displayLabel", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
                PullToRefreshBox(
                    modifier = Modifier.padding(innerPadding),
                    state = state,
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    indicator = {
                        PullToRefreshDefaults.LoadingIndicator(
                            state = state,
                            isRefreshing = isRefreshing,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    },
                ) {
                    AnimatedContent(
                        targetState = selectedIndex,
                        label = "PageContentAnimation",
                        transitionSpec = {
                            val forward = targetState > initialState

                            val enterTransition = slideInHorizontally { width ->
                                if (forward) width else -width
                            } + fadeIn()

                            val exitTransition = slideOutHorizontally { width ->
                                if (forward) -width else width
                            } + fadeOut()

                            enterTransition.togetherWith(exitTransition).using(
                                SizeTransform(clip = true)
                            )
                        }
                    ) {
                    when {
                        uiState.isLoading || uiState.topArtists.isEmpty() -> {
                            StatsScreenSkeleton()
                        }
                        uiState.error != null -> {
                            Text(
                                text = uiState.error!!,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState,
                                contentPadding = PaddingValues(
                                    top = 4.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                )
                            ) {
                                if (uiState.topArtists.isNotEmpty()) {
                                    item {
                                        TopArtistHeroCard(artist = uiState.topArtists.first())
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        )
                                        {
                                            ThreeTwoCardArtist(
                                                artist = uiState.topArtists[1],
                                                shape = MaterialShapes.Cookie7Sided.toShape(),
                                                Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            ThreeTwoCardArtist(
                                                artist = uiState.topArtists[2],
                                                MaterialShapes.Sunny.toShape(),
                                                Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                itemsIndexed(uiState.topArtists.drop(3)) { index, artist ->
                                    ArtistRow(rank = index + 4, artist = artist)
                                }
                            }
                        }
                    }
                    }
                }
                StatsTimeRangeToolbar(
                    selectedIndex = selectedIndex,
                    onSelectionChanged = { newIndex -> selectedIndex = newIndex },
                    scrollBehavior = exitAlwaysScrollBehavior,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -FloatingToolbarDefaults.ScreenOffset - 16.dp)
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopArtistHeroCard(artist: TopArtistsEntity) {
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
                        text = artist.rank.toString(),
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = artist.artistName,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.width(32.dp))
            // --- Album Art ---
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = "Album art for ${artist.artistName}",
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
fun ThreeTwoCardArtist(artist: TopArtistsEntity, shape: Shape, modifier: Modifier) {

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
                model = artist.imageUrl,
                contentDescription = "Album art for ${artist.artistName}",
                modifier = Modifier
                    .size(125.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artist.artistName,
                style = MaterialTheme.typography.titleSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
                    text = artist.rank.toString(),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistRow(rank: Int, artist: TopArtistsEntity) {
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
                text = artist.artistName,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        },
        // --- Trailing Content: The Album Art ---
        trailingContent = {
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = artist.imageUrl,
                    contentDescription = "Album art for ${artist.artistName}",
                )
            }
        }
    )
}
