package com.example.vorratsheld.di

import android.app.Application
import com.example.vorratsheld.data.settings.AppSettings
import com.example.vorratsheld.data.settings.KeyValueStore
import com.example.vorratsheld.notification.NotificationHelper
import com.example.vorratsheld.ui.locale.customAppLocale
import java.util.Locale
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Startet Koin beim App-Start, auch der Worker braucht das
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        // Gespeicherte Einstellungen einmal aus dem DataStore laden
        KeyValueStore.init(this)

        // Gespeicherte Sprache schon vor dem ersten Screen setzen
        customAppLocale = AppSettings.language
        customAppLocale?.let { Locale.setDefault(Locale.forLanguageTag(it)) }

        NotificationHelper.createNotificationChannel(this)
    }
}
