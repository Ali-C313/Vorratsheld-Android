package com.example.vorratsheld.ui.screens.productdetail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.StorageLocation
import com.example.vorratsheld.data.repository.AuthRepositoryMock
import com.example.vorratsheld.data.repository.OpenFoodFactsRepositoryMock
import com.example.vorratsheld.data.repository.ProductRepositoryMock
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.data.repository.ShoppingRepositoryMock
import com.example.vorratsheld.data.repository.UserRepositoryMock
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AuthViewModel
import com.example.vorratsheld.ui.viewmodel.ProductDetailViewModel
import com.example.vorratsheld.util.DateHelper
import org.koin.androidx.compose.koinViewModel

// Neues Produkt nach dem Scan anlegen oder bestehendes bearbeiten
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: ProductDetailViewModel = koinViewModel(),
    authViewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val errorText = uiState.errorRes?.let { stringResource(it, uiState.errorMessage.orEmpty()) } ?: uiState.errorMessage

    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Nach einem Barcode-Scan direkt mit dem MHD weitermachen
    var showExpiryScan by rememberSaveable { mutableStateOf(viewModel.isNewScan) }

    if (showExpiryScan) {
        ExpiryScanOverlay(
            onDateConfirmed = { date ->
                viewModel.updateExpiryDate(date)
                showExpiryScan = false
            },
            onManualPick = {
                showExpiryScan = false
                showDatePicker = true
            },
            onClose = { showExpiryScan = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) stringResource(R.string.product_edit_title) else stringResource(R.string.product_new_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (uiState.isEditMode) {
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.action_delete))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text(stringResource(R.string.product_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            Column {
                Text(text = stringResource(R.string.product_location), style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    FilterChip(
                        selected = uiState.location == StorageLocation.KUEHLSCHRANK,
                        onClick = { viewModel.updateLocation(StorageLocation.KUEHLSCHRANK) },
                        label = { Text(stringResource(R.string.storage_fridge)) }
                    )
                    FilterChip(
                        selected = uiState.location == StorageLocation.KAMMER,
                        onClick = { viewModel.updateLocation(StorageLocation.KAMMER) },
                        label = { Text(stringResource(R.string.storage_pantry)) }
                    )
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.product_quantity),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )
                FilledTonalIconButton(
                    onClick = { viewModel.changeQuantity(-1) },
                    enabled = uiState.quantity > 1
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                }
                Text(
                    text = uiState.quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                FilledTonalIconButton(onClick = { viewModel.changeQuantity(1) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                }
            }

            Column {
                Text(text = stringResource(R.string.product_expiry), style = MaterialTheme.typography.labelMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(DateHelper.formatDate(uiState.expiryDate), modifier = Modifier.padding(start = 8.dp))
                    }
                    FilledTonalButton(
                        onClick = { showExpiryScan = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.expiry_scan_button), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = stringResource(R.string.favorite),
                        tint = if (uiState.isFavorite) Color(0xFFFFA000) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(R.string.product_favorite_hint),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            errorText?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    currentUser?.let { user ->
                        viewModel.save(user.uid) { onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isEditMode) stringResource(R.string.action_save) else stringResource(R.string.action_add))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.expiryDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.updateExpiryDate(it) }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.action_apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.product_delete_title)) },
            text = { Text(stringResource(R.string.product_delete_text, uiState.name)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    currentUser?.let { user ->
                        viewModel.delete(user.uid) { onDeleted() }
                    }
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
private fun ProductDetailScreenPreview() {
    VorratsheldTheme {
        ProductDetailScreen(
            onBack = {},
            onDeleted = {},
            viewModel = ProductDetailViewModel(
                SavedStateHandle(mapOf("productId" to "1", "barcode" to "")),
                ProductRepositoryMock(),
                OpenFoodFactsRepositoryMock(),
                ShoppingRepositoryMock()
            ),
            authViewModel = AuthViewModel(AuthRepositoryMock(), UserRepositoryMock(), SettingsRepositoryMock())
        )
    }
}
