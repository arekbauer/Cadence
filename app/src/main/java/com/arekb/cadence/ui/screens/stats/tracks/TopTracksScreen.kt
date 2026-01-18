package com.arekb.cadence.ui.screens.stats.tracks

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.FloatingToolbarExitDirection.Companion.Bottom
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.cadence.ui.screens.stats.StatRow
import com.arekb.cadence.ui.screens.stats.StatsScreenSkeleton
import com.arekb.cadence.ui.screens.stats.StatsTimeRangeToolbar
import com.arekb.cadence.ui.screens.stats.ThreeTwoCard
import com.arekb.cadence.ui.screens.stats.TopStatHeroCard
import kotlinx.coroutines.launch

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopTracksScreen(
    viewModel: StatsViewModel = hiltViewModel(),
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
        viewModel.fetchTopTracks(timeRanges[selectedIndex])
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
                        uiState.isLoading || uiState.topTracks.isEmpty() -> {
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
                                if (uiState.topTracks.isNotEmpty()) {
                                    item {
                                        val topTrack = uiState.topTracks.first()
                                        TopStatHeroCard(
                                            rank = 1,
                                            name = topTrack.name,
                                            artistNames = topTrack.artists.joinToString(separator = ", ") { it.name },
                                            imageUrl = topTrack.albumImageUrl
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        )
                                        {
                                            val secondTrack = uiState.topTracks[1]
                                            val thirdTrack = uiState.topTracks[2]
                                            ThreeTwoCard(
                                                rank = 2,
                                                imageUrl = secondTrack.albumImageUrl,
                                                name = secondTrack.name,
                                                artistNames = secondTrack.artists.joinToString(separator = ", ") { it.name },
                                                shape = MaterialShapes.Cookie7Sided.toShape(),
                                                Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            ThreeTwoCard(
                                                rank = 3,
                                                imageUrl = thirdTrack.albumImageUrl,
                                                name = thirdTrack.name,
                                                artistNames = thirdTrack.artists.joinToString(separator = ", ") { it.name },
                                                shape = MaterialShapes.Sunny.toShape(),
                                                Modifier.weight(1f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                itemsIndexed(uiState.topTracks.drop(3)) { index, track ->
                                    StatRow(
                                        rank = index + 4,
                                        name = track.name,
                                        artistNames = track.artists.joinToString(separator = ", ") { it.name },
                                        imageUrl = track.albumImageUrl
                                    )
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