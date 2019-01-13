package com.example.campusexplorer.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.example.campusexplorer.R
import com.example.campusexplorer.adapter.FilterAdapter
import com.example.campusexplorer.filter.FilterData


class SettingsActivity : AppCompatActivity() {

    private lateinit var facultyView: RecyclerView
    private lateinit var facultyAdapter: RecyclerView.Adapter<*>
    private lateinit var facultyManager: RecyclerView.LayoutManager

    private lateinit var eventView: RecyclerView
    private lateinit var eventAdapter: RecyclerView.Adapter<*>
    private lateinit var eventManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        facultyManager = LinearLayoutManager(this)
        facultyAdapter = FilterAdapter(FilterData.faculties)

        facultyView = findViewById<RecyclerView>(R.id.filter_list_fakultaeten).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = facultyManager

            // specify an viewAdapter (see also next example)
            adapter = facultyAdapter}

        facultyView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))



            eventManager = LinearLayoutManager(this)
            eventAdapter = FilterAdapter(FilterData.eventTypes)

            eventView = findViewById<RecyclerView>(R.id.filter_list_veranstaltungen).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = eventManager

                // specify an viewAdapter (see also next example)
                adapter = eventAdapter
            }
        eventView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
