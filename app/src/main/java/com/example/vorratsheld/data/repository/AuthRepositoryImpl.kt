package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.model.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Anmeldung und Konto über Firebase Auth
class AuthRepositoryImpl(
    private val auth: FirebaseAuth
) : AuthRepository {

    override val authState: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: ""
        )
    }

    override suspend fun login(email: String, password: String): Result<User> = runCatching {
        val firebaseUser = auth.signInWithEmailAndPassword(email, password).await().user
            ?: throw Exception("Login fehlgeschlagen")
        User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
    }

    override suspend fun register(email: String, password: String, displayName: String): Result<User> = runCatching {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user
            ?: throw Exception("Registrierung fehlgeschlagen")
        firebaseUser.updateProfile(nameChange(displayName)).await()
        User(uid = firebaseUser.uid, email = firebaseUser.email ?: "", displayName = displayName)
    }

    override suspend fun updateDisplayName(displayName: String): Result<Unit> = runCatching {
        auth.currentUser?.updateProfile(nameChange(displayName))?.await()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        reauthenticate(currentPassword)
        auth.currentUser?.updatePassword(newPassword)?.await()
    }

    override suspend fun changeEmail(currentPassword: String, newEmail: String): Result<Unit> = runCatching {
        reauthenticate(currentPassword)
        auth.currentUser?.verifyBeforeUpdateEmail(newEmail)?.await()
    }

    override suspend fun confirmPassword(currentPassword: String): Result<Unit> = runCatching {
        reauthenticate(currentPassword)
    }

    override suspend fun deleteUser(): Result<Unit> = runCatching {
        auth.currentUser?.delete()?.await()
    }

    override suspend fun logout(): Result<Unit> = runCatching {
        auth.signOut()
    }

    // Firebase verlangt vor Passwort-, E-Mail-Änderung und Löschen eine frische Anmeldung
    private suspend fun reauthenticate(currentPassword: String) {
        val firebaseUser = auth.currentUser ?: throw Exception("Nicht eingeloggt")
        val email = firebaseUser.email ?: throw Exception("Keine E-Mail-Adresse hinterlegt")
        firebaseUser.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword)).await()
    }

    private fun nameChange(displayName: String) = UserProfileChangeRequest.Builder()
        .setDisplayName(displayName)
        .build()
}
