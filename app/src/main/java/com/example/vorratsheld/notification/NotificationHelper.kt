package com.example.vorratsheld.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.vorratsheld.R
import com.example.vorratsheld.data.settings.AppSettings
import java.util.Locale

// Bündelt alles rund um den Notification-Channel und das Anzeigen der Ablauf-Erinnerung
object NotificationHelper {

    private const val CHANNEL_ID = "expiry_reminders"
    private const val NOTIFICATION_ID = 1001

    // Einmal beim App-Start aufrufen (macht auf älteren Android-Versionen nichts)
    fun createNotificationChannel(appContext: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val context = localized(appContext)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    // Zeigt eine Sammel-Notification für alle bald ablaufenden Produkte
    fun showExpiryNotification(appContext: Context, productNames: List<String>) {
        if (productNames.isEmpty()) return
        val context = localized(appContext)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

        if (!hasPermission) return

        val title = if (productNames.size == 1) {
            context.getString(R.string.notification_one_expiring)
        } else {
            context.getString(R.string.notification_many_expiring, productNames.size)
        }
        val text = productNames.joinToString(", ")

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    // Texte in der Sprache, die in der App gewählt ist (null = Systemsprache)
    private fun localized(context: Context): Context {
        val code = AppSettings.language ?: return context
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(code))
        return context.createConfigurationContext(config)
    }
}
