package com.example.campusexplorer.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.campusexplorer.R
import java.util.logging.Logger

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    private val log = Logger.getLogger(PinView::class.java.name)
    private val paint = Paint()
    private val vPin = PointF()
    private var sPinList: MutableList<Pair<PointF, Map<String, String>>> = ArrayList()
    private var pin: Bitmap
    private var originalHeight: Int = 0
    private var originalWidth: Int = 0

    init {
        val density = resources.displayMetrics.densityDpi.toFloat()
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.placeholder)
        val w = density / 360f * bitmap.width
        val h = density / 360f * bitmap.height
        log.info("width: $w, height: $h")
        log.info("bitmapWidth: ${bitmap.width}, bitmapHeight: ${bitmap.height}")
        pin = Bitmap.createScaledBitmap(bitmap, w.toInt(), h.toInt(), true)
    }

    fun addPin(sPin: PointF, data: Map<String, String>) {
        sPinList.add(Pair(sPin, data))
        invalidate()
    }

    fun setOriginalDimensions(width: Int, height: Int) {
        originalWidth = width
        originalHeight = height
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Don't draw pin before image is ready so it doesn't move around during setup.
        if (!isReady) return
        val scaledMarkers = sPinList.map {sPin ->
            PointF(markerXToCanvasX(sPin.first.x),
            markerYToCanvasY(sPin.first.y)
            )
        }
        log.info("sWidth: $sWidth, sHeight: $sHeight")
        log.info("originalWidth: $originalWidth, originalHeight: $originalHeight")
        val ratio = originalWidth.toFloat() / sWidth
        log.info("ratio: $ratio")

        log.info("width: $width, height: $height")
        log.info("x: $x, y: $y")
        log.info("center: $center")

        paint.isAntiAlias = true

        log.info("scale: $scale")

        scaledMarkers.forEach {sPin ->
            sourceToViewCoord(sPin, vPin)
            val vX = vPin.x - pin.width / 2
            val vY = vPin.y - pin.height
            //log.info("originalMarker: ${vPin.x}:${vPin.y}; scaledMarker: $vX:$vY")
            canvas.drawBitmap(pin, vX, vY, paint)
        }

    }

    private fun markerXToCanvasX(markerX: Float): Float {
        val canonicalX = markerX / originalWidth
        return canonicalX * sWidth
    }

    private fun markerYToCanvasY(markerY: Float): Float {
        val canonicalY = markerY / originalHeight
        return canonicalY * sHeight
    }

    fun clearAllPins() {
        sPinList = ArrayList()
    }


}