package com.example.campusexplorer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.campusexplorer.service.ImportService
import com.example.campusexplorer.storage.Storage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initStorage()
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        // TODO get updated Location, show on map
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location -> lastLocation = location}
        }
        else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), PackageManager.PERMISSION_GRANTED)
        }
    }

    private fun initStorage() {
        Intent(this, ImportService::class.java).also { intent ->
            startService(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener(this)

        val buildings = Storage.getAllBuildings()

        // TODO Only set markers for buildings once the storage is fully initialized

        buildings.forEach { building ->
            val lat = building.value.first.lat
            val lng = building.value.first.lng
            val name = building.value.first.name
            mMap.addMarker(MarkerOptions().position(LatLng(lat, lng)).title(name))
        }

        val oettingenstrasse = LatLng(48.1500233, 11.5942831)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oettingenstrasse, 15.0f))

    }


    override fun onMarkerClick(marker: Marker): Boolean {
        val intent = Intent(this, BuildingActivity::class.java)
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

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }
}
