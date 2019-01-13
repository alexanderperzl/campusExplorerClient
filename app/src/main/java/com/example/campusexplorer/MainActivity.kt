package com.example.campusexplorer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Room
import com.example.campusexplorer.service.ImportService
import com.example.campusexplorer.storage.Storage
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

lateinit var mMap: GoogleMap
var mLocationPermissionGranted: Boolean = false

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var PERMISSIONS_REQUEST_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(LocalBroadcastReceiver(), IntentFilter("STORAGE_INITIALIZED"))
        initStorage()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun initStorage() {
        Intent(this, ImportService::class.java).also { intent ->
            startService(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)
        updateLocationUI()
    }


    private fun updateLocationUI() {
        try {
            // hier soll eig mLocationPermissionGranted abgefragt werden, dann klappts aber nicht
            if (true) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                this.applicationContext,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }


    private class LocalBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            // get all buildings of which we have buildingId in our BuildingIDConverter
            val buildings = Storage.getAllBuildings()?.filter{buildingId -> BuildingIDConverter.getKeys().contains(buildingId.key)}

            buildings?.forEach { building ->
                setMarker(building)
            }
        }

        private fun setMarker(building: Map.Entry<String, Pair<Building, MutableMap<String, Pair<Floor, MutableMap<String, Room>>>>>) {
            val lat = building.value.first.lat
            val lng = building.value.first.lng
            val name = building.value.first.name
            val marker = mMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(name))
            marker.tag = building.value.first._id
        }
    }


    override fun onMarkerClick(marker: Marker): Boolean {
        val buildingId = marker.tag
        val intent = Intent(this, BuildingActivity::class.java)
        intent.putExtra("id", buildingId.toString())
        startActivity(intent)
        return true
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
        R.id.action_testing-> {
            val intent = Intent(this, RoomDetailActivity::class.java)
            intent.putExtra("room", "708000001_")
            intent.putExtra("building", "bw7070")
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
