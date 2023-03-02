package com.syncdev.geofence.ui

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.syncdev.geofence.R
import com.syncdev.geofence.databinding.ActivityMapsBinding
import com.syncdev.geofence.service.GeofenceReceiver
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback , EasyPermissions.PermissionCallbacks , LocationListener{

    private val TAG="MapActivity"
    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private var markerAdded=false

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var geofenceCircle: Circle
    private var geofenceRadius: Float = 500f
    private val geofenceRequestId = UUID.randomUUID().toString()
    private val geofenceTransitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        geofencingClient = LocationServices.getGeofencingClient(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getCurrentLocation()
        getBackgroundLocation()
        setOnMapLongClick(map)
    }

    private fun setOnMapLongClick(map:GoogleMap){
        map.setOnMapLongClickListener { latLng ->
            val snippet= String.format(
                Locale.getDefault(),
                "Lat: %1$.5f , Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )
            val marker=map.addMarker(
                MarkerOptions().
                position(latLng).
                title(getAddress(latLng)).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).
                snippet(snippet)
            )

            showDialog(marker!!,latLng)
        }
    }


    private fun isLocationPermissionGranted():Boolean{
        return EasyPermissions.hasPermissions(
            this,
            FINE_LOCATION,
            COARSE_LOCATION
        )
    }

    private fun isBackgroundLocationPermissionGranted():Boolean{
        return EasyPermissions.hasPermissions(
            this,
            BACKGROUND_LOCATION
        )
    }

    private fun requestLocationPermission(){
        EasyPermissions.requestPermissions(
            this,
            "The application needs permission to work properly",
            REQUEST_LOCATION_PERMISSION_CODE,
            FINE_LOCATION,
            COARSE_LOCATION
        )
    }

    private fun requestBackgroundPermission(){
        EasyPermissions.requestPermissions(
            this,
            "The application needs permission to work properly",
            REQUEST_BACKGROUND_LOCATION_PERMISSION_CODE,
            BACKGROUND_LOCATION
        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(){
        if(isLocationPermissionGranted()){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_UPDATES, MIN_DISTANCE_UPDATES, this)
        }
        else requestLocationPermission()
    }

    override fun onLocationChanged(location: Location) {
        val latLng=LatLng(location.latitude,location.longitude)
        drawCurrentLocationMarker(latLng)
    }

    private fun getBackgroundLocation(){
        if(RUNNING_Q_OR_LATER){
            if(isBackgroundLocationPermissionGranted()){
                //Toast.makeText(this, "background permission", Toast.LENGTH_SHORT).show()
            }
            else requestBackgroundPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults,this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        getCurrentLocation()
        getBackgroundLocation()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        when(requestCode)
        {
            REQUEST_LOCATION_PERMISSION_CODE->{
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    AppSettingsDialog.Builder(this).build().show()
                }else requestLocationPermission()
            }

            REQUEST_BACKGROUND_LOCATION_PERMISSION_CODE->{
                if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                    AppSettingsDialog.Builder(this).build().show()
                }else requestBackgroundPermission()
            }
        }

    }


    private fun drawCurrentLocationMarker(latLng: LatLng) {
        val zoomLevel = 15f
        val markerOptions = MarkerOptions().position(latLng).title(getAddress(latLng))

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        map.addMarker(markerOptions)

    }

    private fun getAddress(latLng: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val province =
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.get(0)?.adminArea
        val city =
            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)?.get(0)?.locality
        return "$province - $city"
    }

    private fun showDialog(marker: Marker, latLng: LatLng){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Do you want save this location ?")
        builder.setPositiveButton("YES"){dialog,id ->
            addGeofence(latLng)

            //Toast.makeText(this, "location saved", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("NO"){dialog,id->
            marker.remove()
        }
        val alertDialog: AlertDialog = builder.create()

        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng){
        geofenceCircle = map.addCircle(CircleOptions().center(latLng).radius(geofenceRadius.toDouble()).fillColor(
            Color.argb(50, 0, 0, 255)).strokeColor(Color.BLUE))
        val geofence = Geofence.Builder()
            .setRequestId(geofenceRequestId)
            .setCircularRegion(latLng.latitude, latLng.longitude, geofenceRadius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(geofenceTransitionTypes)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()

        val intent = Intent(applicationContext, GeofenceReceiver::class.java)
        intent.putExtra("address",getAddress(latLng))
        val geofencePendingIntent=PendingIntent.getBroadcast(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Geofence added")
            }
            addOnFailureListener {
                Log.e(TAG, "Failed to add geofence: ${it.message}")
            }
        }
    }

/*
    private fun removeGeofence() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                geofenceMarker.remove()
                geofenceCircle.remove()
                Log.d(TAG, "Geofence removed")
            }
            addOnFailureListener {
                Log.e(TAG, "Failed to remove geofence: ${it.message}")
            }
        }
    }
*/

    private val FINE_LOCATION=android.Manifest.permission.ACCESS_FINE_LOCATION
    private val COARSE_LOCATION=android.Manifest.permission.ACCESS_COARSE_LOCATION
    private val BACKGROUND_LOCATION=android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
    private val RUNNING_Q_OR_LATER=Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q
    private val REQUEST_LOCATION_PERMISSION_CODE=1
    private val REQUEST_BACKGROUND_LOCATION_PERMISSION_CODE=2

    private val MIN_TIME_UPDATES = 50000L
    private val MIN_DISTANCE_UPDATES = 10f
}