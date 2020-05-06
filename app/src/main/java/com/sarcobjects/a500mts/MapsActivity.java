package com.sarcobjects.a500mts;

import android.Manifest;
import android.Manifest.permission;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.sarcobjects.a500mts.android.MapApplication;

import java.util.List;

import javax.inject.Inject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Buenos Aires, Argentina) and default zoom to use when location permission is not granted.
    private final LatLng mDefaultLocation = new LatLng(-34.6037, -58.3816);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION = 2;
    public static final String QUARANTINE = "quarantine";
    private boolean mLocationPermissionGranted;
    private boolean mLocationBackgroundPermissionGranted;
    private RequestQueue requestQueue;
    private PolyCircle polyCircle = new PolyCircle();
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;

    @Inject
    SnapToRoad snapToRoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Make Dagger instantiate @Inject fields in MapActivity
        MapApplication.applicationComponent.inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //TODO make singleton
        requestQueue = Volley.newRequestQueue(this);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        geofencingClient = LocationServices.getGeofencingClient(this);
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "OnMap Ready");
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        Log.i(TAG, "updateLocationUI start");
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                // Get the current location of the device and set the position of the map.
                getDeviceLocation();
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
            if (!mLocationBackgroundPermissionGranted) {
                //getLocationBackgroundPermission();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception: ", e);
            Snackbar.make(findViewById(R.id.map),
                    R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            /*&& ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED*/) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_FINE_LOCATION/*, permission.ACCESS_BACKGROUND_LOCATION*/},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mLocationBackgroundPermissionGranted = true;
                }
            }
            case PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    mLocationBackgroundPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    //@RequiresApi(api = Build.VERSION_CODES.Q)
    private void getLocationBackgroundPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationBackgroundPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission.ACCESS_BACKGROUND_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_BACKGROUND_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Log.i(TAG, "getDeviceLocation start");

            if (mLocationPermissionGranted) {
                LocationRequest locationRequest = LocationRequest.create();
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locationRequest.setInterval(20000);
                locationRequest.setFastestInterval(10000);
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        Location mLastKnownLocation = locationResult.getLastLocation();
                        int outRadius = getResources().getInteger(R.integer.out_radius);
                        if (mLastKnownLocation != null) {
                            LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                            mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.quarantine_place)).draggable(true));
                            showCircle(outRadius, latLng);
                            mFusedLocationProviderClient.removeLocationUpdates(this);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title(getString(R.string.quarantine_place)).draggable(true));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            Snackbar.make(findViewById(R.id.map),
                                    R.string.error_location_unavailable, Snackbar.LENGTH_LONG).show();
                        }
                        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                            @Override
                            public void onMarkerDragStart(Marker marker) {
                            }

                            @Override
                            public void onMarkerDrag(Marker marker) {
                            }

                            @Override
                            public void onMarkerDragEnd(Marker marker) {
                                showCircle(outRadius, marker.getPosition());
                            }
                        });
                    }
                }, Looper.getMainLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
            Snackbar.make(findViewById(R.id.map),
                    R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    private void enableGeofencing(LatLng latLng, int outRadius) {
        if (geofencePendingIntent != null) {
            geofencingClient.removeGeofences(geofencePendingIntent);
        }
        if (mLocationBackgroundPermissionGranted) {
            Log.i(TAG, "Activating geofence...");
            geofencingClient.addGeofences(getGeofenceRequest(latLng, outRadius), getGeofencePendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Geofence Activated!");
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Geofence Failed to activate due to " + e.getMessage(), e);
                        }
                    });
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    private GeofencingRequest getGeofenceRequest(LatLng latLng, int outRadius) {

        Geofence geofence = new Geofence.Builder()
                .setRequestId(QUARANTINE)
                .setCircularRegion(latLng.latitude, latLng.longitude, outRadius)
                .setExpirationDuration(3600000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_EXIT)
                .setLoiteringDelay(500)
                .build();
        return new GeofencingRequest.Builder().addGeofence(geofence).build();
    }

    private void showCircle(int outRadius, LatLng latLng) {
        polyCircle.removeCircle();
        polyCircle.removePolygon();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        Circle circle = mMap.addCircle(new CircleOptions().center(latLng).radius(outRadius)
                .visible(true).fillColor(Color.TRANSPARENT).strokeWidth(1));
        polyCircle.setCircle(circle);
        enableGeofencing(latLng, outRadius);
        List<LatLng> latLngs = snapToRoad.calculatePaths(latLng, outRadius);
        snapToRoad.snapToRoad(latLngs, requestQueue, new VolleyCallback<List<LatLng>>() {
            @Override
            public void onSuccessResponse(List<LatLng> latLngs) {
                Polygon polygon = mMap.addPolygon(new PolygonOptions()
                        .addAll(latLngs)
                        .fillColor(Color.argb(64, 0, 255, 0)).strokeWidth(1)
                        .geodesic(false)
                );
                polyCircle.setPolygon(polygon);
            }

            @Override
            public void onErrorResponse(String msg, int stringKey) {
                Snackbar.make(findViewById(R.id.map),
                        stringKey, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
