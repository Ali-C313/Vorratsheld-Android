package com.example.vorratsheld.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.vorratsheld.R
import com.example.vorratsheld.data.repository.ProductRepository
import com.example.vorratsheld.data.repository.ShoppingRepository
import com.example.vorratsheld.ui.navigation.Screen
import com.example.vorratsheld.ui.state.ProductInfoUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Nur-Lesen-Ansicht eines Produkts mit ein paar Schnellaktionen
class ProductInfoViewModel(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    // productId kommt aus der Route
    private val productId = savedStateHandle.toRoute<Screen.ProductInfo>().productId

    private val _uiState = MutableStateFlow(ProductInfoUiState())
    val uiState: StateFlow<ProductInfoUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            productRepository.getProductById(productId)
                .onSuccess { product ->
                    val notFound = if (product == null) R.string.error_product_not_found else null
                    _uiState.update { it.copy(product = product, isLoading = false, errorRes = notFound) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
        }
    }

    fun toggleFavorite() {
        val product = _uiState.value.product ?: return
        val updated = product.copy(isFavorite = !product.isFavorite)
        _uiState.update { it.copy(product = updated) }
        viewModelScope.launch {
            productRepository.updateProduct(updated)
                .onFailure { e ->
                    _uiState.update { it.copy(product = product, errorMessage = e.message) }
                }
        }
    }

    fun addToShoppingList(onAdded: () -> Unit) {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            shoppingRepository.addFromProduct(product)
                .onSuccess { onAdded() }
                .onFailure { e -> _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            productRepository.deleteProduct(product.id)
                .onSuccess {
                    if (product.isFavorite) shoppingRepository.addFromProduct(product)
                    onDeleted()
                }
                .onFailure { e ->
                    _uiState.update { it.copy(errorRes = R.string.error_delete_failed, errorMessage = e.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorRes = null, errorMessage = null) }
    }
}
