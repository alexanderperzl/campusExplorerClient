package com.example.campusexplorer.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.campusexplorer.R
import com.example.campusexplorer.util.BitmapUtil
import com.example.campusexplorer.util.PinColor

interface MapLoadedObserver {

    fun onMapLoaded()

}

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) :
    SubsamplingScaleImageView(context, attr) {

    companion object {

        var observer: MutableList<MapLoadedObserver> = ArrayList()

        fun addObserver(mapLoadedObserver: MapLoadedObserver) {
            if (!observer.contains(mapLoadedObserver)) {
                observer.add(mapLoadedObserver)
            }
        }

        fun notifyObservers() {
            observer.forEach {
                it.onMapLoaded()
            }
        }
    }

    private val paint = Paint()
    private val vPin = PointF()
    private var sPinList: MutableList<Triple<PointF, Map<String, String>, PinColor.Color>> = ArrayList()
    private var pinGreen = BitmapUtil.createCrispBitmap(R.drawable.pin_64, resources)
    private var pinOrange = BitmapUtil.createCrispBitmap(R.drawable.pin_64_orange, resources)
    private var pinBlue = BitmapUtil.createCrispBitmap(R.drawable.pin_64_blue, resources)
    private var pinGrey = BitmapUtil.createCrispBitmap(R.drawable.pin_64_grey, resources)
    private var pinFreeRoom = BitmapUtil.createCrispBitmap(R.drawable.pin_64_free_room, resources)
    private var originalHeight: Int = 0
    private var originalWidth: Int = 0

    init {
    }

    fun addPin(sPin: PointF, data: Map<String, String>, color: PinColor.Color) {
        sPinList.add(Triple(sPin, data, color))
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
        val scaledMarkers = createScaledPointsFromPinList()

        paint.isAntiAlias = true

        scaledMarkers.forEach { sPin ->
            val coloredPin = pinForColor(sPin.third)
            sourceToViewCoord(sPin.first, vPin)
            val vX = vPin.x - coloredPin.width / 2
            val vY = vPin.y - coloredPin.height
            canvas.drawBitmap(coloredPin, vX, vY, paint)
        }

    }

    override fun onReady() {
        setScaleAndCenter(0.3f, center)
        notifyObservers()
    }

    private fun createScaledPointsFromPinList(): List<Triple<PointF, Map<String, String>, PinColor.Color>> {
        return sPinList.map { sPin ->
            Triple(
                PointF(
                    markerXToCanvasX(sPin.first.x),
                    markerYToCanvasY(sPin.first.y)
                ),
                sPin.second,
                sPin.third
            )
        }
    }

    private fun pinForColor(color: PinColor.Color): Bitmap {
        return when (color) {
            PinColor.Color.Orange -> pinOrange
            PinColor.Color.Green -> pinGreen
            PinColor.Color.Blue -> pinBlue
            PinColor.Color.FreeRoom -> pinFreeRoom
            else -> pinGrey
        }
    }

    fun dataForClick(x: Float, y: Float): Map<String, String>? {
        val scaledMarkers = createScaledPointsFromPinList()

        scaledMarkers.forEach { sPin ->
            sourceToViewCoord(sPin.first, vPin)
            val pin = pinForColor(sPin.third)
            val vX = vPin.x - pin.width / 2
            val vY = vPin.y - pin.height
            if (x >= vX && x <= vX + pin.width && y >= vY && y <= vY + pin.height) {
                return sPin.second
            }
        }
        return null
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
        invalidate()
    }


}