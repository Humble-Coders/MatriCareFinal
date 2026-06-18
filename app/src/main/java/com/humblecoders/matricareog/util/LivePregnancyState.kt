package com.humblecoders.matricareog.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
//import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.Calendar

/**
 * Recomputes weeks pregnant when the screen resumes or the calendar day changes,
 * so the profile ring and week count stay in sync with real time.
 */
@Composable
fun rememberLiveWeeksPregnant(dueDate: String, storedWeeks: String): Int {
    var refreshTick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTick = System.currentTimeMillis()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val todayKey = remember(refreshTick) {
        val cal = Calendar.getInstance()
        cal.get(Calendar.YEAR) * 1000 + cal.get(Calendar.DAY_OF_YEAR)
    }

    return remember(dueDate, storedWeeks, todayKey) {
        PregnancyUtils.currentWeeksPregnant(dueDate, storedWeeks)
    }
}
