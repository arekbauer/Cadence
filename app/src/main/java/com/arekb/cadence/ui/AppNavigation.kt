package com.arekb.cadence.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.ui.screens.artist.ArtistScreen
import com.arekb.cadence.ui.screens.genres.GenresScreen
import com.arekb.cadence.ui.screens.home.HomeScreen
import com.arekb.cadence.ui.screens.home.HomeViewModel
import com.arekb.cadence.ui.screens.login.LoginScreen
import com.arekb.cadence.ui.screens.login.LoginViewModel
import com.arekb.cadence.ui.screens.search.SearchScreen
import com.arekb.cadence.ui.screens.stats.artists.TopArtistsScreen
import com.arekb.cadence.ui.screens.stats.tracks.TopTracksScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    authRepository: AuthRepository,
    loginViewModel: LoginViewModel,
    onLoginRequested: () -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // Check the login state asynchronously
    LaunchedEffect(key1 = Unit) {
        coroutineScope.launch {
            if (authRepository.isLoggedIn()) {
                // If logged in, navigate to home screen
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
    }

    // Start navigation is always login, the LaunchedEffect will navigate to home if logged in
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginRequested = onLoginRequested,
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            val homeViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = homeViewModel,
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
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
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
        composable("top_artists"){
            TopArtistsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("analytics"){
            GenresScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable("search"){
            SearchScreen(
                onNavigateToArtist = { artistId ->
                    navController.navigate("artist/$artistId")
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        composable(
            route = "artist/{artistId}",
            arguments = listOf(navArgument("artistId") { type = NavType.StringType })
        ) {
            ArtistScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}