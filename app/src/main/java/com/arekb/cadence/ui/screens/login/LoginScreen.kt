package com.arekb.cadence.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.arekb.cadence.R

//TODO: REVAMP!
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginRequested: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Listen for one-time events from the ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is LoginViewModel.LoginViewEvent.StartSdkLogin -> onLoginRequested()
                is LoginViewModel.LoginViewEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    Scaffold(Modifier.fillMaxSize()) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center)
            {
                Text("Cadence",
                    style = MaterialTheme.typography.displayLargeEmphasized
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Discover your music DNA",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Continue Button
                val size = ButtonDefaults.MediumContainerHeight
                Button(
                    onClick = { viewModel.onLoginClicked() },
                    contentPadding = ButtonDefaults.contentPaddingFor(size),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.spotify_small_logo_black),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.MediumIconSize),
                    )
                    Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(size)))
                    Text("Continue with Spotify", style = ButtonDefaults.textStyleFor(size))
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}