package com.example.vorratsheld.data.model

import com.example.vorratsheld.data.enums.StorageLocation
import com.example.vorratsheld.util.DateHelper

// Repräsentiert ein gescanntes/gespeichertes Produkt
data class Product(
    val id: String = "",
    val userId: String = "",           // Zuordnung zum eingeloggten Nutzer
    val barcode: String = "",
    val name: String = "",
    val imageUrl: String? = null,
    val location: StorageLocation = StorageLocation.KUEHLSCHRANK,
    val expiryDate: Long = 0L,          // Ablaufdatum als Timestamp (ms)
    val quantity: Int = 1,
    // Feld heißt in Firestore "favorite" (siehe ProductRepositoryImpl)
    val isFavorite: Boolean = false,    // Landet bei Verbrauch auf Einkaufsliste
    val addedAt: Long = DateHelper.nowMillis()
)
