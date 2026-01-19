package com.arekb.cadence.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsTimeRangeToolbar(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = listOf("4 Weeks", "6 Months", "12 Months")

    HorizontalFloatingToolbar(
        modifier = modifier,
        expanded = true,
        content = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEachIndexed { index, label ->
                    ToggleButton(
                        shapes = ToggleButtonDefaults.shapes(checkedShape = MaterialTheme.shapes.large),
                        checked = selectedIndex == index,
                        onCheckedChange = { if (it) onSelectionChanged(index) }
                    ) {
                        Text(label)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopStatHeroCard(
    rank: Int,
    name: String,
    artistNames: String?,
    imageUrl: String?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Text Content ---
            Column(modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialShapes.Flower.toShape()
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    // Rank 1
                    Text(
                        text = rank.toString(),
                        style = MaterialTheme.typography.headlineLargeEmphasized,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (artistNames != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = artistNames,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.width(32.dp))
            // Picture
            AsyncImage(
                model = imageUrl,
                contentDescription = "Album art/Artist picture for $name",
                modifier = Modifier
                    .padding(8.dp)
                    .size(130.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThreeTwoCard(
    rank : Int,
    imageUrl: String?,
    name: String,
    artistNames: String?,
    shape: Shape,
    modifier: Modifier
){
    // 2nd and 3rd Track
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally)
        {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Album art for $name",
                modifier = Modifier
                    .size(125.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (artistNames != null)
            {
                Text(
                    text = artistNames,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        shape = shape
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatRow(
    rank: Int,
    name : String,
    artistNames: String?,
    imageUrl: String?
){
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* Handle track click if needed */ },
        // Rank
        leadingContent = {
            Text(
                text = rank.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        // Name
        headlineContent = {
            Text(
                text = name,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        },
        // Artist names
        supportingContent = {
            if (artistNames != null) {
                Text(
                    text = artistNames,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        // Album art
        trailingContent = {
            Card(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Album art for $name",
                )
            }
        }
    )
}
