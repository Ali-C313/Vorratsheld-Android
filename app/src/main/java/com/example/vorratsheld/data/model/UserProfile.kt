package com.example.vorratsheld.data.model

// Profildaten in Firestore unter users/{uid} (E-Mail kommt aus Firebase Auth)
data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    // Profilbild als Base64-JPEG (ca. 256 px), passt so in ein Firestore-Dokument
    val photoBase64: String? = null
)
