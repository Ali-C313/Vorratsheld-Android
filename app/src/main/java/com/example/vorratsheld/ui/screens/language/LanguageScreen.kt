package com.example.vorratsheld.ui.screens.language

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.AppLanguage
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.ui.locale.customAppLocale
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

// Sprache speichern und direkt übernehmen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val selectLanguage: (String?) -> Unit = { code ->
        viewModel.selectLanguage(code)
        customAppLocale = code
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.language_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                LanguageRow(
                    name = stringResource(R.string.language_system),
                    selected = customAppLocale == null,
                    onClick = { selectLanguage(null) }
                )
                HorizontalDivider()
            }
            items(AppLanguage.entries) { language ->
                LanguageRow(
                    name = language.nativeName,
                    selected = customAppLocale == language.code,
                    onClick = { selectLanguage(language.code) }
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun LanguageScreenPreview() {
    VorratsheldTheme {
        LanguageScreen(onBack = {}, viewModel = SettingsViewModel(SettingsRepositoryMock()))
    }
}
