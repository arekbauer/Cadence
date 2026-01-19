package com.arekb.cadence.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arekb.cadence.feature.analytics.artists.TopArtistsScreen
import com.arekb.cadence.feature.analytics.genres.GenresScreen
import com.arekb.cadence.feature.analytics.tracks.TopTracksScreen
import com.arekb.cadence.feature.artist.ArtistScreen
import com.arekb.cadence.feature.home.HomeScreen
import com.arekb.cadence.feature.login.LoginScreen
import com.arekb.cadence.feature.search.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val uriHandler = LocalUriHandler.current

    // Animations
    val animationSpec = tween<IntOffset>(durationMillis = 300)

    val slideIn = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = animationSpec
    )

    val slideOut = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = animationSpec
    )

    val popIn = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = animationSpec
    )

    val popOut = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = animationSpec
    )

    // Start navigation is always login, the LaunchedEffect will navigate to home if logged in
    NavHost(navController = navController, startDestination = "login") {
        composable(
            route = "login",
            enterTransition = { fadeIn(animationSpec = tween(700)) },
            exitTransition = { fadeOut(animationSpec = tween(700)) }
        ){
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "home",
            enterTransition = {
                // If coming from login, fade in. Otherwise, do nothing
                if (initialState.destination.route == "login") {
                    fadeIn(animationSpec = tween(700))
                } else {
                    null
                }
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(300))
            }
        ) {
            HomeScreen(
                onNavigateToMyTopTracks = {
                    navController.navigate("top_tracks")
                },
                onNavigateToMyTopArtists = {
                    navController.navigate("top_artists")
                },
                onNavigateToTopGenres = {
                    navController.navigate("analytics")
                },
                onNavigateToSearch = {
                    navController.navigate("search")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("top_tracks") {
            TopTracksScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("top_artists") {
            TopArtistsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("analytics") {
            GenresScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("search") {
            SearchScreen(
                onNavigateToArtist = { artistId ->
                    navController.navigate("artist/$artistId")
                },
                onNavigateToAlbum = { albumId ->
                    val spotifyUrl = "https://open.spotify.com/album/$albumId"
                    uriHandler.openUri(spotifyUrl)
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = "artist/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType }),
            enterTransition = { slideIn },
            exitTransition = { slideOut },
            popEnterTransition = { popIn },
            popExitTransition = { popOut }
        ) {
            ArtistScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}