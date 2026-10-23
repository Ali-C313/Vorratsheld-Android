package com.example.vorratsheld.ui.screens.auth

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
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

// Anmeldung mit E-Mail und Passwort
@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onNavigateToRegister: () -> Unit,
    authViewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()

    // Sobald der Login erfolgreich war, weiter zur Übersicht
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            onLoggedIn()
        }
    }

    AuthLayout(
        title = stringResource(R.string.login_title),
        subtitle = stringResource(R.string.login_subtitle)
    ) {
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
            isPassword = true,
            imeAction = ImeAction.Done
        )

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = stringResource(R.string.login_button),
            isLoading = isLoading,
            onClick = { authViewModel.login(email, password) }
        )

        TextButton(
            onClick = {
                authViewModel.clearError()
                onNavigateToRegister()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.login_to_register))
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    VorratsheldTheme {
        LoginScreen(
            onLoggedIn = {},
            onNavigateToRegister = {},
            authViewModel = AuthViewModel(AuthRepositoryMock(loggedIn = false), UserRepositoryMock())
        )
    }
}
