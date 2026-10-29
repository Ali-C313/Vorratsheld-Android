package com.example.vorratsheld.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vorratsheld.data.enums.LockTimeout
import com.example.vorratsheld.data.repository.SettingsRepository
import com.example.vorratsheld.ui.state.AppLockUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// App-Sperre: an/aus, Fingerabdruck, Sperrzeit und PIN prüfen oder festlegen
class AppLockViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(currentState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()

    // z. B. nach der Rückkehr aus den Einstellungen
    fun refresh() {
        _uiState.value = currentState()
    }

    // withBiometrics: Wer Fingerabdruck hat, will ihn meistens auch nutzen
    fun onPinCreated(withBiometrics: Boolean) {
        if (withBiometrics) settingsRepository.setBiometricsEnabled(true)
        refresh()
    }

    fun disable() {
        settingsRepository.clearAppLock()
        refresh()
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        settingsRepository.setBiometricsEnabled(enabled)
        refresh()
    }

    fun setTimeout(timeout: LockTimeout) {
        settingsRepository.setLockTimeout(timeout)
        refresh()
    }

    fun checkPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            onResult(settingsRepository.checkPin(pin))
        }
    }

    fun setPin(pin: String, onDone: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.setPin(pin)
            refresh()
            onDone()
        }
    }

    private fun currentState() = AppLockUiState(
        enabled = settingsRepository.isAppLockEnabled(),
        biometricsEnabled = settingsRepository.isBiometricsEnabled(),
        timeout = settingsRepository.getLockTimeout()
    )
}
