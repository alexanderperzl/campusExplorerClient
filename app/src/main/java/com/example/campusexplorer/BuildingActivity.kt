package com.example.campusexplorer

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.example.campusexplorer.extensions.toFile
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.number42.subsampling_pdf_decoder.PDFDecoder
import de.number42.subsampling_pdf_decoder.PDFRegionDecoder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Logger

class BuildingActivity : AppCompatActivity() {
    val gson = Gson()

    private val log = Logger.getLogger(BuildingActivity::class.java.name)
    private lateinit var spinnerWrapper: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building)
        spinnerWrapper = findViewById(R.id.spinnerWrapper)

        val buildingId = intent.getStringExtra("id")

        val buildingIdServer = BuildingIDConverter.fromClientToServer(buildingId)

        spinnerWrapper.visibility = View.VISIBLE
        val mapView = findViewById<ImageView>(R.id.mapView) as SubsamplingScaleImageView
        mapView.setMinimumTileDpi(120)
        val assetStream = assets.open("maps/0000_d_00.pdf")
        val mapFile = File(filesDir, "temp_building.pdf")
        assetStream.toFile(mapFile)
        mapView.setBitmapDecoderFactory { PDFDecoder(0, mapFile, 8f) }
        mapView.setRegionDecoderFactory { PDFRegionDecoder(0, mapFile, 8f) }
        val source = ImageSource.uri(mapFile.absolutePath)
        mapView.setImage(source)
        
        if (Storage.hasLectures()) {
            log.info("lectures already loaded")
            spinnerWrapper.visibility = View.GONE
        } else {
            loadLectures(buildingIdServer ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    log.info("lectures loaded")
                    Storage.setLectures(it)
                }, onError = {
                    log.info("got error ${it.message}")
                }, onComplete = {
                    spinnerWrapper.visibility = View.GONE
                })
        }

        val building = Storage.findBuildingById(buildingId)?.second

        var floorList = ArrayList<Floor>()
        building?.forEach { it ->
            floorList.add(it.value.first)
        }

        floorList.sortedWith(compareBy { it.levelDouble })

    }

    private fun loadLectures(building: String): Observable<List<Lecture>> {
        return Observable.just(building).map {
            val url = URL("$SERVER_URL/postBuilding")
            val conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            val params = HashMap<String, String>()
            params["building"] = building
            val bodyStrings = params.entries.map {
                "\"" + it.key + "\":\"" + it.value + "\""
            }.joinToString(prefix = "{", postfix = "}", separator = ",")
            log.info("Sending request to url ${url.toString()} with body $bodyStrings")
            val outputStream = BufferedOutputStream(conn.outputStream)
            outputStream.write(bodyStrings.toByteArray())
            outputStream.flush()
            val inputStream = BufferedInputStream(conn.inputStream)
            val response: String = inputStream.bufferedReader().use { it.readText() }
            log.info("got response with length ${response.length}")
            val lectureListType = object : TypeToken<Collection<Lecture>>() {}.type
            val lectures: List<Lecture> = gson.fromJson(response, lectureListType)
            log.info("loaded lectures $lectures")
            lectures
        }
    }

    /* Tool Bar */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
