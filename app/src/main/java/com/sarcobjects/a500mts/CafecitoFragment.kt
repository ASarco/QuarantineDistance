package com.sarcobjects.a500mts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.cafecito.*


class CafecitoFragment : Fragment() {

    var showCafecito = false;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.cafecito, container, false)
        val gestureDetector = GestureDetector(activity, SwipeGestureListener())
        view.setOnTouchListener(OnTouchListener { v, event ->
            v.performClick()
            gestureDetector.onTouchEvent(event)
            true
        })
        return view
    }

    override fun onResume() {
        super.onResume()
        if (!showCafecito) {
            fragmentManager!!.beginTransaction().hide(this).commit()
        }
        buttonGotoCafecito.setOnClickListener {
            try {
                val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cafecito_url)))
                startActivity(myIntent)
            } catch (e: Exception) {
                Toast.makeText(this.context, getString(R.string.error_no_browser), Toast.LENGTH_SHORT).show()
            }
        }
        activity?.let { LocalBroadcastManager.getInstance(it).registerReceiver(alarmReceiver, IntentFilter("show_fragment")) };
    }

    override fun onPause() {
        super.onPause()
        buttonGotoCafecito.setOnClickListener(null)
        activity?.let { LocalBroadcastManager.getInstance(it).unregisterReceiver(alarmReceiver) };
    }

    private val alarmReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            showCafecito = true
            fragmentManager!!.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(this@CafecitoFragment).commit();
        }
    }

    companion object {
        val TAG = CafecitoFragment::class.java.simpleName
        private const val SWIPE_MIN_DISTANCE = 120
    }

    inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            //Only left to right or right to left
            if (e1!!.x - e2!!.x > SWIPE_MIN_DISTANCE || e2.x - e1.x > SWIPE_MIN_DISTANCE) {
                fragmentManager!!.beginTransaction()
                        //.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
                        .hide(this@CafecitoFragment)
                        .commit()
            }
            return true
        }
    }
}