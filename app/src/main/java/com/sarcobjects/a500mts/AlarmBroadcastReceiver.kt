package com.sarcobjects.a500mts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AlarmBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Alarma cafecito!!!")
        //Notify cafecito fragment
        val showFragmentIntent = Intent("show_fragment")
        LocalBroadcastManager.getInstance(context).sendBroadcast(showFragmentIntent)
    }

    companion object {
        private val TAG = AlarmBroadcastReceiver::class.java.simpleName
    }

}