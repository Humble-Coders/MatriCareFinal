package com.humblecoders.matricareog.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.humblecoders.matricareog.DataStoreManager
import com.humblecoders.matricareog.viewmodels.AuthViewModel
import com.humblecoders.matricareog.model.AuthResult
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    onNavigateToTerms: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val primaryPink = Color(0xFFE91E63)
    val dataStoreManager = remember { DataStoreManager(context) }
    val termsAccepted by dataStoreManager.termsAccepted.collectAsState(initial = false)
    
    // Log DataStore changes
    LaunchedEffect(termsAccepted) {
        println("🔵 SplashScreen: termsAccepted changed to: $termsAccepted")
    }

    LaunchedEffect(Unit) {
        println("🔵 SplashScreen: Starting initial delay and auth check")
        delay(2000) // Show splash for 2 seconds
        println("🔵 SplashScreen: Delay completed, checking auth state")
        // Add a small delay to ensure DataStore is loaded
        delay(100)
        authViewModel.checkAuthState()
    }

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Single navigation logic that waits for everything to be ready
    LaunchedEffect(authState, currentUser, termsAccepted) {
        // Wait for DataStore to load and auth check to complete
        if (authState != AuthResult.Idle) {
            println("🔵 SplashScreen: Navigation logic triggered - termsAccepted=$termsAccepted, currentUser=${currentUser != null}, authState=$authState")
            
            when {
                currentUser != null -> {
                    println("🔵 SplashScreen: Navigating to HOME")
                    onNavigateToHome()
                }
                termsAccepted -> {
                    println("🔵 SplashScreen: Navigating to WELCOME")
                    onNavigateToWelcome()
                }
                else -> {
                    println("🔵 SplashScreen: Navigating to TERMS")
                    onNavigateToTerms()
                }
            }
        } else {
            println("🔵 SplashScreen: Waiting for auth check to complete - termsAccepted=$termsAccepted, currentUser=${currentUser != null}, authState=$authState")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
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

        // Loading indicator
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