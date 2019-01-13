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
import com.example.campusexplorer.filter.FilterData
import com.example.campusexplorer.filter.FilterData.getFilteredFloors
import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Room
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
class BuildingMapFragment: Fragment() {
    private val TAG = "BuildingMapFragment"
    private lateinit var mapView: PinView
    private val log = Logger.getLogger(BuildingMapFragment::class.java.name)
    private var currentFloorIndex: Int = 0
    private var floorList = ArrayList<Floor>()
    private var buildingId: String? = ""
    private lateinit var textFloor: TextView
    private lateinit var buttonFloorUp: ImageButton
    private lateinit var buttonFloorDown: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildingId = arguments?.getString("buildingId")
        Log.d(TAG,buildingId)
    }

    companion object {
        fun newInstance(id: String): BuildingMapFragment {
            val fragment = BuildingMapFragment ()
            val args = Bundle()
            args.putString("buildingId", id)
            fragment.arguments = args
            return fragment
        }
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_building_map, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUIElements(view)

        floorList = getOrderedFloors(buildingId!!)
        currentFloorIndex = floorList.indexOf(floorList.first { it -> it.levelDouble == 0.0 })
        updateFloor()

        mapView.setMinimumTileDpi(120)

        setPDF(mapView, floorList[currentFloorIndex].mapFileName)
//        val rooms = Storage.findAllRooms(floorList[currentFloorIndex]._id)
        val building = Storage.findBuilding(buildingId!!)
        val rooms = FilterData.getFilteredFloors(building!!, floorList[currentFloorIndex])
        Log.d(TAG, rooms.toString())
        setMarkers(rooms)
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                mapView.viewToSourceCoord(e.x, e.y)?.let {
                    log.info("Got click on ${e.x}:${e.y}")
                } ?: run {
                    log.info("Not ready for clicking")
                }
                return true
            }

        })
        mapView.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

    private fun initUIElements(view: View) {
        mapView = view.findViewById(R.id.mapView) as PinView

        buttonFloorUp = view.findViewById(R.id.button_floor_up)
        buttonFloorDown = view.findViewById(R.id.button_floor_down)
        textFloor = view.findViewById(R.id.text_floor)

        buttonFloorUp.setOnClickListener { onFloorUp() }
        buttonFloorDown.setOnClickListener { onFloorDown() }
    }

    private fun setMarkers(rooms: List<Room>) {
        rooms.forEach {
            mapView.addPin(
                PointF(it.mapX.toFloat() - 50f, it.mapY.toFloat() + 25f),
                mutableMapOf(Pair("room", it.name))
            )
        }
    }

    private fun setPDF(mapView: PinView, floorPlan: String) {
        val assetStream = activity!!.assets.open("maps/$floorPlan")
        val mapFile = File(activity!!.filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 4.18f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 4.18f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
    }

    private fun onFloorDown() {
        if (currentFloorIndex - 1 >= 0) {
            currentFloorIndex--
            updateFloor()
        }
    }

    private fun onFloorUp() {
        if (currentFloorIndex + 1 < floorList.size) {
            currentFloorIndex++
            updateFloor()
        }
    }

    private fun updateFloor() {
        textFloor.text = floorList[currentFloorIndex].level
        mapView.clearAllPins()
        setPDF(mapView, floorList[currentFloorIndex].mapFileName)
//        val rooms = Storage.findAllRooms(floorList[currentFloorIndex]._id)
        val building = Storage.findBuilding(buildingId!!)
        val rooms = FilterData.getFilteredFloors(building!!, floorList[currentFloorIndex])
        setMarkers(rooms)
    }

    private fun getOrderedFloors(buildingId: String): ArrayList<Floor> {
        val buildingMap = Storage.findBuildingById(buildingId)?.second

        buildingMap?.forEach { it ->
            floorList.add(it.value.first)
        }

        return ArrayList(floorList.sortedWith(compareBy { it.levelDouble }))
    }

}

