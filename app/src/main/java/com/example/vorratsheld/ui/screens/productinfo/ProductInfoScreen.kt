package com.example.vorratsheld.ui.screens.productinfo

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.repository.ProductRepositoryMock
import com.example.vorratsheld.data.repository.ShoppingRepositoryMock
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.ProductInfoViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// Produktansicht: lädt das Produkt bei jedem Betreten neu (z. B. nach dem Bearbeiten)
@Composable
fun ProductInfoScreen(
    onBack: () -> Unit,
    onEdit: (productId: String) -> Unit,
    viewModel: ProductInfoViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val product = uiState.product
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.load()
    }

    val errorText = uiState.errorRes?.let { stringResource(it, uiState.errorMessage.orEmpty()) } ?: uiState.errorMessage
    LaunchedEffect(errorText) {
        errorText?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // Header zeichnet selbst hinter die Statusleiste
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (product != null) {
                ActionBar(
                    onDeleteClick = { showDeleteConfirmation = true },
                    onAddToListClick = {
                        viewModel.addToShoppingList {
                            scope.launch {
                                snackbarHostState.showSnackbar(context.getString(R.string.info_added_to_list, product.name))
                            }
                        }
                    },
                    onEditClick = { onEdit(product.id) }
                )
            }
        }
    ) { innerPadding ->
        when {
            product == null && uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            product == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.error_product_not_found), style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .verticalScroll(rememberScrollState())
                ) {
                    InfoHeader(product = product, onBackClick = onBack, onFavoriteToggle = { viewModel.toggleFavorite() })
                    InfoBody(
                        product = product,
                        modifier = Modifier.offset(y = (-28).dp)
                    )
                }
            }
        }
    }

    if (showDeleteConfirmation && product != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.product_delete_title)) },
            text = { Text(stringResource(R.string.product_delete_text, product.name)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    viewModel.delete { onBack() }
                }) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun ProductInfoScreenPreview() {
    VorratsheldTheme {
        ProductInfoScreen(
            onBack = {},
            onEdit = {},
            viewModel = ProductInfoViewModel(
                SavedStateHandle(mapOf("productId" to "1")),
                ProductRepositoryMock(),
                ShoppingRepositoryMock()
            ).apply { load() }
        )
    }
}
