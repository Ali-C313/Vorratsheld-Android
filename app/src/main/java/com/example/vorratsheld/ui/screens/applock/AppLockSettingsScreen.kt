package com.example.vorratsheld.ui.screens.applock

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.BiometricResult
import com.example.vorratsheld.data.enums.BiometricType
import com.example.vorratsheld.data.enums.LockTimeout
import com.example.vorratsheld.data.enums.PinFlowMode
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.service.rememberBiometricAuthenticator
import com.example.vorratsheld.ui.components.SettingsItem
import com.example.vorratsheld.ui.screens.lock.PinFlowScreen
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AppLockViewModel
import com.example.vorratsheld.util.BiometricHelper
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.launch

// Einstellungen der App-Sperre: an/aus, Fingerabdruck, Zeitpunkt, PIN ändern
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(
    onBack: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    val authenticator = rememberBiometricAuthenticator()
    val biometricType = remember { authenticator.type }
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pinFlow by remember { mutableStateOf<PinFlowMode?>(null) }
    var showTimeoutDialog by remember { mutableStateOf(false) }

    val reason = stringResource(R.string.lock_biometric_reason)
    val usePin = stringResource(R.string.lock_use_pin)

    pinFlow?.let { mode ->
        PinFlowScreen(
            mode = mode,
            onFinished = {
                when (mode) {
                    PinFlowMode.CREATE -> viewModel.onPinCreated(withBiometrics = biometricType != BiometricType.NONE)
                    PinFlowMode.DISABLE -> viewModel.disable()
                    PinFlowMode.CHANGE -> Unit
                }
                pinFlow = null
            },
            onCancel = { pinFlow = null },
            viewModel = viewModel
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.applock_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
                    .size(88.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Icon(
                    imageVector = if (biometricType != BiometricType.NONE) BiometricHelper.icon(biometricType) else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }
            Text(
                text = stringResource(R.string.applock_intro),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                SwitchRow(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.applock_enable),
                    subtitle = stringResource(R.string.applock_enable_hint),
                    checked = uiState.enabled,
                    onCheckedChange = { on -> pinFlow = if (on) PinFlowMode.CREATE else PinFlowMode.DISABLE }
                )
            }

            if (uiState.enabled) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    if (biometricType != BiometricType.NONE) {
                        SwitchRow(
                            icon = BiometricHelper.icon(biometricType),
                            title = stringResource(R.string.applock_unlock_with, BiometricHelper.label(biometricType)),
                            subtitle = null,
                            checked = uiState.biometricsEnabled,
                            onCheckedChange = { on ->
                                if (on) {
                                    // Erst einmal bestätigen lassen, dass es wirklich klappt
                                    scope.launch {
                                        if (authenticator.authenticate(reason, usePin) == BiometricResult.SUCCESS) {
                                            viewModel.setBiometricsEnabled(true)
                                        }
                                    }
                                } else {
                                    viewModel.setBiometricsEnabled(false)
                                }
                            }
                        )
                    } else {
                        ListItem(
                            headlineContent = {
                                Text(
                                    text = stringResource(R.string.applock_no_biometrics),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        )
                    }
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Filled.Timer,
                        title = stringResource(R.string.applock_timeout),
                        subtitle = timeoutLabel(uiState.timeout),
                        onClick = { showTimeoutDialog = true }
                    )
                    HorizontalDivider()
                    SettingsItem(
                        icon = Icons.Filled.Password,
                        title = stringResource(R.string.applock_change_pin),
                        onClick = { pinFlow = PinFlowMode.CHANGE }
                    )
                }
            }
        }
    }

    if (showTimeoutDialog) {
        AlertDialog(
            onDismissRequest = { showTimeoutDialog = false },
            title = { Text(stringResource(R.string.applock_timeout)) },
            text = {
                Column {
                    LockTimeout.entries.forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = option == uiState.timeout,
                                    role = Role.RadioButton,
                                    onClick = {
                                        viewModel.setTimeout(option)
                                        showTimeoutDialog = false
                                    }
                                )
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = option == uiState.timeout, onClick = null)
                            Text(text = timeoutLabel(option), modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun AppLockSettingsScreenPreview() {
    VorratsheldTheme {
        AppLockSettingsScreen(onBack = {}, viewModel = AppLockViewModel(SettingsRepositoryMock()))
    }
}
