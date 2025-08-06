package com.arekb.cadence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.arekb.cadence.data.repository.AuthRepository
import com.arekb.cadence.ui.AppNavigation
import com.arekb.cadence.ui.screens.login.LoginViewModel
import com.arekb.cadence.ui.theme.CadenceTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository
    private val loginViewModel: LoginViewModel by viewModels()

    private val spotifyAuthLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val response = AuthorizationClient.getResponse(result.resultCode, result.data)
        when (response.type) {
            AuthorizationResponse.Type.CODE -> response.code?.let { loginViewModel.onAuthCodeReceived(it) }
            AuthorizationResponse.Type.ERROR -> { /* Handle error */ }
            else -> { /* Handle other cases */ }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CadenceTheme {
                AppNavigation(
                    loginViewModel = loginViewModel,
                    onLoginRequested = { launchSpotifyLogin() }
                )
            }
        }
    }

    private fun launchSpotifyLogin() {
        val intent = AuthorizationClient.createLoginActivityIntent(this, authRepository.getAuthorizationRequest())
        spotifyAuthLauncher.launch(intent)
    }
}
