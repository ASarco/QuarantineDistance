package com.sarcobjects.a500mts;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
    private boolean mLocationPermissionGranted;
    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private RequestQueue requestQueue;
    private PolyCircle polyCircle = new PolyCircle();

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
        mMap = googleMap;
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
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
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
            Snackbar.make(findViewById(R.id.map),
                    R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            Location mLastKnownLocation = task.getResult();
                            LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                            int outRadius= getResources().getInteger(R.integer.out_radius) ;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                            Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).title(getString(R.string.quarantine_place)).draggable(true));
                            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                                @Override
                                public void onMarkerDragStart(Marker marker) {};

                                @Override
                                public void onMarkerDrag(Marker marker) {}

                                @Override
                                public void onMarkerDragEnd(Marker marker) {
                                    showCircle(outRadius, marker.getPosition());
                                }
                            });
                            showCircle(outRadius, latLng);
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            Snackbar.make(findViewById(R.id.map),
                                    R.string.error_location_unavailable, Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
            Snackbar.make(findViewById(R.id.map),
                    R.string.error_security_unavailable, Snackbar.LENGTH_LONG).show();
        }
    }

    private void showCircle(int outRadius, LatLng latLng) {
        polyCircle.removeCircle();
        polyCircle.removePolygon();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
        Circle circle = mMap.addCircle(new CircleOptions().center(latLng).radius(outRadius)
                .visible(true).fillColor(Color.TRANSPARENT).strokeWidth(1));
        polyCircle.setCircle(circle);

        List<LatLng> latLngs = snapToRoad.calculatePaths(latLng, outRadius);
/*                            for (LatLng latLng : latLngs) {
                                mMap.addMarker(new MarkerOptions().position(latLng)
                                        .icon(BitmapDescriptorFactory.fromAsset("ylw-pushpin.png")));
                            }*/
        snapToRoad.snapToRoad(latLngs, requestQueue , new VolleyCallback<List<LatLng>>() {
            @Override
            public void onSuccessResponse(List<LatLng> latLngs) {
                Polygon polygon = mMap.addPolygon(new PolygonOptions()
                        .addAll(latLngs)
                        .fillColor(Color.argb(64, 0, 255, 0)).strokeWidth(1)
                        .geodesic(false)
                );
                polyCircle.setPolygon(polygon);
/*                                    for (LatLng latLng : latLngs) {
                    mMap.addMarker(new MarkerOptions().position(latLng)
                            .icon(BitmapDescriptorFactory.fromAsset("pink-pushpin.png")));
                }*/
            }

            @Override
            public void onErrorResponse(String msg, int stringKey) {
                Snackbar.make(findViewById(R.id.map),
                        stringKey, Snackbar.LENGTH_LONG).show();
            }
        });
    }

static class PolyCircle {
        Polygon polygon;
        Circle circle;

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void removeCircle() {
        if (circle != null) {
            circle.remove();
        }
    }

    public void removePolygon() {
        if (polygon != null) {
            polygon.remove();
        }
    }
}

}
