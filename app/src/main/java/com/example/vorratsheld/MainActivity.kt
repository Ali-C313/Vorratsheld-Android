package com.example.vorratsheld

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.example.vorratsheld.ui.AppStart

// FragmentActivity statt ComponentActivity, sonst geht der BiometricPrompt nicht
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // App zeichnet hinter Status- und Navigationsleiste, die Screens halten selbst Abstand (WindowInsets)
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppStart()
        }
    }
}
