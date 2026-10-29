package com.example.vorratsheld.util

import android.os.Build

// Version und Gerät, gehen bei Support-Anfragen mit
object AppInfo {

    const val VERSION = "1.0"

    // z. B. "Android 15"
    val deviceName: String = "Android ${Build.VERSION.RELEASE}"
}
