package com.example.campusexplorer.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.campusexplorer.R
import com.example.campusexplorer.util.BitmapUtil
import java.util.logging.Logger

class PinView @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) : SubsamplingScaleImageView(context, attr) {

    enum class PinColor {
        Green, Orange, Blue
    }

    private val log = Logger.getLogger(PinView::class.java.name)
    private val paint = Paint()
    private val vPin = PointF()
    private var sPinList: MutableList<Triple<PointF, Map<String, String>, PinColor>> = ArrayList()
    private var pinGreen = BitmapUtil.createCrispBitmap(R.drawable.pin_64, resources)
    private var pinOrange = BitmapUtil.createCrispBitmap(R.drawable.pin_64_orange, resources)
    private var pinBlue = BitmapUtil.createCrispBitmap(R.drawable.pin_64_blue, resources)
    private var originalHeight: Int = 0
    private var originalWidth: Int = 0

    init {}

    fun addPin(sPin: PointF, data: Map<String, String>, color: PinColor) {
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

        scaledMarkers.forEach {sPin ->
            val coloredPin = pinForColor(sPin.third)
            sourceToViewCoord(sPin.first, vPin)
            val vX = vPin.x - coloredPin.width / 2
            val vY = vPin.y - coloredPin.height
            canvas.drawBitmap(coloredPin, vX, vY, paint)
        }

    }

    private fun createScaledPointsFromPinList(): List<Triple<PointF, Map<String, String>, PinColor>> {
        return sPinList.map {sPin -> Triple(
            PointF(
                markerXToCanvasX(sPin.first.x),
                markerYToCanvasY(sPin.first.y)
            ),
            sPin.second,
            sPin.third
        )}
    }

    private fun pinForColor(color: PinColor): Bitmap {
        return if (color == PinColor.Orange) {
            pinOrange
        } else if (color == PinColor.Green) {
            pinGreen
        } else {
            pinBlue
        }
    }

    fun dataForClick(x: Float, y: Float): Map<String, String>? {
        val scaledMarkers = createScaledPointsFromPinList()

        scaledMarkers.forEach {sPin ->
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
    }


}