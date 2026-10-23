package com.example.vorratsheld.ui.screens.help

import com.example.vorratsheld.R
import com.example.vorratsheld.data.enums.SupportCategory

// Text für die Auswahl-Chips
val SupportCategory.label: Int
    get() = when (this) {
        SupportCategory.PROBLEM -> R.string.help_category_problem
        SupportCategory.FEATURE -> R.string.help_category_feature
        SupportCategory.QUESTION -> R.string.help_category_question
    }
