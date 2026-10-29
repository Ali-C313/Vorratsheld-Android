package com.example.vorratsheld.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vorratsheld.data.model.User
import com.example.vorratsheld.data.model.UserProfile
import com.example.vorratsheld.data.repository.AuthRepository
import com.example.vorratsheld.data.repository.SettingsRepository
import com.example.vorratsheld.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Hält den Login-Zustand für die Login/Register-Screens
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(authRepository.getCurrentUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Login-Zustand direkt von Firebase (AuthStateListener)
    val isLoggedIn: StateFlow<Boolean> = authRepository.authState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            authRepository.getCurrentUser() != null
        )

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Für den Lade-Spinner auf den Buttons
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.login(email.trim(), password)
                .onSuccess { user ->
                    _errorMessage.value = null
                    _currentUser.value = user
                }
                .onFailure { e -> _errorMessage.value = e.message }
            _isLoading.value = false
        }
    }

    // Name landet in Firebase Auth und im Firestore-Profil
    fun register(firstName: String, lastName: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val first = firstName.trim()
            val last = lastName.trim()
            authRepository.register(email.trim(), password, "$first $last")
                .onSuccess { user ->
                    // Falls das Profil nicht klappt, steht der Name trotzdem in Firebase Auth
                    userRepository.saveProfile(user.uid, UserProfile(firstName = first, lastName = last))
                    _errorMessage.value = null
                    _currentUser.value = user
                }
                .onFailure { e -> _errorMessage.value = e.message }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Navigation zum Login übernimmt der NavGraph, sobald isLoggedIn false wird
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // PIN gehört zum Konto, der nächste Nutzer legt sich einen eigenen an
            settingsRepository.clearAppLock()
            _currentUser.value = null
        }
    }
}
