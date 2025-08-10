package com.arekb.cadence.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.arekb.cadence.data.local.database.entity.TopTracksEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // State to keep track of the selected time range index
    var selectedIndex by remember { mutableIntStateOf(0) }
    val timeRanges = listOf("short_term", "medium_term", "long_term")
    val options = listOf("Last Month", "Last 6 Months", "Last 12 Months")

    // This LaunchedEffect will run once initially, and then again whenever
    // the selectedIndex changes, triggering a new data fetch.
    LaunchedEffect(selectedIndex) {
        viewModel.fetchTopTracks(timeRanges[selectedIndex])
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- The Connected Button Group ---
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Content Area ---
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = uiState.error!!)
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        itemsIndexed(uiState.topTracks) { index, track ->
                            TrackRow(rank = index + 1, track = track)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackRow(rank: Int, track: TopTracksEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$rank",
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        AsyncImage(
            model = track.imageUrl,
            contentDescription = "Album art for ${track.trackName}",
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = track.trackName, maxLines = 1, style = MaterialTheme.typography.bodyLarge)
            Text(text = track.artistNames, maxLines = 1, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}