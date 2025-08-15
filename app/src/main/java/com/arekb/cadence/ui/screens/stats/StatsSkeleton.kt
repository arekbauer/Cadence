package com.arekb.cadence.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.arekb.cadence.ui.screens.util.shimmer

/**
 * The main skeleton layout that mimics the structure of the real screen.
 */
@Composable
fun StatsScreenSkeleton(innerPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = innerPadding.calculateTopPadding(),
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        ),
        userScrollEnabled = false // Disable scrolling for the skeleton
    ) {
        item {
            TopTrackHeroCardSkeleton()
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThreeTwoCardSkeleton(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                ThreeTwoCardSkeleton(Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        // Show a few placeholder track rows
        items(5) {
            TrackRowSkeleton()
        }
    }
}

/**
 * A skeleton placeholder for the TopTrackHeroCard.
 */
@Composable
fun TopTrackHeroCardSkeleton() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .shimmer(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) { /* Empty card is fine */  }
}

/**
 * A skeleton placeholder for the ThreeTwoCard.
 */
@Composable
fun ThreeTwoCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(245.dp)
            .clip(RoundedCornerShape(24.dp))
            .shimmer(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp)
    ) { /* Empty card is fine */ }
}

/**
 * A skeleton placeholder for the TrackRow.
 */
@Composable
fun TrackRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(30.dp).height(20.dp).clip(RoundedCornerShape(8.dp)).shimmer())
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.height(18.dp).fillMaxWidth(0.8f).clip(RoundedCornerShape(8.dp)).shimmer())
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(8.dp)).shimmer())
        }
        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp)).shimmer())
    }
}
