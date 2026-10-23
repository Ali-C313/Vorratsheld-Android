package com.example.vorratsheld.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.vorratsheld.R

// "Läuft morgen ab", "Seit 2 Tagen abgelaufen" usw.
@Composable
fun expiryLabel(daysLeft: Long): String = when {
    daysLeft < 0 -> stringResource(R.string.expiry_expired_days, -daysLeft)
    daysLeft == 0L -> stringResource(R.string.expiry_today)
    daysLeft == 1L -> stringResource(R.string.expiry_tomorrow)
    else -> stringResource(R.string.expiry_in_days, daysLeft)
}
