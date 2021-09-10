// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelabs.buildyourfirstmap

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.codelabs.buildyourfirstmap.place.Place
import com.google.codelabs.buildyourfirstmap.place.PlaceRenderer
import com.google.codelabs.buildyourfirstmap.place.PlacesReader
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager

class MainActivity : AppCompatActivity() {

    private val places: List<Place> by lazy {
        PlacesReader(this).read()
    }

    private var circle:Circle? = null

    private val bicycleIcon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(this, R.color.colorPrimary)
        BitmapHelper.vectorToBitmap(this, R.drawable.ic_directions_bike_black_24dp, color)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap ->
                //googleMap->addMarkers(googleMap)
                //googleMap.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
                addClusteredMarkers(googleMap)
        }
    }

    private fun addClusteredMarkers(googleMap: GoogleMap) {
        val clusterManager = ClusterManager<Place>(this, googleMap)
        clusterManager.renderer = PlaceRenderer(this, googleMap, clusterManager)
        clusterManager.markerCollection.setInfoWindowAdapter(MarkerInfoWindowAdapter(this))
        clusterManager.addItems(places)
        clusterManager.cluster()
        clusterManager.setOnClusterItemClickListener {
            item -> addCircle(googleMap, item)
            return@setOnClusterItemClickListener false
        }
        googleMap.setOnCameraIdleListener { clusterManager.onCameraIdle() }
    }

    private fun addCircle(googleMap: GoogleMap, item: Place)
    {
        circle?.remove()
        circle = googleMap.addCircle(
            CircleOptions()
                .center(item.latLng)
                .radius(1000.0)
                .fillColor(ContextCompat.getColor(this, R.color.colorPrimaryTranslucent))
                .strokeColor(ContextCompat.getColor(this, R.color.colorPrimary))
        )
    }

    private fun addMarkers(map: GoogleMap) {
        places.forEach { place ->
            val marker = map.addMarker(
                MarkerOptions()
                    .title(place.name)
                    .position(place.latLng)
                    .icon(bicycleIcon)
            )
            marker.tag = place
        }
    }
}
