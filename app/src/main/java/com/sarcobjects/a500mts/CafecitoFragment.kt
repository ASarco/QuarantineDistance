package com.sarcobjects.a500mts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.android.synthetic.main.cafecito.*


class CafecitoFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.cafecito, container, false)
    }

    override fun onResume() {
        super.onResume()

        fragmentManager!!.beginTransaction().hide(this).commit()
        buttonGotoCafecito.setOnClickListener {
            try {
                val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.cafecito_url)))
                startActivity(myIntent)
            } catch (e:Exception) {
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
            val cafecitoFragment = fragmentManager!!.findFragmentById(R.id.cafecito) as Fragment
            fragmentManager!!.beginTransaction().setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).show(cafecitoFragment).commit();
        }
    }
}

