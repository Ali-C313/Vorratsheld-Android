package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.enums.LockTimeout

// Einstellungen, die nur auf dem Gerät liegen: Sprache, Onboarding und App-Sperre
interface SettingsRepository {
    // null = Systemsprache
    fun getLanguage(): String?

    fun setLanguage(code: String?)

    fun isOnboardingDone(): Boolean

    fun setOnboardingDone()

    fun isAppLockEnabled(): Boolean

    fun isBiometricsEnabled(): Boolean

    fun setBiometricsEnabled(enabled: Boolean)

    fun getLockTimeout(): LockTimeout

    fun setLockTimeout(timeout: LockTimeout)

    suspend fun checkPin(pin: String): Boolean

    // Neuer PIN schaltet die Sperre gleich mit ein
    suspend fun setPin(pin: String)

    // Beim Ausschalten, Abmelden oder Konto löschen
    fun clearAppLock()
}
