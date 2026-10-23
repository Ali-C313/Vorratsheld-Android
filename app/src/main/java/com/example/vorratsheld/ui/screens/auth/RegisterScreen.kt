package com.example.vorratsheld.ui.screens.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.repository.AuthRepositoryMock
import com.example.vorratsheld.data.repository.UserRepositoryMock
import com.example.vorratsheld.ui.components.AuthLayout
import com.example.vorratsheld.ui.components.AuthTextField
import com.example.vorratsheld.ui.components.PrimaryButton
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

// Registrierung mit Vor- und Nachname
@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    val nameRequiredText = stringResource(R.string.error_name_required)
    val tooShortText = stringResource(R.string.error_password_too_short)
    val mismatchText = stringResource(R.string.error_passwords_mismatch)

    // Sobald die Registrierung erfolgreich war, weiter zur Übersicht
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onRegistered()
        }
    }

    AuthLayout(
        title = stringResource(R.string.register_title),
        subtitle = stringResource(R.string.register_subtitle)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuthTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = stringResource(R.string.profile_first_name),
                icon = Icons.Filled.Person,
                modifier = Modifier.weight(1f)
            )
            AuthTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = stringResource(R.string.profile_last_name),
                icon = Icons.Filled.Badge,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = stringResource(R.string.email),
            icon = Icons.Filled.Email,
            keyboardType = KeyboardType.Email
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.password),
            icon = Icons.Filled.Lock,
            isPassword = true
        )
        Spacer(Modifier.height(12.dp))
        AuthTextField(
            value = passwordConfirm,
            onValueChange = { passwordConfirm = it },
            label = stringResource(R.string.register_password_confirm),
            icon = Icons.Filled.Lock,
            isPassword = true,
            imeAction = ImeAction.Done
        )

        // Eigene Prüfung hat Vorrang vor Firebase-Fehlern
        (localError ?: errorMessage)?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.register_button),
            isLoading = isLoading,
            onClick = {
                localError = when {
                    firstName.isBlank() || lastName.isBlank() -> nameRequiredText
                    password.length < 6 -> tooShortText
                    password != passwordConfirm -> mismatchText
                    else -> null
                }
                if (localError == null) {
                    authViewModel.register(firstName, lastName, email, password)
                }
            }
        )

        TextButton(
            onClick = {
                authViewModel.clearError()
                onBack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.register_to_login))
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    VorratsheldTheme {
        RegisterScreen(
            onRegistered = {},
            onBack = {},
            authViewModel = AuthViewModel(AuthRepositoryMock(loggedIn = false), UserRepositoryMock())
        )
    }
}
