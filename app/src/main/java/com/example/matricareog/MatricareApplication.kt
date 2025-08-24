package com.example.matricareog

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel

class MatricareApplication : Application() {

    // Use IO dispatcher for background operations and add proper lifecycle management
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        Log.d("MatricareApp", "🚀 Application starting - initializing API-based chatbot...")

        // Initialize API-based chatbot in background
      //  initializeChatbot()
    }

}
