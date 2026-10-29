package com.example.vorratsheld.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.AppLanguage
import com.example.vorratsheld.data.enums.LegalType
import com.example.vorratsheld.data.repository.AuthRepositoryMock
import com.example.vorratsheld.data.repository.ProductRepositoryMock
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.data.repository.ShoppingRepositoryMock
import com.example.vorratsheld.data.repository.UserRepositoryMock
import com.example.vorratsheld.ui.components.SettingsItem
import com.example.vorratsheld.ui.components.SettingsSectionTitle
import com.example.vorratsheld.ui.locale.customAppLocale
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AppLockViewModel
import com.example.vorratsheld.ui.viewmodel.AuthViewModel
import com.example.vorratsheld.ui.viewmodel.ProfileViewModel
import com.example.vorratsheld.util.AppInfo
import org.koin.androidx.compose.koinViewModel

// Einstellungen: Profil, Sprache, App-Sperre, Hilfe, Rechtliches, Abmelden
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToAppLock: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToLegal: (LegalType) -> Unit,
    authViewModel: AuthViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    appLockViewModel: AppLockViewModel = koinViewModel()
) {
    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val appLockState by appLockViewModel.uiState.collectAsStateWithLifecycle()

    // Nach "Profil bearbeiten" bzw. "App-Sperre" die Anzeige aktualisieren
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        profileViewModel.loadProfile()
        appLockViewModel.refresh()
    }

    val currentLanguage = AppLanguage.entries.firstOrNull { it.code == customAppLocale }?.nativeName
        ?: stringResource(R.string.language_system)

    var showLogoutConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
            ProfileCard(state = profileState, onClick = onNavigateToProfile)

            SettingsSectionTitle(
                text = stringResource(R.string.settings_section_app),
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingsItem(
                    icon = Icons.Filled.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = currentLanguage,
                    onClick = onNavigateToLanguage
                )
            }

            SettingsSectionTitle(
                text = stringResource(R.string.settings_section_security),
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingsItem(
                    icon = Icons.Filled.Lock,
                    title = stringResource(R.string.settings_app_lock),
                    subtitle = stringResource(if (appLockState.enabled) R.string.state_on else R.string.state_off),
                    onClick = onNavigateToAppLock
                )
            }

            SettingsSectionTitle(
                text = stringResource(R.string.settings_section_support),
                modifier = Modifier.padding(top = 8.dp)
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    title = stringResource(R.string.settings_help),
                    onClick = onNavigateToHelp
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.Description,
                    title = stringResource(R.string.settings_terms),
                    onClick = { onNavigateToLegal(LegalType.TERMS) }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.PrivacyTip,
                    title = stringResource(R.string.settings_privacy),
                    onClick = { onNavigateToLegal(LegalType.PRIVACY) }
                )
                HorizontalDivider()
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = stringResource(R.string.settings_imprint),
                    onClick = { onNavigateToLegal(LegalType.IMPRINT) }
                )
            }

            OutlinedButton(
                onClick = { showLogoutConfirmation = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(R.string.settings_logout))
            }

            Text(
                text = stringResource(R.string.settings_version, AppInfo.VERSION),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text(stringResource(R.string.logout_dialog_title)) },
            text = { Text(stringResource(R.string.logout_dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirmation = false
                    authViewModel.logout()
                }) {
                    Text(stringResource(R.string.settings_logout), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    VorratsheldTheme {
        SettingsScreen(
            onBack = {},
            onNavigateToProfile = {},
            onNavigateToLanguage = {},
            onNavigateToAppLock = {},
            onNavigateToHelp = {},
            onNavigateToLegal = {},
            authViewModel = AuthViewModel(AuthRepositoryMock(), UserRepositoryMock(), SettingsRepositoryMock()),
            profileViewModel = ProfileViewModel(AuthRepositoryMock(), UserRepositoryMock(), ProductRepositoryMock(), ShoppingRepositoryMock(), SettingsRepositoryMock()),
            appLockViewModel = AppLockViewModel(SettingsRepositoryMock())
        )
    }
}
