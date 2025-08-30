package com.arekb.cadence.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.arekb.cadence.ui.theme.CadenceTheme

// --- Data Models & Mock Data (Same as before) ---
data class User(val name: String, val profilePicUrl: String)
data class Track(val title: String, val artist: String, val albumArtUrl: String)
data class Album(val title: String, val artist: String, val albumArtUrl: String)
val mockUser = User("Arek", "https://randomuser.me/api/portraits/men/75.jpg")
val mockJumpBackInTracks = listOf(
    Track("Golden Hour", "Kacey Musgraves", "https://picsum.photos/seed/goldenhour/400"),
    Track("Utopia", "Travis Scott", "https://picsum.photos/seed/utopia/400"),
    Track("After Hours", "The Weeknd", "https://picsum.photos/seed/afterhours/400"),
)
val mockNewReleases = listOf(
    Album("Radical Optimism", "Dua Lipa", "https://picsum.photos/seed/radical/400"),
    Album("The Tortured Poets Department", "Taylor Swift", "https://picsum.photos/seed/ttpd/400"),
    Album("Hit Me Hard and Soft", "Billie Eilish", "https://picsum.photos/seed/hitme/400"),
)

// --- Main Home Screen ---
@Composable
fun SoundscapeHomeScreen() {
    // The entire screen's color scheme is derived from the top track
    val topTrackColor = MaterialTheme.colorScheme.primary
    val backgroundColor = topTrackColor.copy(alpha = 0.1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // --- Layer 1: The Soundscape Illustration ---
        // This is a fixed element at the top, like the Pixel Weather city view.
        SoundscapeHeader(user = mockUser, topTrackColor = topTrackColor)

        // --- Layer 2: The Scrolling Content Feed ---
        // This LazyColumn scrolls "over" the background but "under" the soundscape hills.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // A spacer pushes the content down to start below the illustration
            item { Spacer(modifier = Modifier.height(300.dp)) }

            // The content is wrapped in a card to give it a distinct, elevated feel
            item {
                Surface(
                    modifier = Modifier.fillParentMaxHeight(),
                    shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.padding(top = 32.dp)) {
                        SectionHeader(title = "Your Listening DNA")
                        AnalyticsQuickAccess()

                        SectionHeader(title = "Jump Back In")
                        JumpBackInCarousel(tracks = mockJumpBackInTracks)

                        SectionHeader(title = "New Release Radar")
                        NewReleaseCarousel(albums = mockNewReleases)

                        SectionHeader(title = "Library")
                        LibraryQuickAccess()
                    }
                }
            }
        }
    }
}

// --- Screen Components ---

@Composable
private fun SoundscapeHeader(user: User, topTrackColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        // --- The "Hills" of the soundscape ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStartPercent = 50, bottomEndPercent = 50))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            topTrackColor.copy(alpha = 0.3f),
                            topTrackColor.copy(alpha = 0.0f)
                        )
                    )
                )
        )
        // A second, smaller hill for more depth
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.6f)
                .fillMaxHeight(0.5f)
                .clip(RoundedCornerShape(topEnd = 100.dp))
                .background(topTrackColor.copy(alpha = 0.2f))
        )

        // --- The "Sun" or "Record" ---
        Box(
            modifier = Modifier
                .padding(top = 60.dp)
                .size(80.dp)
                .background(topTrackColor.copy(alpha = 0.9f), CircleShape)
        )

        // --- Header Content (Greeting & Profile Pic) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp, start = 24.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Good morning,",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            AsyncImage(
                model = user.profilePicUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 24.dp)
    )
}

@Composable
private fun AnalyticsQuickAccess() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AnalyticsCard(
                text = "Top Tracks",
                icon = Icons.Default.MusicNote,
                onClick = {}
            )
        }
        item {
            AnalyticsCard(
                text = "Top Artists",
                icon = Icons.Default.Person,
                onClick = {}
            )
        }
        item {
            AnalyticsCard(
                text = "Top Genres",
                icon = Icons.Default.Category,
                onClick = {}
            )
        }
    }
}

@Composable
private fun AnalyticsCard(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun JumpBackInCarousel(tracks: List<Track>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tracks) { track ->
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .clickable { /* Play Track */ }
            ) {
                AsyncImage(
                    model = track.albumArtUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun NewReleaseCarousel(albums: List<Album>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(albums) { album ->
            Column(
                modifier = Modifier
                    .width(160.dp)
                    .clickable { /* View Album */ }
            ) {
                AsyncImage(
                    model = album.albumArtUrl,
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(Modifier.height(8.dp))
                Text(text = album.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(text = album.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}


@Composable
private fun LibraryQuickAccess() {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LibraryRow(text = "Your Playlists", icon = { Icon(Icons.Default.QueueMusic, null) }, onClick = {})
        LibraryRow(text = "Saved Albums", icon = { Icon(Icons.Default.Album, null) }, onClick = {})
        LibraryRow(text = "Saved Tracks", icon = { Icon(Icons.Default.LibraryMusic, null) }, onClick = {})
    }
}

@Composable
private fun LibraryRow(text: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(Modifier.width(16.dp))
            Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}


// --- Preview and Activity ---
@Preview(showBackground = true)
@Composable
fun SoundscapeHomeScreenPreview() {
    CadenceTheme {
        SoundscapeHomeScreen()
    }
}