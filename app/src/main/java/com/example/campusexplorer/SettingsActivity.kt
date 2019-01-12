package com.example.campusexplorer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import com.example.campusexplorer.Adapter.FilterAdapter

class SettingsActivity : AppCompatActivity() {

    private lateinit var fakultaetView: RecyclerView
    private lateinit var fakultaetAdapter: RecyclerView.Adapter<*>
    private lateinit var fakultaetManager: RecyclerView.LayoutManager

    private lateinit var veranstaltungsView: RecyclerView
    private lateinit var veranstaltungsAdapter: RecyclerView.Adapter<*>
    private lateinit var veranstaltungsManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        fakultaetManager = LinearLayoutManager(this)
        fakultaetAdapter = FilterAdapter(myDataset)

        fakultaetView = findViewById<RecyclerView>(R.id.filter_list_fakultaeten).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = fakultaetManager

            // specify an viewAdapter (see also next example)
            adapter = fakultaetAdapter}



            veranstaltungsManager = LinearLayoutManager(this)
            veranstaltungsAdapter = FilterAdapter(myDataset)

            veranstaltungsView = findViewById<RecyclerView>(R.id.filter_list_veranstaltungen).apply {
                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                setHasFixedSize(true)

                // use a linear layout manager
                layoutManager = veranstaltungsManager

                // specify an viewAdapter (see also next example)
                adapter = veranstaltungsAdapter
            }

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
