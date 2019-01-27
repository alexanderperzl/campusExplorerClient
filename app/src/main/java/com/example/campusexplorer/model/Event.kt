package com.example.campusexplorer.model

data class Event(
    var room: String,
    var building: String,
    var time: String,
    var date: String,
    var dayOfWeek: String,
    var cycle: String

) {
    override fun toString(): String {
        return "Event(room='$room', building='$building', time='$time', date='$date', dayOfWeek='$dayOfWeek', cycle='$cycle')"
    }
}