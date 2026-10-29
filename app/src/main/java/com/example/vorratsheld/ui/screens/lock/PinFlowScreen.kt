package com.example.vorratsheld.ui.screens.lock

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.PinFlowMode
import com.example.vorratsheld.data.enums.PinStep
import com.example.vorratsheld.data.local.AppLockSettings
import com.example.vorratsheld.data.repository.SettingsRepositoryMock
import com.example.vorratsheld.ui.components.PinPad
import com.example.vorratsheld.ui.theme.LockGradient
import com.example.vorratsheld.ui.theme.VorratsheldTheme
import com.example.vorratsheld.ui.viewmodel.AppLockViewModel
import org.koin.androidx.compose.koinViewModel

// PIN festlegen, ändern oder zum Ausschalten bestätigen
@Composable
fun PinFlowScreen(
    mode: PinFlowMode,
    onFinished: () -> Unit,
    onCancel: () -> Unit,
    viewModel: AppLockViewModel = koinViewModel()
) {
    var step by remember { mutableStateOf(if (mode == PinFlowMode.CREATE) PinStep.NEW else PinStep.VERIFY) }
    var entered by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var shakeTrigger by remember { mutableIntStateOf(0) }
    var busy by remember { mutableStateOf(false) }

    val wrongPin = stringResource(R.string.lock_wrong_pin)
    val mismatch = stringResource(R.string.pin_mismatch)

    if (!LocalInspectionMode.current) {
        BackHandler(onBack = onCancel)
    }

    fun fail(message: String) {
        errorText = message
        shakeTrigger += 1
        entered = ""
    }

    fun onComplete(pin: String) {
        when (step) {
            PinStep.VERIFY -> {
                busy = true
                viewModel.checkPin(pin) { correct ->
                    busy = false
                    when {
                        !correct -> fail(wrongPin)
                        mode == PinFlowMode.DISABLE -> onFinished()
                        else -> {
                            step = PinStep.NEW
                            entered = ""
                        }
                    }
                }
            }
            PinStep.NEW -> {
                firstPin = pin
                step = PinStep.CONFIRM
                entered = ""
            }
            PinStep.CONFIRM -> {
                if (pin != firstPin) {
                    step = PinStep.NEW
                    fail(mismatch)
                } else {
                    busy = true
                    viewModel.setPin(pin) {
                        busy = false
                        onFinished()
                    }
                }
            }
        }
    }

    val (title, subtitle) = when (step) {
        PinStep.VERIFY -> stringResource(R.string.pin_current) to stringResource(R.string.pin_current_hint)
        PinStep.NEW -> stringResource(R.string.pin_create) to stringResource(R.string.pin_create_hint)
        PinStep.CONFIRM -> stringResource(R.string.pin_confirm) to stringResource(R.string.pin_confirm_hint)
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2F5530)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LockGradient)
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(8.dp)
            ) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_cancel), tint = Color(0xFFFBF6EA))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.Center)
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 56.dp, bottom = 24.dp)
            ) {
                PinPad(
                    title = title,
                    subtitle = subtitle,
                    entered = entered,
                    onDigit = { digit ->
                        if (!busy && entered.length < AppLockSettings.PIN_LENGTH) {
                            entered += digit
                            errorText = null
                            if (entered.length == AppLockSettings.PIN_LENGTH) onComplete(entered)
                        }
                    },
                    onDelete = {
                        entered = entered.dropLast(1)
                        errorText = null
                    },
                    errorText = errorText,
                    shakeTrigger = shakeTrigger,
                    enabled = !busy
                )
            }
        }
    }
}

@SuppressLint("ViewModelConstructorInComposable")
@Preview(showBackground = true)
@Composable
private fun PinFlowScreenPreview() {
    VorratsheldTheme {
        PinFlowScreen(
            mode = PinFlowMode.CREATE,
            onFinished = {},
            onCancel = {},
            viewModel = AppLockViewModel(SettingsRepositoryMock())
        )
    }
}
