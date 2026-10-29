package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.enums.LockTimeout

// Für Previews: Einstellungen nur im Speicher, PIN ist 1234
class SettingsRepositoryMock : SettingsRepository {

    private var language: String? = null
    private var onboardingDone = true
    private var pin: String? = "1234"
    private var biometrics = false
    private var timeout = LockTimeout.ONE_MINUTE

    override fun getLanguage(): String? = language

    override fun setLanguage(code: String?) {
        language = code
    }

    override fun isOnboardingDone(): Boolean = onboardingDone

    override fun setOnboardingDone() {
        onboardingDone = true
    }

    override fun isAppLockEnabled(): Boolean = pin != null

    override fun isBiometricsEnabled(): Boolean = biometrics

    override fun setBiometricsEnabled(enabled: Boolean) {
        biometrics = enabled
    }

    override fun getLockTimeout(): LockTimeout = timeout

    override fun setLockTimeout(timeout: LockTimeout) {
        this.timeout = timeout
    }

    override suspend fun checkPin(pin: String): Boolean = pin == this.pin

    override suspend fun setPin(pin: String) {
        this.pin = pin
    }

    override fun clearAppLock() {
        pin = null
        biometrics = false
    }
}
