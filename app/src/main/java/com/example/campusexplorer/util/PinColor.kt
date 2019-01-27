package com.example.campusexplorer.util

import android.content.Context
import android.support.v4.content.ContextCompat
import com.example.campusexplorer.R

object PinColor {


    enum class Color {
        Green, Orange, Blue, Grey, FreeRoom
    }

    fun getEventTypeGroup(type: String): String {
        return if (type.toLowerCase().contains("vorlesung"))
            "Vorlesung"
        else if (type.toLowerCase().contains("übung") || type.toLowerCase().contains("uebung") || type.toLowerCase().contains(
                "tutorium"
            )
        )
            "Übung"
        else if (type.toLowerCase().contains("seminar"))
            "Seminar"
        else if (type == "FreeRoom")
            "FreeRoom"
        else
            "Sonstiges"
    }

    fun eventTypeToColor(type: String): Color {
        val typeGroup = getEventTypeGroup(type)
        return when (typeGroup) {
            "Vorlesung" -> Color.Green
            "Übung" -> Color.Orange
            "Seminar" -> Color.Blue
            "FreeRoom" -> Color.FreeRoom
            else -> Color.Grey
        }
    }

    fun eventTypeToUiColor(type: String, context: Context): Int? {
        val typeGroup = getEventTypeGroup(type)
        return when (typeGroup) {
            "Vorlesung" -> ContextCompat.getColor(context, R.color.eventVorlesung)
            "Übung" -> ContextCompat.getColor(context, R.color.eventUebung)
            "Seminar" -> ContextCompat.getColor(context, R.color.eventSeminar)
            else -> ContextCompat.getColor(context, R.color.eventOther)
        }
    }

}