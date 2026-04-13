package com.uc.caffeine.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay

@Stable
class AppHaptics(
    private val view: View,
) {
    fun navigation() {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun toggle() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    fun confirm() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun tick() {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    suspend fun celebration() {
        confirm()
        delay(150)
        tick()
        delay(100)
        tick()
    }
}

@Composable
fun rememberAppHaptics(): AppHaptics {
    val view = LocalView.current
    return remember(view) {
        AppHaptics(view)
    }
}
