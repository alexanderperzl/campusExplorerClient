package com.example.campusexplorer.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import com.example.campusexplorer.R
import com.example.campusexplorer.SharedPrefmanager
import com.example.campusexplorer.adapter.RoomDetailAdapter
import com.example.campusexplorer.filter.FilterData
import com.example.campusexplorer.storage.Storage
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var eventView: RecyclerView
    private lateinit var eventAdapter: RecyclerView.Adapter<*>
    private lateinit var eventManager: RecyclerView.LayoutManager

    private lateinit var roomName: TextView
    private lateinit var floorName: TextView
    private lateinit var buildingName: TextView

    private val TAG = "RoomDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        roomName = findViewById(R.id.room_name)
        floorName = findViewById(R.id.floor)
        buildingName = findViewById(R.id.building_name)


        val roomID: String = intent.getStringExtra("room")
        val buildingID: String = intent.getStringExtra("building")
        Log.d(TAG, "buildingID $buildingID")
        val building = Storage.findBuilding(buildingID)
        val room = Storage.findRoom(roomID)

        val currentTime = intent.getStringExtra("time")
        val roomTriple =
            FilterData.getRoomTriple(room!!, FilterData.getFilteredDataForBuilding(building!!, true), currentTime)
        Log.d(TAG, "roomtriple")
        val gson = Gson()
        Log.d(TAG, gson.toJson(roomTriple))
        val floor = Storage.findFloor(room.floor)

        buildingName.text = "Adresse: ${building!!.name}"
        floorName.text = "Stockwerk: ${floor!!.level}"
        roomName.text = "Raum: ${room.name}"


        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        eventManager = LinearLayoutManager(this)
        eventAdapter = RoomDetailAdapter(roomTriple)

        eventView = findViewById<RecyclerView>(R.id.lecture_list_detail).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = eventManager

            // specify an viewAdapter (see also next example)
            adapter = eventAdapter
        }
        eventView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

    }

    /* Tool Bar */

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            true
        }

        android.R.id.home -> {
            finish()
            true
        }

        R.id.ip -> {
            val alertDialog: AlertDialog = this.let {
                val builder = AlertDialog.Builder(it)

                // Create the AlertDialog
                val view = layoutInflater.inflate(R.layout.ip_alert, null)
                val ipAddress: EditText = view.findViewById(R.id.ip_edit)
                ipAddress.setText(SharedPrefmanager.getIP())
                builder.setView(view)
                    .setPositiveButton(
                        "OK"
                    ) { _, _ ->
                        // User clicked OK button
                        SharedPrefmanager.saveIP(ipAddress.text.toString())
                    }
                    .setNegativeButton(
                        "No!"
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                builder.create()
            }

            alertDialog.show()
            true
        }
        R.id.show_tour -> {
            val intent = Intent(this, PagerActivity::class.java)
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
