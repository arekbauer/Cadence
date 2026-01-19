package com.arekb.cadence.feature.analytics.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arekb.cadence.core.ui.component.CadenceErrorState
import com.arekb.cadence.core.ui.component.StatRow
import com.arekb.cadence.core.ui.component.StatsTimeRangeToolbar
import com.arekb.cadence.core.ui.component.ThreeTwoCard
import com.arekb.cadence.core.ui.component.TopStatHeroCard
import com.arekb.cadence.core.ui.component.skeleton.StatsScreenSkeleton

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GenericAnalyticsScreen(
    // Data State
    items: List<RankedItem>,
    isLoading: Boolean,
    error: String?,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    timeRangeOptions: List<String>,
    selectedTimeRangeIndex: Int,
    onTimeRangeSelected: (Int) -> Unit,
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AnalyticsTopBar(
                title = title,
                subtitle = "Last ${timeRangeOptions.getOrNull(selectedTimeRangeIndex) ?: ""}",
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = pullRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullRefreshState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        ) {
        Box(Modifier.fillMaxSize()
        ){
            AnimatedContent(
                targetState = selectedTimeRangeIndex,
                label = "ContentSwap",
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { it * direction } + fadeIn())
                        .togetherWith(slideOutHorizontally { -it * direction } + fadeOut())
                        .using(SizeTransform(clip = true))
                }
            ) { _ ->
                when {
                    isLoading || (items.isEmpty() && error == null) -> {
                        StatsScreenSkeleton()
                    }
                    error != null -> {
                        CadenceErrorState(message = error, onRetry = onRefresh)
                    }
                    else -> {
                        RankedListContent(
                            items = items,
                            listState = lazyListState
                        )
                    }
                }
            }
        }
            // Floating Toolbar
            StatsTimeRangeToolbar(
                selectedIndex = selectedTimeRangeIndex,
                onSelectionChanged = onTimeRangeSelected,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = FloatingToolbarDefaults.ScreenOffset)
            )
        }
    }
}

/**
 * Isolated logic for rendering the 1-2-3 Grid + List
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun RankedListContent(
    items: List<RankedItem>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 100.dp)
    ) {
        if (items.isNotEmpty()) {
            // Rank #1
            item {
                val item = items[0]
                TopStatHeroCard(
                    rank = 1,
                    name = item.name,
                    artistNames = item.subtitle,
                    imageUrl = item.imageUrl
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Rank #2 & #3 (Split Row)
            if (items.size >= 3) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val second = items[1]
                        val third = items[2]

                        ThreeTwoCard(
                            rank = 2,
                            imageUrl = second.imageUrl,
                            name = second.name,
                            artistNames = second.subtitle,
                            shape = MaterialShapes.Cookie7Sided.toShape(),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        ThreeTwoCard(
                            rank = 3,
                            imageUrl = third.imageUrl,
                            name = third.name,
                            artistNames = third.subtitle,
                            shape = MaterialShapes.Sunny.toShape(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // The Rest (#4+)
        itemsIndexed(items.drop(3)) { index, item ->
            StatRow(
                rank = index + 4,
                name = item.name,
                artistNames = item.subtitle,
                imageUrl = item.imageUrl
            )
        }
    }
}

/**
 * A consistent Large Top Bar for the Cadence app.
 * Wraps Material3's LargeFlexibleTopAppBar with standard styling.
 *
 * @param title The main screen title.
 * @param subtitle Optional subtitle (e.g., "Last 4 Weeks").
 * @param onNavigateBack Action when the back arrow is clicked.
 * @param scrollBehavior The scroll behavior to handle collapsing.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AnalyticsTopBar(
    title: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    subtitle: String? = null
) {
    LargeFlexibleTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        subtitle = if (subtitle != null) {
            {
                Text(
                    text = subtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else null,
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}