package com.arekb.cadence.feature.analytics.components

import androidx.compose.runtime.Immutable

/**
 * A generic representation of a ranked item (Artist, Track, or Genre).
 */
@Immutable
data class RankedItem(
    val id: String,
    val name: String,
    val subtitle: String? = null, // Artist name for Tracks, null for Artists
    val imageUrl: String?
)