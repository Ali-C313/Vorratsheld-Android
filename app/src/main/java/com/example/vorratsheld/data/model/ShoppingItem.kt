package com.example.vorratsheld.data.model

import com.example.vorratsheld.util.DateHelper

// Eintrag auf der Einkaufsliste, unabhängig vom Vorrat
data class ShoppingItem(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val checked: Boolean = false,
    val imageUrl: String? = null,
    val addedAt: Long = DateHelper.nowMillis()
)
