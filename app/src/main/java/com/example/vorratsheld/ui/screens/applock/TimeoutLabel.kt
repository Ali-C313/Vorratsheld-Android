package com.example.vorratsheld.ui.screens.applock

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.LockTimeout

// Anzeigetext für die Sperrzeit
@Composable
fun timeoutLabel(timeout: LockTimeout): String = stringResource(
    when (timeout) {
        LockTimeout.IMMEDIATELY -> R.string.timeout_immediately
        LockTimeout.ONE_MINUTE -> R.string.timeout_1min
        LockTimeout.FIVE_MINUTES -> R.string.timeout_5min
        LockTimeout.ON_START_ONLY -> R.string.timeout_start
    }
)
