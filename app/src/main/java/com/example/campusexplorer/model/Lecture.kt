package com.example.campusexplorer.model

data class Lecture(
    val _id: String,
    val name: String,
    var events: List<Event>,
    val department: String,
    val type: String,
    val faculty: String,
    val link: String
)