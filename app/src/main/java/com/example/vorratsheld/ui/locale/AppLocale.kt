package com.example.vorratsheld.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

// Vom Nutzer gewählte Sprache (z. B. "en"), null = Systemsprache
var customAppLocale by mutableStateOf<String?>(null)

// Setzt die gewählte Sprache in der Configuration, damit stringResource() die richtigen Texte liefert
object LocalAppLocale {
    private var default: Locale? = null

    val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = LocalConfiguration.current

        if (default == null) {
            default = Locale.getDefault()
        }

        val new = if (value == null) default!! else Locale.forLanguageTag(value)
        Locale.setDefault(new)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources

        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}

// Um die ganze App legen: Sprachwechsel baut alles neu auf, Arabisch läuft von rechts nach links
@Composable
fun AppEnvironment(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalAppLocale provides customAppLocale) {
        val isRtl = LocalAppLocale.current.startsWith("ar")
        CompositionLocalProvider(
            LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        ) {
            key(customAppLocale) {
                content()
            }
        }
    }
}
