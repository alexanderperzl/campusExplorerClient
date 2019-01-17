package com.example.campusexplorer

import android.content.Context
import android.content.res.Resources
import com.example.campusexplorer.util.BitmapUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer


class CustomClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<BuildingMarkerItem>
) : DefaultClusterRenderer<BuildingMarkerItem>(context, map, clusterManager) {
    private var resources: Resources = context.resources
    override fun onBeforeClusterItemRendered(item: BuildingMarkerItem, markerOptions: MarkerOptions?) {
        val markerDescriptor =
            BitmapDescriptorFactory.fromBitmap(BitmapUtil.createCrispBitmap(R.drawable.pin_64, resources))
        markerOptions?.icon(markerDescriptor)
    }
}