package com.arekb.cadence.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.ui.screens.genres.GenresScreen
import com.arekb.cadence.ui.screens.home.HomeScreen
import com.arekb.cadence.ui.screens.home.HomeViewEvent
import com.arekb.cadence.ui.screens.home.HomeViewModel
import com.arekb.cadence.ui.screens.login.LoginScreen
import com.arekb.cadence.ui.screens.login.LoginViewModel
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

    // Listen for the logout event from the HomeViewModel
    val homeViewModel: HomeViewModel = hiltViewModel()
    LaunchedEffect(Unit) {
        homeViewModel.eventFlow.collect { event ->
            when (event) {
                is HomeViewEvent.NavigateToLogin -> {
                    navController.navigate("login") {
                        // Clear the entire back stack so the user can't go back to the broken home screen
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
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
    }
}