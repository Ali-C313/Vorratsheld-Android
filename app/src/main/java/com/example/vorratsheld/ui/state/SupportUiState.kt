package com.example.vorratsheld.ui.state

import androidx.annotation.StringRes
import com.example.vorratsheld.data.enums.SupportCategory

// Zustand des Hilfe-Formulars
data class SupportUiState(
    val category: SupportCategory = SupportCategory.PROBLEM,
    val subject: String = "",
    val message: String = "",
    val replyEmail: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false,
    @StringRes val errorRes: Int? = null,
    val errorMessage: String? = null
)
