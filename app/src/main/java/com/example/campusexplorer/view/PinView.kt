package com.example.campusexplorer.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.campusexplorer.R

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    private val paint = Paint()
    private val vPin = PointF()
    private var sPinList: MutableList<Pair<PointF, Map<String, String>>> = ArrayList()
    private var pin: Bitmap

    init {
        val density = resources.displayMetrics.densityDpi.toFloat()
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.placeholder)
        val w = density / 360f * bitmap.width
        val h = density / 360f * bitmap.height
        pin = Bitmap.createScaledBitmap(bitmap, w.toInt(), h.toInt(), true)
    }

    fun addPin(sPin: PointF, data: Map<String, String>) {
        sPinList.add(Pair(sPin, data))
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) return

        paint.isAntiAlias = true

        sPinList.forEach {sPin ->
            sourceToViewCoord(sPin.first, vPin)
            val vX = vPin.x - pin.width / 2
            val vY = vPin.y - pin.height
            canvas.drawBitmap(pin, vX, vY, paint)
        }

    }


}