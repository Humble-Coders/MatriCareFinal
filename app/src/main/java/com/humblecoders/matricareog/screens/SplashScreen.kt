package com.humblecoders.matricareog.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.humblecoders.matricareog.DataStoreManager
import com.humblecoders.matricareog.model.AuthResult
import com.humblecoders.matricareog.viewmodels.AuthViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToTerms: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val primaryPink = Color(0xFFE91E63)
    val dataStoreManager = remember { DataStoreManager(context) }
    val termsAccepted by dataStoreManager.termsAccepted.collectAsState(initial = false)

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val sessionChecked by authViewModel.sessionChecked.collectAsState()

    LaunchedEffect(sessionChecked, authState, currentUser, termsAccepted) {
        if (sessionChecked && authState != AuthResult.Idle) {
            Log.d(
                "SplashScreen",
                "navigate gate: checked=$sessionChecked auth=${authState::class.simpleName} user=${currentUser?.uid} terms=$termsAccepted"
            )
            when {
                currentUser != null -> {
                    Log.d("SplashScreen", "navigate: HOME")
                    onNavigateToHome()
                }
                termsAccepted -> {
                    Log.d("SplashScreen", "navigate: WELCOME")
                    onNavigateToWelcome()
                }
                else -> {
                    Log.d("SplashScreen", "navigate: TERMS")
                    onNavigateToTerms()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "Matri",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "care",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = primaryPink
            )
        }

        CircularProgressIndicator(
            color = primaryPink,
            modifier = Modifier.size(32.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading...",
            fontSize = 16.sp,
            color = Color.Gray
        )
    }
}
