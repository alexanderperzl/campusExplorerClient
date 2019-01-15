package com.example.campusexplorer.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

object BitmapUtil {

    fun createCrispBitmap(resource: Int, resources: Resources): Bitmap {
        val density = resources.displayMetrics.densityDpi.toFloat()
        val bitmap = BitmapFactory.decodeResource(resources, resource)
        val w = density / 720f * bitmap.width
        val h = density / 720f * bitmap.height
        return Bitmap.createScaledBitmap(bitmap, w.toInt(), h.toInt(), true)
    }

}