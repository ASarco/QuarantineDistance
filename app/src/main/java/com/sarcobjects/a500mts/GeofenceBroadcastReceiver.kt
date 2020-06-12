package com.sarcobjects.a500mts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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

        // Get the 1st geofence that were triggered, since we only have 1 geofence.
        val geofence = geofencingEvent.triggeringGeofences[0]
        val transitionType = geofencingEvent.geofenceTransition
        // Test that the reported transition was of interest.
        when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                //vibrate the mobile
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrate26(vibrator)
                } else {
                    vibrate(vibrator)
                }
                Toast.makeText(context, context.getString(R.string.out_of_geofence), Toast.LENGTH_LONG).show()
                Log.i(TAG, "Geofence EXIT event $transitionType triggered: ${geofence.requestId}")
            }
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Toast.makeText(context, context.getString(R.string.out_of_geofence), Toast.LENGTH_LONG).show()
                Log.i(TAG, "Geofence ENTER event $transitionType triggered: ${geofence.requestId}")
            }
            else -> {
                // Log the error.
                Log.e(TAG, "Geofence incorrect transition type: $transitionType")
            }
        }
    }

    private fun vibrate(context: Vibrator) {
        context.vibrate(800)
        Thread.sleep(200)
        context.vibrate(800)
        Thread.sleep(200)
        context.vibrate(1000)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrate26(context: Vibrator) {

        val effect = VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE)
        context.vibrate(effect)
        Thread.sleep(200)
        context.vibrate(effect)
        Thread.sleep(200)
        context.vibrate(effect)
    }

    companion object {
        private val TAG = GeofenceBroadcastReceiver::class.java.simpleName
    }
}