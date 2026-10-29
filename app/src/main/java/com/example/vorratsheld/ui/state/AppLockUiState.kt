package com.example.vorratsheld.ui.state

import com.example.vorratsheld.data.enums.LockTimeout

// Zustand der App-Sperre für Einstellungen, Sperrbildschirm und PIN-Eingabe
data class AppLockUiState(
    val enabled: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val timeout: LockTimeout = LockTimeout.ONE_MINUTE
)
