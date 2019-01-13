package com.example.campusexplorer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.example.campusexplorer.adapter.FilterAdapter
import com.example.campusexplorer.adapter.RoomDetailAdapter
import com.example.campusexplorer.filter.FilterData

class RoomDetailActivity : AppCompatActivity() {

    private lateinit var eventView: RecyclerView
    private lateinit var eventAdapter: RecyclerView.Adapter<*>
    private lateinit var eventManager: RecyclerView.LayoutManager

    private lateinit var roomName: TextView
    private lateinit var floorName: TextView
    private lateinit var buildingName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_detail)

        /*var room:String = intent.getStringExtra("")
        var floor:String = intent.getStringExtra("")
        var building:String = intent.getStringExtra("")

        roomName.setText(room)
        floorName.setText(floor)
        buildingName.setText(building)*/


        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        eventManager = LinearLayoutManager(this)
        //eventAdapter = RoomDetailAdapter(myData)

        eventView = findViewById<RecyclerView>(R.id.filter_list_veranstaltungen).apply {
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
