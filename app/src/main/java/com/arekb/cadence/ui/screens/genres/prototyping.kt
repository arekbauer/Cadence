package com.arekb.cadence.ui.screens.genres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arekb.cadence.ui.theme.CadenceTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

// --- FAKE DATA MODELS ---
data class Artist(
    val name: String,
    val imageUrl: String
)

data class GenreWithArtists(
    val name: String,
    val count: Int,
    val artists: List<Artist>
)

// --- MAIN SCREEN COMPOSABLE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnappingGenreChartScreen(genres: List<GenreWithArtists>) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Calculate which item is in the center
    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) -1 else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItemsInfo.minByOrNull { abs((it.offset + it.size / 2) - viewportCenter) }?.index ?: -1
            }
        }
    }

    val selectedGenre = if (centerItemIndex != -1) genres.getOrNull(centerItemIndex) else null

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Your Genre Deep Dive") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- The Horizontally Scrolling & Snapping Chart ---
            Text(
                text = "Your Top Genres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            val screenWidth = LocalConfiguration.current.screenWidthDp.dp
            val itemWidth = screenWidth * 0.75f // Each bar takes up 75% of the screen
            val horizontalPadding = (screenWidth - itemWidth) / 2

            LazyRow(
                state = lazyListState,
                modifier = Modifier.height(250.dp),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
            ) {
                itemsIndexed(genres) { index, genre ->
                    GenreColumn(
                        modifier = Modifier.width(itemWidth), // Set the width here
                        genre = genre,
                        isHighlighted = index == centerItemIndex,
                        onClick = {
                            coroutineScope.launch {
                                lazyListState.animateScrollToItem(index)
                            }
                        }
                    )
                }
            }

            // --- The Expandable Artist List ---
            AnimatedVisibility(
                visible = selectedGenre != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                selectedGenre?.let {
                    ArtistList(genre = it)
                }
            }
        }
    }
}

// --- CHART & ARTIST LIST COMPOSABLES ---
@Composable
private fun GenreColumn(
    modifier: Modifier = Modifier,
    genre: GenreWithArtists,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val maxCount = 20f // Assume a max count for normalization

    val animatedHeight by animateDpAsState(
        targetValue = if (isHighlighted) 220.dp else 180.dp,
        animationSpec = tween(300),
        label = "HeightAnimation"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.9f,
        animationSpec = tween(300),
        label = "ScaleAnimation"
    )

    Column(
        modifier = modifier
            .height(animatedHeight)
            .scale(animatedScale)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .fillMaxHeight(genre.count / maxCount)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
fun ArtistList(genre: GenreWithArtists) {
    Column(modifier = Modifier.padding(16.dp).animateContentSize()) {
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Artists in ${genre.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            genre.artists.forEach { artist ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = artist.imageUrl,
                        contentDescription = "Image for ${artist.name}",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = artist.name, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}


// --- PREVIEW ---
@Preview(showBackground = true, name = "Snapping Carousel Genre Chart")
@Composable
fun SnappingGenreChartScreenPreview() {
    val sampleData = listOf(
        GenreWithArtists("art pop", 12, listOf(
            Artist("Björk", "https://placehold.co/40x40/7E57C2/FFFFFF?text=B"),
            Artist("FKA twigs", "https://placehold.co/40x40/5C6BC0/FFFFFF?text=F")
        )),
        GenreWithArtists("indie rock", 9, listOf(
            Artist("The Strokes", "https://placehold.co/40x40/42A5F5/FFFFFF?text=S"),
            Artist("Arctic Monkeys", "https://placehold.co/40x40/29B6F6/FFFFFF?text=A")
        )),
        GenreWithArtists("wave", 7, listOf(
            Artist("The Cure", "https://placehold.co/40x40/26C6DA/FFFFFF?text=C")
        )),
        GenreWithArtists("garage", 5, listOf(
            Artist("The White Stripes", "https://placehold.co/40x40/66BB6A/FFFFFF?text=W")
        )),
        GenreWithArtists("alt rock", 4, listOf(
            Artist("Nirvana", "https://placehold.co/40x40/9CCC65/FFFFFF?text=N")
        ))
    )

    CadenceTheme {
        SnappingGenreChartScreen(genres = sampleData)
    }
}
