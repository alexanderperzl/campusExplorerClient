package com.example.campusexplorer.fragment

import android.content.Context
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.davemorrissey.labs.subscaleview.ImageSource

import com.example.campusexplorer.R
import com.example.campusexplorer.extensions.toFile
import com.example.campusexplorer.view.PinView
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import java.io.File

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_building_map, container, false)

        val mapView = view.findViewById<ImageView>(R.id.mapView) as PinView
        mapView.setMinimumTileDpi(120)
        val assetStream = activity!!.assets.open("maps/0000_d_00.pdf")
        val mapFile = File(activity!!.filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 8f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 8f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
        mapView.addPin(PointF(5500f, 4250f), mutableMapOf(Pair("room", "23")))
        mapView.addPin(PointF(8000f, 3800f), mutableMapOf(Pair("room", "24")))
        mapView.addPin(PointF(4500f, 5500f), mutableMapOf(Pair("room", "25")))
        mapView.setOnClickListener {

        }
        return view
    }

}
