package com.example.vorratsheld.ui.state

import androidx.annotation.StringRes

// Zustand für Einstellungen (Profil-Card) und "Profil bearbeiten"
data class ProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",          // aktuelle Adresse aus Firebase Auth
    val newEmail: String = "",       // Eingabefeld, kann geändert werden
    val photoBase64: String? = null,
    val isSaving: Boolean = false,
    @StringRes val messageRes: Int? = null,   // Erfolgsmeldung für die Snackbar
    val messageArg: String = "",
    @StringRes val errorRes: Int? = null,
    val errorMessage: String? = null
) {
    val fullName: String get() = "$firstName $lastName".trim()
    val emailChanged: Boolean get() = newEmail.trim().isNotEmpty() && newEmail.trim() != email
}
