package com.example.campusexplorer.fragment

import android.graphics.PointF
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import com.davemorrissey.labs.subscaleview.ImageSource

import com.example.campusexplorer.R
import com.example.campusexplorer.extensions.toFile
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
        val assetStream = activity!!.assets.open("maps/0000_d_00.pdf")
        val mapFile = File(activity!!.filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 1f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 1f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
        mapView.addPin(PointF(550f, 425f), mutableMapOf(Pair("room", "23")))
        mapView.addPin(PointF(800f, 380f), mutableMapOf(Pair("room", "24")))
        mapView.addPin(PointF(450f, 550f), mutableMapOf(Pair("room", "25")))
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

    }
}
