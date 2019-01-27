package com.example.campusexplorer.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.example.campusexplorer.*
import com.example.campusexplorer.model.Building
import com.example.campusexplorer.model.Floor
import com.example.campusexplorer.model.Room
import com.example.campusexplorer.service.ImportService
import com.example.campusexplorer.storage.Storage
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager

lateinit var mMap: GoogleMap
var mLocationPermissionGranted: Boolean = false
var mClusterManager: ClusterManager<BuildingMarkerItem>? = null

class MainActivity : AppCompatActivity(), OnMapReadyCallback, ClusterManager.OnClusterClickListener<BuildingMarkerItem>,
    ClusterManager.OnClusterItemClickListener<BuildingMarkerItem>,
    ClusterManager.OnClusterItemInfoWindowClickListener<BuildingMarkerItem> {

    private var PERMISSIONS_REQUEST_LOCATION = 1
    private val campusCenter = LatLng(48.150740, 11.581363)
    private val campusRadiusInMeters = 2000.0f
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        SharedPrefmanager.init(this)
        Log.d(TAG, "is it true?" + SharedPrefmanager.getIntroBool())
        if (SharedPrefmanager.getIntroBool() == false) {
            Log.d(TAG, "pager says hi")
            val intent = Intent(this, PagerActivity::class.java)
            startActivity(intent)
        }

        setContentView(R.layout.activity_main)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(LocalBroadcastReceiver(), IntentFilter("STORAGE_INITIALIZED"))
        initStorage()
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    private fun initStorage() {
        Intent(this, ImportService::class.java).also { intent ->
            startService(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setUpClusterer()
        getLocationPermission()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        animateToCenter(campusCenter)
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true

    }

    private fun animateToCenter(latLng: LatLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f), 3000, null)
    }

    private fun updateLocationUI() {
        try {
            if (mLocationPermissionGranted) {
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
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        animateToCurrentPositionWhenInCampus(location)
                    }

                }
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

    private fun animateToCurrentPositionWhenInCampus(location: Location) {
        val distance = FloatArray(2)
        Location.distanceBetween(
            location.getLatitude(), location.getLongitude(),
            campusCenter.latitude, campusCenter.longitude, distance
        )
        if (distance[0] < campusRadiusInMeters) {
            animateToCenter(LatLng(location.latitude, location.longitude))
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
            // get all buildings of which we have buildingId in our BuildingIDConverter; to try out clustering comment out the filtering
            val buildings = Storage.getAllBuildings()
                ?.filter { buildingId -> BuildingIDConverter.getKeys().contains(buildingId.key) }

            buildings?.forEach { building ->
                setMarker(building)
            }
        }

        private fun setMarker(building: Map.Entry<String, Pair<Building, MutableMap<String, Pair<Floor, MutableMap<String, Room>>>>>) {
            val lat = building.value.first.lat
            val lng = building.value.first.lng
            val name = building.value.first.name
            val buildingId = building.value.first._id
            val marker = BuildingMarkerItem(lat, lng, name, buildingId)
            mClusterManager!!.addItem(marker)
        }
    }


    private fun setUpClusterer() {
        // Point the map's listeners at the listeners implemented by the cluster manager
        mClusterManager = ClusterManager(this, mMap)
        val renderer = CustomClusterRenderer(this, mMap, mClusterManager!!)
        mClusterManager!!.renderer = renderer
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setInfoWindowAdapter(mClusterManager!!.markerManager)
        mMap.setOnMarkerClickListener(mClusterManager)
        mMap.setOnInfoWindowClickListener(mClusterManager)
        mClusterManager!!.setOnClusterClickListener(this)
        mClusterManager!!.setOnClusterItemClickListener(this)
        mClusterManager!!.setOnClusterItemInfoWindowClickListener(this)
    }

    override fun onClusterClick(cluster: Cluster<BuildingMarkerItem>?): Boolean {
        Log.d(TAG, "Cluster clicked")
        val builder = LatLngBounds.builder()
        val markers = cluster?.items

        for (marker in markers!!) {
            builder.include(marker.position)
        }

        val bounds = builder.build()

        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }
        return true
    }

    override fun onClusterItemClick(marker: BuildingMarkerItem?): Boolean {
        Log.d(TAG, "Item clicked")
        return false
    }

    override fun onClusterItemInfoWindowClick(marker: BuildingMarkerItem?) {
        Log.d(TAG, "Infowindow clicked")
        val buildingId = marker?.getBuildingId()
        if (BuildingIDConverter.getKeys().contains(buildingId)) {
            val intent = Intent(this, BuildingActivity::class.java)
            intent.putExtra("id", buildingId.toString())
            startActivity(intent)
        } else {
            Toast.makeText(this, "Missing mapping of building id", Toast.LENGTH_SHORT).show()
        }
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
