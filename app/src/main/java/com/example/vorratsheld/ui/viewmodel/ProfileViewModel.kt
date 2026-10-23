package com.example.vorratsheld.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vorratsheld.R
import com.example.vorratsheld.data.model.UserProfile
import com.example.vorratsheld.data.repository.AuthRepository
import com.example.vorratsheld.data.repository.ProductRepository
import com.example.vorratsheld.data.repository.ShoppingRepository
import com.example.vorratsheld.data.repository.UserRepository
import com.example.vorratsheld.data.settings.AppLockSettings
import com.example.vorratsheld.ui.state.ProfileUiState
import kotlin.io.encoding.Base64
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Profil, E-Mail, Passwort und Konto löschen
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val user = authRepository.getCurrentUser() ?: return
        // Fallback aus Firebase Auth, falls Firestore (noch) nichts liefert
        val fallbackFirst = user.displayName.substringBefore(" ").trim()
        val fallbackLast = user.displayName.substringAfter(" ", "").trim()

        viewModelScope.launch {
            userRepository.getProfile(user.uid)
                .onSuccess { profile ->
                    _uiState.value = _uiState.value.copy(
                        firstName = profile.firstName.ifBlank { fallbackFirst },
                        lastName = profile.lastName.ifBlank { fallbackLast },
                        photoBase64 = profile.photoBase64,
                        email = user.email,
                        newEmail = user.email,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        firstName = fallbackFirst,
                        lastName = fallbackLast,
                        email = user.email,
                        newEmail = user.email,
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
        }
    }

    fun updateFirstName(value: String) {
        _uiState.value = _uiState.value.copy(firstName = value)
    }

    fun updateLastName(value: String) {
        _uiState.value = _uiState.value.copy(lastName = value)
    }

    fun updateEmail(value: String) {
        _uiState.value = _uiState.value.copy(newEmail = value)
    }

    // Bild kommt schon verkleinert aus dem Screen (ImageHelper)
    fun setPhoto(bytes: ByteArray) {
        _uiState.value = _uiState.value.copy(photoBase64 = Base64.encode(bytes))
    }

    fun removePhoto() {
        _uiState.value = _uiState.value.copy(photoBase64 = null)
    }

    fun showError(message: String) {
        _uiState.value = _uiState.value.copy(errorMessage = message)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(messageRes = null, messageArg = "", errorRes = null, errorMessage = null)
    }

    // currentPassword wird nur gebraucht, wenn sich die E-Mail ändert
    fun saveProfile(currentPassword: String) {
        val user = authRepository.getCurrentUser() ?: return
        val state = _uiState.value

        viewModelScope.launch {
            if (state.emailChanged && currentPassword.isBlank()) {
                _uiState.value = state.copy(errorRes = R.string.error_password_required)
                return@launch
            }

            _uiState.value = state.copy(isSaving = true)
            try {
                // Name auch in Firebase Auth speichern, falls Firestore nicht klappt
                authRepository.updateDisplayName(state.fullName).getOrThrow()
                userRepository.saveProfile(
                    user.uid,
                    UserProfile(
                        firstName = state.firstName.trim(),
                        lastName = state.lastName.trim(),
                        photoBase64 = state.photoBase64
                    )
                ).getOrThrow()

                if (state.emailChanged) {
                    val newEmail = state.newEmail.trim()
                    authRepository.changeEmail(currentPassword, newEmail).getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        messageRes = R.string.profile_email_verification_sent,
                        messageArg = newEmail
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, messageRes = R.string.profile_saved)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }

    // onSuccess leert die Passwortfelder im Screen
    fun changePassword(currentPassword: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(messageRes = R.string.profile_password_changed)
                    onSuccess()
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(errorMessage = e.message)
                }
        }
    }

    // Reihenfolge: Passwort prüfen -> Produkte, Einkaufsliste + Profil löschen -> Account löschen
    // Danach schickt der NavGraph zum Login (isLoggedIn wird false)
    fun deleteAccount(currentPassword: String) {
        val user = authRepository.getCurrentUser() ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                authRepository.confirmPassword(currentPassword).getOrThrow()
                productRepository.deleteAllProducts(user.uid).getOrThrow()
                shoppingRepository.deleteAllItems(user.uid).getOrThrow()
                userRepository.deleteProfile(user.uid).getOrThrow()
                authRepository.deleteUser().getOrThrow()
                AppLockSettings.clear()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }
}
