package com.humblecoders.matricareog

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MatricareApplication : Application() {

    // Use IO dispatcher for background operations and add proper lifecycle management
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance()

        Log.d("MatricareApp", "Application started — Firebase Auth initialized")

        // Initialize API-based chatbot in background
      //  initializeChatbot()
    }

}
