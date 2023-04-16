package com.example.mobilproje

import MyLocation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import kotlin.math.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class MapFragment : Fragment() {

    val locations = mutableListOf<MyLocation>()
    val filteredLocations = mutableListOf<MyLocation>()
    lateinit var userName: String
    private var currUserLocation : MyLocation? = null
    private lateinit var userListSearchView: SearchView
    lateinit var mapFragment: SupportMapFragment
    val database = FirebaseDatabase.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userName = arguments?.getString("userName").toString()

        val rootView = inflater.inflate(R.layout.fragment_map, container, false)
        mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        lifecycleScope.launch {
            getLocationsFromDatabase()
        }

        return rootView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userListSearchView = view.findViewById(R.id.searchView)
        viewLifecycleOwner.lifecycleScope.launch{
            currUserLocation = getMyLocation()
        }
        showMap()
        userListSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })

    }
    fun getLocationsFromDatabase() {
        database.child("locations").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                locations.clear()
                for (childSnapshot in snapshot.children) {
                    childSnapshot.getValue(MyLocation::class.java)?.let { myLocation ->
                        locations.add(myLocation)

                    }
                }
                filteredLocations.addAll(locations)
                }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    suspend fun getMyLocation(): MyLocation {
        val dataSnapshot = database.child("locations").child(userName).get().await()
        return dataSnapshot.getValue(MyLocation::class.java)!!
    }
     private fun showMap(){
        mapFragment.getMapAsync { googleMap ->
            googleMap.clear()
            val builder = LatLngBounds.Builder()
            for (location in filteredLocations) {
                val markerOptions = MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .title(location.name)
                    .zIndex(0.0F)
                    .snippet(location.surName)
                if (location.userName == userName) {
                    markerOptions.title("You")
                    markerOptions.zIndex(12.1F)
                    markerOptions.snippet("You are here!!!")
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                }
                googleMap.addMarker(markerOptions)
                builder.include(LatLng(location.latitude, location.longitude))
            }
            val bounds = builder.build()
            val padding = 100
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
            googleMap.moveCamera(cameraUpdate)

        }
    }

    private fun filter(text: String){
        filteredLocations.clear()
        val value = if (!text.isNullOrEmpty()) text.toInt() else Int.MAX_VALUE
        locations.forEach { location->
            if(distance(currUserLocation!!,location)<value)
                filteredLocations.add(location)
        }

        showMap()
    }
    private fun distance(loc1: MyLocation, loc2: MyLocation): Double {
        val R = 6371 // Dünya'nın yarıçapı (km)
        val dLat = (loc2.latitude - loc1.latitude).toRadians()
        val dLon = (loc2.longitude - loc1.longitude).toRadians()
        val a = sin(dLat/2) * sin(dLat/2) +
                cos(loc1.latitude.toRadians()) * cos(loc2.latitude.toRadians()) *
                sin(dLon/2) * sin(dLon/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val distance = R * c * 1000 // Metre cinsinden uzaklık
        return abs(distance)
    }

    private fun Double.toRadians(): Double {
        return this * Math.PI / 180
    }



}