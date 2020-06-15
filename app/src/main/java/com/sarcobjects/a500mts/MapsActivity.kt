package com.sarcobjects.a500mts

import android.Manifest.permission
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.sarcobjects.a500mts.android.MapApplication
import javax.inject.Inject


class MapsActivity : FragmentActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap

    // The entry point to the Fused Location Provider.
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    // A default location (Buenos Aires, Argentina) and default zoom to use when location permission is not granted.
    private val mDefaultLocation = LatLng(-34.6037, -58.3816)
    private var mLocationPermissionGranted = false
    private var mLocationBackgroundPermissionGranted = false
    private lateinit var requestQueue: RequestQueue
    private val polyAndCircle = PolyAndCircle(null, null)
    private lateinit var geofencingClient: GeofencingClient
    private var geofencePendingIntent: PendingIntent? = null

    @Inject
    lateinit var snapToRoad: SnapToRoad

    override fun onCreate(savedInstanceState: Bundle?) {
        // Make Dagger instantiate @Inject fields in MapActivity
        MapApplication.applicationComponent!!.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_500mts)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //TODO make singleton
        requestQueue = Volley.newRequestQueue(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)

        setAlarmCafecito()
        checkLocationServices()
    }

    private fun setAlarmCafecito() {
        val intent = Intent(this@MapsActivity, AlarmBroadcastReceiver::class.java)
        val sender = PendingIntent.getBroadcast(this@MapsActivity, 666, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val am: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + resources.getInteger(R.integer.show_cafecito_at).toLong(), sender)
        Log.i(TAG, "Alarm set for ${resources.getInteger(R.integer.show_cafecito_at)}ms from now")
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "OnMap Ready")
        map = googleMap
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
    }

    private fun updateLocationUI() {
        Log.i(TAG, "updateLocationUI start")
        try {
            if (!mLocationPermissionGranted) {
                map.isMyLocationEnabled = false
                map.uiSettings.isMyLocationButtonEnabled = false
                locationPermission
            }
            deviceLocation
        } catch (e: SecurityException) {
            Log.e(TAG, "Security Exception: ", e)
            Snackbar.make(findViewById(R.id.map),
                    R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        mLocationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true
                    mLocationBackgroundPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }/*&& ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED*/

    /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
    private val locationPermission: Unit
        get() {
            /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED /*&& ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
                mLocationPermissionGranted = true
                mLocationBackgroundPermissionGranted = true
            } else {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_BACKGROUND_LOCATION),
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                }
            }
        }

    /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
    private val deviceLocation: Unit
        get() {
            /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
            try {
                Log.i(TAG, "getDeviceLocation start")
                if (mLocationPermissionGranted) {
                    map.isMyLocationEnabled = true
                    map.uiSettings.isMyLocationButtonEnabled = true
                    val locationRequest = LocationRequest.create()
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 20000
                    locationRequest.fastestInterval = 10000
                    mFusedLocationProviderClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            val mLastKnownLocation = locationResult.lastLocation
                            val outRadius = resources.getInteger(R.integer.out_radius)
                            if (mLastKnownLocation != null) {
                                val latLng = LatLng(mLastKnownLocation.latitude, mLastKnownLocation.longitude)
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
                                map.addMarker(MarkerOptions().position(latLng).title(getString(R.string.quarantine_place)).draggable(true))
                                showCircle(outRadius, latLng)
                                mFusedLocationProviderClient.removeLocationUpdates(this)
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.")
                                map.addMarker(MarkerOptions().position(mDefaultLocation).title(getString(R.string.quarantine_place)).draggable(true))
                                map.uiSettings.isMyLocationButtonEnabled = false
                                Snackbar.make(findViewById(R.id.map),
                                        R.string.error_location_unavailable, Snackbar.LENGTH_LONG).show()
                            }
                            map.setOnMarkerDragListener(object : OnMarkerDragListener {
                                override fun onMarkerDragStart(marker: Marker) {}
                                override fun onMarkerDrag(marker: Marker) {}
                                override fun onMarkerDragEnd(marker: Marker) {
                                    showCircle(outRadius, marker.position)
                                }
                            })
                        }
                    }, Looper.getMainLooper())
                }
            } catch (e: SecurityException) {
                Log.e("Exception: %s", e.message!!)
                Snackbar.make(findViewById(R.id.map),
                        R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show()
            }
        }

    private fun enableGeofencing(latLng: LatLng, outRadius: Int) {

        geofencePendingIntent?.let { geofencingClient.removeGeofences(it) }

        Log.i(TAG, "Activating geofence...")
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "No permisssion to enable geofence")
            return
        }
        geofencingClient.addGeofences(getGeofenceRequest(latLng, outRadius), getGeofencePendingIntent())
                .addOnSuccessListener(this) { Log.i(TAG, "Geofence Activated!") }
                .addOnFailureListener(this) { e ->
                    Log.e(TAG, "Geofence Failed to activate due to " + e.message, e)
                    Snackbar.make(findViewById(R.id.map), R.string.error_no_geofence, Snackbar.LENGTH_LONG).show()
                }
    }

    private fun getGeofencePendingIntent(): PendingIntent?  {

        geofencePendingIntent?.let { return it }

        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getGeofenceRequest(latLng: LatLng, outRadius: Int): GeofencingRequest {
        val geofence = Geofence.Builder()
                .setRequestId(QUARANTINE)
                .setCircularRegion(latLng.latitude, latLng.longitude, outRadius.toFloat())
                .setExpirationDuration(3600000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT or Geofence.GEOFENCE_TRANSITION_ENTER)
                .setLoiteringDelay(500)
                .build()
        return GeofencingRequest.Builder().addGeofence(geofence).build()
    }

    private fun showCircle(outRadius: Int, latLng: LatLng) {
        polyAndCircle.removeCircle()
        polyAndCircle.removePolygon()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM.toFloat()))
        val circle = map.addCircle(CircleOptions().center(latLng).radius(outRadius.toDouble())
                .visible(true).fillColor(Color.TRANSPARENT).strokeWidth(2f).strokeColor(Color.RED))
        polyAndCircle.circle = circle
        enableGeofencing(latLng, outRadius)
        val latLngs: List<LatLng?> = snapToRoad.calculatePaths(latLng, outRadius.toDouble())
        snapToRoad.snapToRoad(latLngs, requestQueue, object : VolleyCallback<List<LatLng?>?> {
            override fun onSuccessResponse(results: List<LatLng?>?) {
                val polygon = map.addPolygon(PolygonOptions()
                        .addAll(results)
                        .fillColor(Color.argb(64, 0, 255, 0)).strokeWidth(3f)
                        .geodesic(false)
                )
                polyAndCircle.polygon = polygon
            }

            override fun onErrorResponse(msg: String?, stringKey: Int) {
                Snackbar.make(findViewById(R.id.map),
                        stringKey, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun checkLocationServices() {
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        val isLocationEnabled = LocationManagerCompat.isLocationEnabled(lm);
        if (!isLocationEnabled) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.gps_not_found_title) // GPS not found
                    .setMessage(R.string.gps_not_found_message) // Want to enable?
                    .setPositiveButton(R.string.yes) {
                        _, _ -> this.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        const val QUARANTINE = "quarantine"
    }
}