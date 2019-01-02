package com.example.campusexplorer.model

data class Floor(
    val _id: String,
    val building: String,
    val mapFileName: String,
    val level: String,
    val mapWidth: Int,
    val mapHeight: Int,
    val address: String)