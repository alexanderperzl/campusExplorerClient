package com.example.campusexplorer.util

import android.graphics.Color

object PinColor {


    enum class Color {
        Green, Orange, Blue
    }

    fun eventTypeToColor(type: String): Color {
        return if (type == "Vorlesung") {
            Color.Green
        } else if (type == "Übung") {
            Color.Orange
        } else {
            Color.Blue
        }
    }

    fun eventTypeToUiColor(type: String): Int? {
        return when (type) {
            "Vorlesung" -> android.graphics.Color.argb(0xFF, 0x00, 0x85, 0x77)
            "Übung" -> android.graphics.Color.argb(0xFF, 0xD6, 0x5B, 0x00)
            "Seminar" -> android.graphics.Color.argb(0xFF, 0x0E, 0x3B, 0x8E)
            else -> android.graphics.Color.argb(0xFF, 0x00, 0x00, 0x8E)
        }
    }

}