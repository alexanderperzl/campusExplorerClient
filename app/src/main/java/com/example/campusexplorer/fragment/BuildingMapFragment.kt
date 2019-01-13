package com.example.campusexplorer.fragment

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import com.davemorrissey.labs.subscaleview.ImageSource

import com.example.campusexplorer.R
import com.example.campusexplorer.extensions.toFile
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.storage.Storage
import com.example.campusexplorer.view.PinView
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import java.io.File
import java.util.logging.Logger


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [BuildingMapFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [BuildingMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class BuildingMapFragment : Fragment() {

    private lateinit var mapView: PinView
    private val log = Logger.getLogger(BuildingMapFragment::class.java.name)
    private var currentFloorIndex: Int = 0
    private var floorList = ArrayList<Floor>()
    private var buildingId = "bw0000"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_building_map, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapView = view.findViewById(R.id.mapView) as PinView
        mapView.setMinimumTileDpi(120)
        val assetStream = activity!!.assets.open("maps/7070_d_00.pdf")
        val mapFile = File(activity!!.filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 4.18f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 4.18f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
        val rooms = Storage.findAllRooms("g7070-1")
        val TAG = "BuildingMapFragment"
        Log.d(TAG, rooms.toString())
        rooms.forEach {
            mapView.addPin(
                PointF(it.mapX.toFloat() - 50f, it.mapY.toFloat() + 25f),
                mutableMapOf(Pair("room", it.name))
            )
        }
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                mapView.viewToSourceCoord(e.x, e.y)?.let {
                    log.info("Got click on ${e.x}:${e.y}")
                } ?: run {
                    log.info("NOt ready for clicking")
                }
                return true
            }

        })
        mapView.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }

        val buttonFloorUp = view.findViewById<ImageButton>(R.id.button_floor_up)
        val buttonFloorDown = view.findViewById<ImageButton>(R.id.button_floor_down)
        val textFloor = view.findViewById<TextView>(R.id.text_floor)

        floorList = getOrderedFloors(buildingId)
        currentFloorIndex = floorList.indexOf(floorList.first { it -> it.levelDouble == 0.0 })
        updateFloor(textFloor)

        buttonFloorUp.setOnClickListener {
            onFloorUp(textFloor)
        }

        buttonFloorDown.setOnClickListener {
            onFloorDown(textFloor)
        }
    }

    private fun onFloorDown(textFloor: TextView) {
        if (currentFloorIndex - 1 >= 0) {
            currentFloorIndex--
            updateFloor(textFloor)
        }
    }

    private fun onFloorUp(textFloor: TextView) {
        if (currentFloorIndex + 1 < floorList.size) {
            currentFloorIndex++
            updateFloor(textFloor)
        }
    }

    private fun updateFloor(textFloor: TextView) {
        textFloor.text = floorList[currentFloorIndex].level
    }

    private fun getOrderedFloors(buildingId: String): ArrayList<Floor> {
        val buildingMap = Storage.findBuildingById(buildingId)?.second

        buildingMap?.forEach { it ->
            floorList.add(it.value.first)
        }

        return ArrayList(floorList.sortedWith(compareBy { it.levelDouble }))
    }

}

