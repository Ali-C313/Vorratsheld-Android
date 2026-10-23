package com.example.vorratsheld.data.remote.model

import com.example.vorratsheld.data.enums.StorageLocation
import com.example.vorratsheld.data.model.Product

// Produkt in Firestore, alle Felder brauchen Standardwerte für toObject()
data class ProductDto(
    val id: String = "",
    val userId: String = "",
    val barcode: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val location: String = StorageLocation.KUEHLSCHRANK.name,
    val expiryDate: Long = 0L,
    val quantity: Int = 1,
    // heißt in Firestore "favorite", im Product "isFavorite"
    val favorite: Boolean = false,
    val addedAt: Long = 0L
)

fun ProductDto.toProduct(): Product = Product(
    id = id,
    userId = userId,
    barcode = barcode,
    name = name,
    imageUrl = imageUrl,
    location = StorageLocation.entries.firstOrNull { it.name == location } ?: StorageLocation.KUEHLSCHRANK,
    expiryDate = expiryDate,
    quantity = quantity,
    isFavorite = favorite,
    addedAt = addedAt
)

fun Product.toDto(): ProductDto = ProductDto(
    id = id,
    userId = userId,
    barcode = barcode,
    name = name,
    imageUrl = imageUrl,
    location = location.name,
    expiryDate = expiryDate,
    quantity = quantity,
    favorite = isFavorite,
    addedAt = addedAt
)
