package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.enums.LockTimeout
import com.example.vorratsheld.data.local.AppLockSettings
import com.example.vorratsheld.data.local.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Einstellungen aus dem DataStore (über AppSettings und AppLockSettings)
class SettingsRepositoryImpl : SettingsRepository {

    override fun getLanguage(): String? = AppSettings.language

    override fun setLanguage(code: String?) {
        AppSettings.language = code
    }

    override fun isOnboardingDone(): Boolean = AppSettings.onboardingDone

    override fun setOnboardingDone() {
        AppSettings.onboardingDone = true
    }

    override fun isAppLockEnabled(): Boolean = AppLockSettings.enabled

    override fun isBiometricsEnabled(): Boolean = AppLockSettings.biometricsEnabled

    override fun setBiometricsEnabled(enabled: Boolean) {
        AppLockSettings.biometricsEnabled = enabled
    }

    override fun getLockTimeout(): LockTimeout = AppLockSettings.timeout

    override fun setLockTimeout(timeout: LockTimeout) {
        AppLockSettings.timeout = timeout
    }

    // Hash mit vielen Runden, deshalb nicht auf dem UI-Thread
    override suspend fun checkPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        AppLockSettings.checkPin(pin)
    }

    override suspend fun setPin(pin: String) = withContext(Dispatchers.Default) {
        AppLockSettings.setPin(pin)
    }

    override fun clearAppLock() {
        AppLockSettings.clear()
    }
}
