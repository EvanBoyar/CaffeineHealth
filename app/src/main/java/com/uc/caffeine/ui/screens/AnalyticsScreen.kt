package com.uc.caffeine.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uc.caffeine.ui.screens.analytics.AnalyticsRoot
import com.uc.caffeine.ui.viewmodel.CaffeineViewModel

@Composable
fun AnalyticsScreen(
    viewModel: CaffeineViewModel = viewModel(),
) {
    AnalyticsRoot(viewModel = viewModel)
}