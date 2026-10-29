package com.example.vorratsheld.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.vorratsheld.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Sprache und Onboarding
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    // null = Systemsprache
    private val _language = MutableStateFlow(settingsRepository.getLanguage())
    val language: StateFlow<String?> = _language.asStateFlow()

    fun selectLanguage(code: String?) {
        settingsRepository.setLanguage(code)
        _language.value = code
    }

    fun isOnboardingDone(): Boolean = settingsRepository.isOnboardingDone()

    fun finishOnboarding() {
        settingsRepository.setOnboardingDone()
    }
}
