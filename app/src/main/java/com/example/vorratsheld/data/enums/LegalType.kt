package com.example.vorratsheld.data.enums

import androidx.annotation.StringRes
import com.example.vorratsheld.R

// Rechtstexte mit Titel und Inhalt aus den Ressourcen
enum class LegalType(@StringRes val title: Int, @StringRes val text: Int) {
    TERMS(R.string.settings_terms, R.string.legal_terms_text),
    PRIVACY(R.string.settings_privacy, R.string.legal_privacy_text),
    IMPRINT(R.string.settings_imprint, R.string.legal_imprint_text)
}
