package com.arekb.cadence.ui.screens.genres

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.abs

const val NUMBER_OF_GENRES = 5

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

    val selectedGenre = if (centerItemIndex != -1) uiState.topGenresWithArtists.getOrNull(centerItemIndex) else null

    Scaffold(
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Your Top Genres") },
                subtitle = {
                    Text(text = "Past 12 Months", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    CircularWavyProgressIndicator()
                }
                uiState.error != null -> {
                    Text(text = uiState.error!!)
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        BoxWithConstraints {
                            val itemWidth = this.maxWidth * 0.30f
                            val horizontalPadding = (this.maxWidth - itemWidth) / 2

                            val maxCount = remember(uiState.topGenresWithArtists.take(NUMBER_OF_GENRES)) {
                                uiState.topGenresWithArtists.maxOfOrNull { it.count }?.toFloat() ?: 1f
                            }

                            LazyRow(
                                state = lazyListState,
                                modifier = Modifier.fillMaxHeight(0.60f),
                                contentPadding = PaddingValues(horizontal = horizontalPadding),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)
                            ) {
                                itemsIndexed(uiState.topGenresWithArtists.take(NUMBER_OF_GENRES)) { index, genre ->
                                    GenreCarouselItem(
                                        modifier = Modifier.width(itemWidth),
                                        genre = genre,
                                        isHighlighted = index == centerItemIndex,
                                        maxCount = maxCount,
                                        onClick = {
                                            coroutineScope.launch {
                                                lazyListState.animateScrollToItem(index)
                                            }
                                        }
                                    )
                                }
                            }
                        }

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
        }
    }
}

//TODO: Not finished - rework completely
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GenreCarouselItem(
    modifier: Modifier = Modifier,
    genre: GenreWithArtists,
    isHighlighted: Boolean,
    maxCount: Float,
    onClick: () -> Unit
) {

    val animatedAlpha by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.5f,
        animationSpec = tween(300),
        label = "AlphaAnimation"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0.9f,
        animationSpec = tween(300),
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
        Box(
            modifier = Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth(0.8f)
                .fillMaxHeight((genre.count / maxCount) * 0.85f)
                .clip(CircleShape)
                .background(if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight(0.2f)
                    .padding(top = 16.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimary,
                        shape = MaterialShapes.Flower.toShape()
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Rank 1
                Text(
                    text = "1",
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = genre.name,
            style = MaterialTheme.typography.titleLargeEmphasized,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
        )
    }
}

//TODO: Not finished - rework completely
@Composable
fun ArtistList(genre: GenreWithArtists) {
    Column(modifier = Modifier
        .padding(16.dp)
        .animateContentSize()) {
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