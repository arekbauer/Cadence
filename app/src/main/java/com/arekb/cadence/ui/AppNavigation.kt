package com.arekb.cadence.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arekb.cadence.ui.screens.login.LoginScreen
import com.arekb.cadence.ui.screens.login.LoginViewModel

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    onLoginRequested: () -> Unit
) {
    val navController = rememberNavController()
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
            // Your main app screen will go here
            androidx.compose.material3.Text("Welcome Home!")
        }
    }
}