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
import com.humblecoders.matricareog.DataStoreManager
import com.humblecoders.matricareog.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

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
    
    // Log DataStore changes
    LaunchedEffect(termsAccepted) {
        println("🔵 SplashScreen: termsAccepted changed to: $termsAccepted")
    }

    LaunchedEffect(Unit) {
        delay(1500)
    }

    val sessionChecked by authViewModel.sessionChecked.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(sessionChecked, currentUser, termsAccepted) {
        if (!sessionChecked) return@LaunchedEffect
        delay(800)

        when {
            currentUser != null -> onNavigateToHome()
            termsAccepted -> onNavigateToWelcome()
            else -> onNavigateToTerms()
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