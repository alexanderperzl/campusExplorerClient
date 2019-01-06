package com.example.campusexplorer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.campusexplorer.storage.Storage
import org.json.JSONObject

const val SERVER_URL = "http://192.168.178.48:8080/"

class BuildingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building)

        // set image
        val buildingId = intent.getStringExtra("id")
        val floors = Storage.findFloors(buildingId)
        val groundFloor = floors?.filterValues { it -> it.first.level.trim() == "EG" }

        val imageView = findViewById<ImageView>(R.id.imageView)
        imageView.setOnClickListener {
            requestBuilding()
            //val intent = Intent(this, RoomDetailActivity::class.java)
            //startActivity(intent)
        }
    }

    fun requestBuilding() {
        val route = "/getBuilding"
        val body = HashMap<String, String>()
        body.put("building", "blablabla")
        post(route, body)
    }

    fun post(route: String, body: HashMap<String, String>) {
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = SERVER_URL + route

        //String Request initialized
        val request = object : StringRequest(Request.Method.POST, url, Response.Listener { response ->
            Log.d("Response:", response)
            Toast.makeText(applicationContext, response, Toast.LENGTH_LONG).show()
        }, Response.ErrorListener { error ->
            Log.d("Error", error.message)
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }) {
            override fun getBodyContentType(): String {
                return "application/json"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                return JSONObject(body).toString().toByteArray()
            }
        }
        queue!!.add(request)
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
