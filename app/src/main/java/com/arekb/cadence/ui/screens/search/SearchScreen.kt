package com.arekb.cadence.ui.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.arekb.cadence.R
import com.arekb.cadence.data.remote.paging.SearchResult

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()

    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Search", style = MaterialTheme.typography.titleLargeEmphasized) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            ExpressiveSearchTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    // CHANGED: Detect when the focus state changes
                    .onFocusChanged { focusState ->
                        isSearchActive = focusState.isFocused
                    },
                value = query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = { Text("Search artists and albums...") },
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                })
            )

            // CHANGED: The ButtonGroup is now wrapped in AnimatedVisibility
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn(animationSpec = tween(200)) + slideInVertically(),
                exit = fadeOut(animationSpec = tween(200)) + slideOutVertically()
            ) {
                val filters = SearchFilter.entries.toTypedArray()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    filters.forEachIndexed { index, filter ->
                        ToggleButton(
                            checked = filter == selectedFilter,
                            onCheckedChange = { viewModel.onFilterSelected(filter) },
                            modifier = Modifier.weight(1f),
                            shapes = when (index) {
                                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                filters.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                            }
                        ) {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = filter.name,
                                modifier = Modifier.size(ToggleButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                            Text(filter.name)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (query.isBlank()) {
                IdleSearchPrompt(modifier = Modifier.weight(1f))
            } else {
                SearchResultsContent(pagingItems = searchResults)
            }
        }
    }
}

@Composable
fun ExpressiveSearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
        trailingIcon = {
            // Show a clear button only when there is text
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                }
            }
        },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = keyboardActions
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SearchResultsContent(pagingItems: LazyPagingItems<SearchResult>) {
    // Handle the initial loading state (for the whole screen)
    when (val state = pagingItems.loadState.refresh) {
        is LoadState.Loading -> {
            Box(Modifier.fillMaxSize()) {
                CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
        is LoadState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: ${state.error.message}")
            }
        }
        else -> {
            if (pagingItems.itemCount == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        count = pagingItems.itemCount,
                        key = { index -> pagingItems.peek(index)?.id ?: index }
                    ) { index ->
                        val result = pagingItems[index]
                        if (result != null) {
                            SearchResultItem(result = result)
                        }
                    }

                    // Handle loading state for the next page (append)
                    when (val appendState = pagingItems.loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp)) {
                                    CircularWavyProgressIndicator(Modifier.align(Alignment.Center))
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item { Text("Error loading more: ${appendState.error.message}") }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IdleSearchPrompt(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_animation")

    // Animate a slow, gentle bobbing motion
    val verticalOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vertical_offset"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .graphicsLayer { translationY = verticalOffset * 1.2f }
                        .size(32.dp)
                        .alpha(0.8f),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .graphicsLayer { translationY = -verticalOffset * 1.2f }
                        .size(32.dp)
                        .alpha(0.8f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    painter = painterResource(id = R.drawable.spotify_small_logo_black),
                    contentDescription = "Spotify Logo",
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // The main text content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Deep Dive",
                    style = MaterialTheme.typography.headlineLargeEmphasized,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Find any artist or album",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // "Powered by Spotify" footer
            Text(
                text = "Powered by Spotify",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(0.7f)
            )
        }
    }
}

// TODO: Look into changing
@Composable
fun SearchResultItem(result: SearchResult) {
    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.medium),
        headlineContent = { Text(result.title, fontWeight = FontWeight.SemiBold) },
        supportingContent = { Text(result.subtitle) },
        leadingContent = {
            AsyncImage(
                model = result.imageUrl,
                contentDescription = "Image for ${result.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
        }
    )
}