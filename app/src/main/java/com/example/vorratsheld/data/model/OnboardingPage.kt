package com.example.vorratsheld.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color

// Inhalt einer Onboarding-Seite
data class OnboardingPage(
    @StringRes val title: Int,
    @StringRes val text: Int,
    val accent: Color
)
