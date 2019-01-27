package com.example.campusexplorer.model

data class Lecture(
    val _id: String,
    val name: String,
    var events: List<Event>,
    val department: String,
    val type: String,
    val faculty: String,
    val link: String
) {
    override fun toString(): String {
        return "Lecture(_id='$_id', name='$name', events=$events, department='$department', type='$type', faculty='$faculty', link='$link')"
    }
}