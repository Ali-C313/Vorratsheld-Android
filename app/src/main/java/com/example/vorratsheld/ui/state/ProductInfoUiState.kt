package com.example.vorratsheld.ui.state

import androidx.annotation.StringRes
import com.example.vorratsheld.data.model.Product

// Zustand der Produktansicht
data class ProductInfoUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    @StringRes val errorRes: Int? = null,
    val errorMessage: String? = null
)
