package com.example.vorratsheld.util

import android.os.Build

const val APP_VERSION = "1.0"

// z. B. "Android 15", geht bei Support-Anfragen mit
val deviceName: String = "Android ${Build.VERSION.RELEASE}"
