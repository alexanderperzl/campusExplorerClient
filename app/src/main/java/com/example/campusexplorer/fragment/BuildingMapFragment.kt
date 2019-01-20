package com.example.campusexplorer.fragment

import android.app.Dialog
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import com.davemorrissey.labs.subscaleview.ImageSource
import com.example.campusexplorer.R
import com.example.campusexplorer.SliderRangeTimeConverter
import com.example.campusexplorer.activities.BuildingActivity
import com.example.campusexplorer.activities.RoomDetailActivity
import com.example.campusexplorer.extensions.toFile
import com.example.campusexplorer.filter.FilterData
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.model.Room
import com.example.campusexplorer.storage.Storage
import com.example.campusexplorer.util.PinColor
import com.example.campusexplorer.view.PinView
import com.google.gson.Gson
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import io.apptik.widget.MultiSlider
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
interface FloorChangeObserver {

    fun onFloorChange()

}

class BuildingMapFragment: Fragment(){


    private val TAG = "BuildingMapFragment"
    private lateinit var mapView: PinView
    private val log = Logger.getLogger(BuildingMapFragment::class.java.name)
    private var currentFloorIndex: Int = 0
    private var floorList = ArrayList<Floor>()
    private var buildingId: String? = ""
    private lateinit var textFloor: Button
    private lateinit var buttonFloorUp: ImageButton
    private lateinit var buttonFloorDown: ImageButton
    private lateinit var rooms: List<Room>
    private var floorChangeObserver: MutableList<FloorChangeObserver> = ArrayList()
    private lateinit var seekBar: MultiSlider
    private lateinit var time:TextView
    private var seekbarState:Int = R.id.action_events

    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildingId = arguments?.getString("buildingId")
        Log.d(TAG, buildingId)
        dialog = Dialog(activity)
    }

    fun updateSeekBar(menuItem: MenuItem) {
        when(menuItem.itemId){
            R.id.action_free_rooms -> {
                seekBar.addThumb()
                seekbarState = menuItem.itemId
                seekBarToTime(seekBar)
            }
            R.id.action_events -> {
                seekBar.removeThumb(1)
                seekbarState = menuItem.itemId
                seekBarToTime(seekBar)
            }
        }
    }

    companion object {
        fun newInstance(id: String): BuildingMapFragment {
            val fragment = BuildingMapFragment()
            val args = Bundle()
            args.putString("buildingId", id)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        val building = Storage.findBuilding(buildingId!!)
        rooms = FilterData.getFilteredFloors(building!!, floorList[currentFloorIndex])
        mapView.clearAllPins()
        setMarkers(rooms)
        dialog.dismiss()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_building_map, container, false)

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUIElements(view)

        seekBarToTime(seekBar)

        seekBar.setOnThumbValueChangeListener { multiSlider, thumb, thumbIndex, value ->
//            if (thumbIndex == 0)time.text = value.toString()
            val timesStartEnd = seekBarToTime(multiSlider)


        }




        floorList = getOrderedFloors(buildingId!!)
        currentFloorIndex = floorList.indexOf(floorList.first { it -> it.levelDouble == 0.0 })
        updateLevelArrows()
        updateFloor()

        mapView.setMinimumTileDpi(120)
        mapView.maxScale = 12.0f

        setPDF(mapView, floorList[currentFloorIndex].mapFileName)
        val building = Storage.findBuilding(buildingId!!)
        rooms = FilterData.getFilteredFloors(building!!, floorList[currentFloorIndex])
        Log.d(TAG, rooms.toString())

        setMarkers(rooms)
        val gestureDetector = GestureDetector(activity, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                mapView.viewToSourceCoord(e.x, e.y)?.let {
                    val roomData = mapView.dataForClick(e.x, e.y)
                    if (roomData != null && roomData.containsKey("roomId")) {
                        showDialogWindow(e, roomData)
                    }
                } ?: run {
                    log.info("Not ready for clicking")
                }
                return true
            }

        })
        mapView.setOnTouchListener { _, motionEvent -> gestureDetector.onTouchEvent(motionEvent) }
    }

    private fun showDialogWindow(e: MotionEvent, roomData: Map<String, String>) {
        dialog.setContentView(R.layout.info_window)
        val dialogWindow = dialog.window
        dialogWindow.setGravity(Gravity.START or Gravity.TOP)
        val layoutParams = dialogWindow.attributes
        layoutParams.x = e.x.toInt() - 100
        layoutParams.y = e.y.toInt() - 30
        dialogWindow.attributes = layoutParams
        dialog.show()
        val infoWindowButton = dialog.findViewById<Button>(R.id.infoWindowButton)
        // TODO Hier sollte eig der name der aktuellen Veranstaltung gesetzt werden. Muesste aber noch korrekt geholt werden
        infoWindowButton.text = roomData["roomId"]
        infoWindowButton.setOnClickListener { openRoomDetailActivity(roomData) }
    }

    private fun openRoomDetailActivity(roomData: Map<String, String>) {
        val roomId = roomData["roomId"]!!
        log.info("Clicked room $roomId")
        val intent = Intent(context, RoomDetailActivity::class.java)
        intent.putExtra("room", roomId)
        intent.putExtra("building", buildingId)
        startActivity(intent)
    }

    private fun initUIElements(view: View) {
        mapView = view.findViewById(R.id.mapView) as PinView

        time = view.findViewById(R.id.time)

        buttonFloorUp = view.findViewById(R.id.button_floor_up)
        buttonFloorDown = view.findViewById(R.id.button_floor_down)
        textFloor = view.findViewById(R.id.text_floor)

        buttonFloorUp.setOnClickListener { onFloorUp() }
        buttonFloorDown.setOnClickListener { onFloorDown() }

        seekBar = view.findViewById(R.id.seekbar)

    }

    private fun seekBarToTime(multiSlider: MultiSlider):MutableList<String>{
        var times:MutableList<String> = ArrayList()

        when(seekbarState){
            R.id.action_events -> {
                times.add(SliderRangeTimeConverter.valueToTime(multiSlider.getThumb(0).value)!!)
                times.add(SliderRangeTimeConverter.valueToTime(multiSlider.getThumb(0).value)!!)
                time.text = "${times[0]}"
            }
            R.id.action_free_rooms -> {
                times.add(SliderRangeTimeConverter.valueToTime(multiSlider.getThumb(0).value)!!)
                times.add(SliderRangeTimeConverter.valueToTime(multiSlider.getThumb(1).value)!!)
                time.text = "${times[0]} : ${times[1]}"
            }
        }

        return times
    }

    private fun setMarkers(rooms: List<Room>) {
        // TODO Hier sollte der Wert des Zeitsliders übergeben werden
        val lectures =
            FilterData.getFilteredDataForFloor(Storage.findBuilding(buildingId!!)!!, floorList[currentFloorIndex])
        val floor = floorList[currentFloorIndex]
        val markerOffsetX = floor.markerOffsetX ?: 0
        val markerOffsetY = floor.markerOffsetY ?: 0
        rooms.forEach {
            mapView.addPin(
                PointF((it.mapX - markerOffsetX).toFloat(), (it.mapY - markerOffsetY).toFloat()),
                mutableMapOf(Pair("roomId", it._id)),
                // TODO Hier sollte der Wert des Zeitsliders übergeben werden
                roomEventToColor(it, lectures)
            )
        }
    }

    private fun roomEventToColor(room: Room, lectures: List<Lecture>): PinColor.Color {
        val gson = Gson()
        Log.d(TAG, "lectures" + gson.toJson(lectures))
        Log.d(TAG, "room" + room.name)
        // TODO Hier sollte der Wert des Zeitsliders übergeben werden
        val roomTriple = FilterData.getRoomTriple(room, lectures)
        return PinColor.eventTypeToColor(roomTriple.third!!.type)
    }

    private fun setPDF(mapView: PinView, floorPlan: String) {
        val assetStream = activity!!.assets.open("maps/$floorPlan")
        val mapFile = File(activity!!.filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 8f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 8f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
    }

    private fun onFloorDown() {
        if (currentFloorIndex - 1 >= 0) {
            currentFloorIndex--
            updateLevelArrows()
            updateFloor()
        }
    }

    private fun onFloorUp() {
        if (currentFloorIndex + 1 < floorList.size) {
            currentFloorIndex++
            updateLevelArrows()
            updateFloor()
        }
    }

    private fun updateLevelArrows() {
        if (currentFloorIndex <= 0) {
            buttonFloorDown.visibility = View.INVISIBLE
        } else {
            buttonFloorDown.visibility = View.VISIBLE
        }
        if (currentFloorIndex >= floorList.size - 1) {
            buttonFloorUp.visibility = View.INVISIBLE
        } else {
            buttonFloorUp.visibility = View.VISIBLE
        }
    }

    private fun updateFloor() {
        notifyFloorChangeObserver()
        textFloor.text = floorList[currentFloorIndex].level
        mapView.clearAllPins()
        setPDF(mapView, floorList[currentFloorIndex].mapFileName)
        val currFloor = floorList[currentFloorIndex]
        mapView.setOriginalDimensions(currFloor.mapWidth, currFloor.mapHeight)
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

    fun addFloorChangeObserver(observer: FloorChangeObserver) {
        if (!floorChangeObserver.contains(observer)) {
            floorChangeObserver.add(observer)
        }
    }

    private fun notifyFloorChangeObserver() {
        floorChangeObserver.forEach {
            it.onFloorChange()
        }
    }

}

