package com.example.campusexplorer.util

import android.content.Context
import android.support.v4.content.ContextCompat
import com.example.campusexplorer.R

object PinColor {


    enum class Color {
        Green, Orange, Blue, Grey
    }

    fun eventTypeToColor(type: String): Color {
        return if (type == "Vorlesung") {
            Color.Green
        } else if (type == "Übung") {
            Color.Orange
        } else if (type == "Seminar"){
            Color.Blue
        } else {
            Color.Grey
        }
    }

    fun eventTypeToUiColor(type: String, context:Context): Int? {
        return when (type) {
            "Vorlesung" -> ContextCompat.getColor(context, R.color.eventVorlesung)
            "Übung" -> ContextCompat.getColor(context, R.color.eventUebung)
            "Seminar" -> ContextCompat.getColor(context, R.color.eventSeminar)
            else -> ContextCompat.getColor(context, R.color.eventOther)
        }
    }

}