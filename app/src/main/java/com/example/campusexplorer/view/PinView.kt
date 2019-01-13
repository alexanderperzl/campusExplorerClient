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
    private var markerAreRescaled = false

    init {
        val density = resources.displayMetrics.densityDpi.toFloat()
        val bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.placeholder)
        val w = density / 360f * bitmap.width
        val h = density / 360f * bitmap.height
        log.info("width: $w, height: $h")
        log.info("bitmapWidth: ${bitmap.width}, bitmapHeight: ${bitmap.height}")
        pin = Bitmap.createScaledBitmap(bitmap, w.toInt(), h.toInt(), true)
    }

    fun setNewImage(source: ImageSource) {
        super.setImage(source)
        markerAreRescaled = false
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
        if (!markerAreRescaled) {
            sPinList.forEach {sPin ->
                sPin.first.x = markerXToCanvasX(sPin.first.x)
                sPin.first.y = markerYToCanvasY(sPin.first.y)
                markerAreRescaled = true
            }
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

        sPinList.forEach {sPin ->
            sourceToViewCoord(sPin.first, vPin)
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