package com.arekb.cadence.feature.analytics.artists

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arekb.cadence.feature.analytics.components.GenericAnalyticsScreen
import com.arekb.cadence.feature.analytics.components.RankedItem
import kotlinx.coroutines.launch

@Composable
fun TopArtistsScreen(
    viewModel: ArtistsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var selectedIndex by remember { mutableIntStateOf(0) }

    val timeRanges = remember { listOf("short_term", "medium_term", "long_term") }
    val displayLabels = remember { listOf("4 Weeks", "6 Months", "12 Months") }

    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(selectedIndex) {
        viewModel.fetchTopArtists(timeRanges[selectedIndex])
    }

    val handleRefresh: () -> Unit = {
        isRefreshing = true
        coroutineScope.launch {
            viewModel.onRefresh(timeRanges[selectedIndex])
            isRefreshing = false
        }
    }

    val rankedItems = remember(uiState.topArtists) {
        uiState.topArtists.map { artist ->
            RankedItem(
                id = artist.id,
                name = artist.name,
                subtitle = null,
                imageUrl = artist.imageUrl
            )
        }
    }

    GenericAnalyticsScreen(
        items = rankedItems,
        isLoading = uiState.isLoading,
        error = uiState.error,
        title = "Your Top Artists",

        isRefreshing = isRefreshing,
        onRefresh = handleRefresh,

        timeRangeOptions = displayLabels,
        selectedTimeRangeIndex = selectedIndex,
        onTimeRangeSelected = { newIndex -> selectedIndex = newIndex },

        onNavigateBack = onNavigateBack
    )
}