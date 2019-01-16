package com.example.campusexplorer.activities

import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.campusexplorer.BuildingIDConverter
import com.example.campusexplorer.R
import com.example.campusexplorer.fragment.BuildingMapFragment
import com.example.campusexplorer.fragment.FloorChangeObserver
import com.example.campusexplorer.model.Lecture
import com.example.campusexplorer.server.IpAddress
import com.example.campusexplorer.storage.Storage
import com.example.campusexplorer.view.MapLoadedObserver
import com.example.campusexplorer.view.PinView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.logging.Logger

class BuildingActivity : AppCompatActivity(), MapLoadedObserver, FloorChangeObserver {
    val TAG = "BuildingActivity"
    val gson = Gson()

    private val log = Logger.getLogger(BuildingActivity::class.java.name)
    private lateinit var spinnerWrapper: ConstraintLayout
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var fragObj: BuildingMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building)
        spinnerWrapper = findViewById(R.id.spinnerWrapper)
        bottomNavigation = findViewById(R.id.bottomNavigationView)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        val buildingId = intent.getStringExtra("id")

        val building = Storage.findBuilding(buildingId)!!

        Log.d(TAG,buildingId)

        val buildingIdServer = BuildingIDConverter.fromClientToServer(buildingId)

        fragObj = BuildingMapFragment.newInstance(buildingId)
        fragObj.addFloorChangeObserver(this)

        spinnerWrapper.visibility = View.VISIBLE

        if (Storage.hasLecturesForBuilding(building)) {

            log.info("lectures already loaded")
//            FilterData.getFilteredDataForFloor(building)
//            FilterData.getRoomTriple(Room("", "B 001", "", 0, 0), FilterData.getFilteredDataForFloor(building))
            startFragment(fragObj)
            spinnerWrapper.visibility = View.GONE
        } else {
            loadLectures(buildingIdServer ?: "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    log.info("lectures loaded")
                    Storage.setBuildingLectures(building, it)
                    PinView.addObserver(this)
                    startFragment(fragObj)
                }, onError = {
                    log.info("got error ${it.message}")
                }, onComplete = {
                })
        }
    }

    override fun onMapLoaded() {
        spinnerWrapper.visibility = View.GONE
    }

    override fun onFloorChange() {
        spinnerWrapper.visibility = View.VISIBLE
    }

    private fun startFragment(fragment: BuildingMapFragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.buildingMapFragmentContainer, fragment)
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
        transaction.commit()
    }

    private fun loadLectures(building: String): Observable<List<Lecture>> {
        return Observable.just(building).map {
            val url = URL("http://${IpAddress.IP}:8080/postBuilding")
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
        android.R.id.home-> {
            finish()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

}
