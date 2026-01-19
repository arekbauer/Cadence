package com.arekb.cadence.feature.analytics.genres

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.arekb.cadence.core.ui.component.skeleton.shimmer

@Composable
fun GenresScreenSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Section: Bar Charts
        BoxWithConstraints {
            val itemWidth = this.maxWidth * 0.30f
            val horizontalPadding = (this.maxWidth - itemWidth) / 2
            LazyRow(
                modifier = Modifier.fillMaxHeight(0.55f),
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false
            ) {
                items(5) { index ->
                    GenreBarChartSkeleton(
                        modifier = Modifier.width(itemWidth),
                        heightFraction = when (index) {
                            1, 3 -> 0.4f
                            else -> 0.6f
                        }
                    )
                }
            }
        }

        // Bottom Section: Artist Grid Card
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            GenreControlRowSkeleton()
            ArtistGridSkeleton()
        }
    }
}

@Composable
private fun GenreBarChartSkeleton(
    modifier: Modifier = Modifier,
    heightFraction: Float
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(heightFraction)
                .clip(CircleShape) // Clip the shape first
                .shimmer()         // Then apply the shimmer
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GenreControlRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .height(28.dp)
                .fillMaxWidth(0.5f)
                .clip(MaterialTheme.shapes.small)
                .shimmer()
        )
    }
}

@Composable
private fun ArtistGridSkeleton() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        userScrollEnabled = false
    ) {
        items(6) {
            ArtistGridItemSkeleton()
        }
    }
}

@Composable
private fun ArtistGridItemSkeleton() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Image placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.medium)
                .shimmer()
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Text placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmer()
        )
    }
}