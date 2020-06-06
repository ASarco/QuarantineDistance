package com.sarcobjects.a500mts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, errorMessage)
            return
        }
        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Get the 1st geofence that were triggered, since we only have 1 geofence.
            val geofence = geofencingEvent.triggeringGeofences[0]
            //vibrate the mobile
            vibrate(context)
            Toast.makeText(context, context.getString(R.string.out_of_geofence), Toast.LENGTH_LONG).show()
            Log.i(TAG, String.format("Geofence event %s triggered: %s", geofenceTransition, geofence.requestId))
        } else {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    private fun vibrate(context: Context) {
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500)
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(1000)
        (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(500)
    }

    companion object {
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
        val TIMINGS = longArrayOf(500, 1000, 500)
    }
}