package com.example.vorratsheld.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.BiometricType

// Name und Icon für Fingerabdruck oder Gesichtserkennung
object BiometricHelper {

    // Name, wie ihn der Nutzer von seinem Gerät kennt
    @Composable
    fun label(type: BiometricType): String = stringResource(
        when (type) {
            BiometricType.FINGERPRINT -> R.string.biometric_fingerprint
            BiometricType.FACE -> R.string.biometric_face
            else -> R.string.biometric_generic
        }
    )

    fun icon(type: BiometricType): ImageVector = when (type) {
        BiometricType.FACE -> Icons.Filled.Face
        else -> Icons.Filled.Fingerprint
    }
}
