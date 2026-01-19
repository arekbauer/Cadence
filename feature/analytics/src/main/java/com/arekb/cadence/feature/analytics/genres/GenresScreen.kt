package com.arekb.cadence.feature.analytics.genres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.arekb.cadence.core.model.Artist
import com.arekb.cadence.core.model.Genre
import com.arekb.cadence.core.ui.component.CadenceErrorState
import com.arekb.cadence.feature.analytics.components.AnalyticsTopBar
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

const val NUMBER_OF_GENRES = 10

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GenresScreen(
    viewModel: GenresViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

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

    val selectedGenre = if (centerItemIndex != -1) uiState.topGenres.getOrNull(centerItemIndex) else null

    Scaffold(
        topBar = {
            AnalyticsTopBar(
                title = "Your Top Genres",
                subtitle = "Past 12 Months",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    GenresScreenSkeleton()
                }
                uiState.error != null -> {
                    CadenceErrorState(
                        message = "Error loading top genres"
                    )
                }
                else -> {
                    val genres = uiState.topGenres.take(NUMBER_OF_GENRES)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        BoxWithConstraints {
                            val itemWidth = this.maxWidth * 0.30f
                            val horizontalPadding = (this.maxWidth - itemWidth) / 2

                            val maxCount = remember(uiState.topGenres.take(NUMBER_OF_GENRES)) {
                                uiState.topGenres.maxOfOrNull { it.artistCount }?.toFloat()
                                    ?.pow(0.75f)
                                    ?: 1f
                            }

                            LazyRow(
                                state = lazyListState,
                                modifier = Modifier.fillMaxHeight(0.55f),
                                contentPadding = PaddingValues(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
                            ) {
                                itemsIndexed(uiState.topGenres.take(NUMBER_OF_GENRES)) { index, genre ->
                                    GenreBarChart(
                                        modifier = Modifier.width(itemWidth),
                                        genre = genre,
                                        isHighlighted = index == centerItemIndex,
                                        maxCount = maxCount,
                                        index = index + 1,
                                        onClick = {
                                            coroutineScope.launch {
                                                lazyListState.animateScrollToItem(index)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        ElevatedCard(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            GenreControlRow(
                                selectedGenre = selectedGenre,
                                centerItemIndex = centerItemIndex,
                                genreCount = genres.size,
                                onPrevious = {
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(
                                            (centerItemIndex - 1).coerceAtLeast(
                                                0
                                            )
                                        )
                                    }
                                },
                                onNext = {
                                    coroutineScope.launch {
                                        lazyListState.animateScrollToItem(
                                            (centerItemIndex + 1).coerceAtMost(
                                                genres.lastIndex
                                            )
                                        )
                                    }
                                }
                            )

                            selectedGenre?.let {
                                ArtistGrid(genre = it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GenreControlRow(
    selectedGenre: Genre?,
    centerItemIndex: Int,
    genreCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Previous Button
        IconButton(onClick = onPrevious, enabled = centerItemIndex > 0) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Genre")
        }

        Text(
            text = selectedGenre?.name ?: "Select a Genre",
            style = MaterialTheme.typography.headlineMediumEmphasized,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        // Next Button
        IconButton(onClick = onNext, enabled = centerItemIndex < genreCount - 1 && centerItemIndex != -1) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Genre")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GenreBarChart(
    modifier: Modifier = Modifier,
    genre: Genre,
    isHighlighted: Boolean,
    maxCount: Float,
    index: Int,
    onClick: () -> Unit
) {
    val springSpec = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.5f,
        animationSpec = springSpec,
        label = "AlphaAnimation"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.9f,
        animationSpec = springSpec,
        label = "ScaleAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(animatedScale)
            .alpha(animatedAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        GenreBar(
            modifier = Modifier.fillMaxWidth(0.8f),
            count = genre.artistCount,
            maxCount = maxCount,
            rank = index,
            isHighlighted = isHighlighted,
            onClick = onClick
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GenreBar(
    modifier: Modifier = Modifier,
    count: Int,
    maxCount: Float,
    rank: Int,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val barHeightFraction = ( (count.toFloat().pow(0.75f) / maxCount) * 0.9f )
        .coerceAtLeast(0.25f)

    Surface(
        shape = CircleShape,
        shadowElevation = if (isHighlighted) 8.dp else 2.dp
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight(barHeightFraction)
                .background(
                    if (isHighlighted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .size(75.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialShapes.VerySunny.toShape()
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistGrid(genre: Genre) {
    var itemsVisible by remember(genre) { mutableStateOf(false) }

    LaunchedEffect(genre) {
        itemsVisible = true
    }
    // List of material 3 expressive shapes
    val allShapes = remember {
        listOf(
            MaterialShapes.Slanted,
            MaterialShapes.Pill,
            MaterialShapes.Gem,
            MaterialShapes.Ghostish,
            MaterialShapes.Bun,
        )
    }

    val randomShapes = remember(genre.artists, allShapes) {
        List(genre.artists.size) { allShapes.random() }
    }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            itemsIndexed(genre.artists,
                key = { _, artist -> "${genre.name}-${artist.name}" }
            ) { index, artist ->
                AnimatedVisibility(
                    visible = itemsVisible,
                    enter = fadeIn(
                        animationSpec = tween(
                            durationMillis = 300,
                            // ✨ This is the key to the staggered effect! ✨
                            delayMillis = index * 50 // Each item waits 50ms longer than the last
                        )
                    ) + scaleIn(
                        initialScale = 0.8f,
                        animationSpec = tween(
                            durationMillis = 300,
                            delayMillis = index * 50
                        )
                    )
                ) {
                ArtistGridItem(
                    artist = artist,
                    shape = randomShapes[index].toShape(),
                )
            }
            }
        }

}

@Composable
fun ArtistGridItem(artist: Artist, shape: Shape) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AsyncImage(
                model = artist.imageUrl,
                contentDescription = "Image for ${artist.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = artist.name,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
}