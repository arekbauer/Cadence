package com.arekb.cadence.ui.screens.stats

import com.arekb.cadence.data.remote.dto.TrackObject
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage

@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // This LaunchedEffect will run once when the screen is first composed.
    LaunchedEffect(key1 = Unit) {
        viewModel.fetchTopTracks()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else if (uiState.error != null) {
            Text(text = uiState.error!!)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(uiState.topTracks) { index, track ->
                    TrackRow(rank = index + 1, track = track)
                }
            }
        }
    }
}

@Composable
fun TrackRow(rank: Int, track: TrackObject) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$rank", modifier = Modifier.width(40.dp))
        AsyncImage(
            model = track.album.images.firstOrNull()?.url,
            contentDescription = "Album art for ${track.name}",
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = track.name, maxLines = 1)
            Text(text = track.artists.joinToString(", ") { it.name }, maxLines = 1)
        }
    }
}