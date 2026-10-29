package com.example.vorratsheld.ui.screens.lock

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.BiometricResult
import com.example.vorratsheld.data.enums.BiometricType
import com.example.vorratsheld.data.local.AppLockSettings
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.service.rememberBiometricAuthenticator
import com.example.vorratsheld.ui.components.PadIconKey
import com.example.vorratsheld.ui.components.PinPad
import com.example.vorratsheld.ui.components.VorratsheldLogo
import com.example.vorratsheld.ui.theme.LockGradient
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AppLockViewModel
import com.example.vorratsheld.util.BiometricHelper
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ATTEMPTS_BEFORE_WAIT = 5

// Liegt über der ganzen App, bis Fingerabdruck oder der PIN stimmt
@Composable
fun LockScreen(
    onUnlocked: () -> Unit,
    onForgotPin: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    val authenticator = rememberBiometricAuthenticator()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val biometricType = remember(uiState.biometricsEnabled) {
        if (uiState.biometricsEnabled) authenticator.type else BiometricType.NONE
    }
    val scope = rememberCoroutineScope()

    var entered by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var waitSeconds by remember { mutableIntStateOf(0) }
    var checking by remember { mutableStateOf(false) }
    var showForgotDialog by remember { mutableStateOf(false) }

    val reason = stringResource(R.string.lock_biometric_reason)
    val usePin = stringResource(R.string.lock_use_pin)
    val wrongPin = stringResource(R.string.lock_wrong_pin)

    fun askBiometric() {
        scope.launch {
            if (authenticator.authenticate(reason, usePin) == BiometricResult.SUCCESS) onUnlocked()
        }
    }

    // Fingerabdruck gleich beim Erscheinen anbieten
    LaunchedEffect(Unit) {
        if (biometricType != BiometricType.NONE) askBiometric()
    }

    // Nach zu vielen Fehlversuchen herunterzählen
    LaunchedEffect(waitSeconds) {
        if (waitSeconds > 0) {
            delay(1000)
            waitSeconds -= 1
        }
    }

    // Zurück darf die Sperre nicht umgehen (in der Preview gibt es kein Zurück)
    if (!LocalInspectionMode.current) {
        BackHandler(onBack = {})
    }

    fun onDigit(digit: Char) {
        if (checking || entered.length >= AppLockSettings.PIN_LENGTH) return
        entered += digit
        errorText = null
        if (entered.length == AppLockSettings.PIN_LENGTH) {
            val pin = entered
            checking = true
            viewModel.checkPin(pin) { correct ->
                checking = false
                if (correct) {
                    onUnlocked()
                } else {
                    failedAttempts += 1
                    shakeTrigger += 1
                    entered = ""
                    errorText = wrongPin
                    if (failedAttempts % ATTEMPTS_BEFORE_WAIT == 0) {
                        // Jede weitere Runde Fehlversuche wartet länger
                        waitSeconds = 30 * (failedAttempts / ATTEMPTS_BEFORE_WAIT)
                    }
                }
            }
        }
    }

    // Surface fängt alle Berührungen ab, darunter bleibt nichts bedienbar
    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2F5530)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(LockGradient)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(vertical = 24.dp)
            ) {
                VorratsheldLogo(modifier = Modifier.size(96.dp))
                Spacer(Modifier.height(20.dp))

                PinPad(
                    title = stringResource(R.string.lock_title),
                    subtitle = stringResource(R.string.lock_enter_pin),
                    entered = entered,
                    onDigit = ::onDigit,
                    onDelete = {
                        entered = entered.dropLast(1)
                        errorText = null
                    },
                    errorText = if (waitSeconds > 0) stringResource(R.string.lock_wait, waitSeconds) else errorText,
                    shakeTrigger = shakeTrigger,
                    enabled = waitSeconds == 0 && !checking,
                    extraKey = if (biometricType != BiometricType.NONE) {
                        {
                            PadIconKey(
                                icon = BiometricHelper.icon(biometricType),
                                description = BiometricHelper.label(biometricType),
                                onClick = ::askBiometric
                            )
                        }
                    } else {
                        null
                    }
                )

                TextButton(
                    onClick = { showForgotDialog = true },
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text(stringResource(R.string.lock_forgot_pin), color = Color(0xFFFBF6EA))
                }
            }
        }
    }

    if (showForgotDialog) {
        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = { Text(stringResource(R.string.lock_forgot_pin)) },
            text = { Text(stringResource(R.string.lock_forgot_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showForgotDialog = false
                    onForgotPin()
                }) {
                    Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun LockScreenPreview() {
    VorratsheldTheme {
        LockScreen(
            onUnlocked = {},
            onForgotPin = {},
            viewModel = AppLockViewModel(SettingsRepositoryMock())
        )
    }
}
