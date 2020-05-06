package com.sarcobjects.a500mts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import static java.lang.String.format;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            // Get the 1st geofence that were triggered, since we only have 1 geofence.
            Geofence geofence = geofencingEvent.getTriggeringGeofences().get(0);
            //vibrate the mobile

            Log.i(TAG, format("Geofence event %s triggered: %s", geofenceTransition, geofence.getRequestId()));
        } else {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }
}
