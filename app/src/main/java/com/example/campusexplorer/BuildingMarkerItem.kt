package com.example.campusexplorer

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem


class BuildingMarkerItem(lat: Double, lng: Double, private val mTitle: String, private val buildingId: String) : ClusterItem {
    private val mPosition: LatLng = LatLng(lat, lng)

    override fun getPosition(): LatLng {
        return mPosition
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getSnippet(): String {
        return ""
    }

    fun getBuildingId(): String {
        return buildingId
    }
}