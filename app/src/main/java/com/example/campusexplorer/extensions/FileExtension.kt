package com.example.campusexplorer.extensions

import java.io.File
import java.io.InputStream

fun InputStream.toFile(path: File) {
    path.outputStream().use { this.copyTo(it) }
}