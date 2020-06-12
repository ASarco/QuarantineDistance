package com.sarcobjects.a500mts

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.gms.maps.model.LatLng
import org.json.JSONException
import java.util.*
import java.util.stream.Collectors

class GoogleSnapToRoad(var context: Context? = null) : SnapToRoad() {

    init {
        angles = findAngles(12)
    }

    //for test
    constructor(slices: Int) : this() {
        angles = findAngles(slices)
    }

    override fun snapToRoad(coords: List<LatLng?>?, requestQueue: RequestQueue?, callback: VolleyCallback<List<LatLng?>?>?) {
        val key = context!!.getString(R.string.google_maps_key)
        val request = JsonObjectRequest(Request.Method.GET, String.format(SNAP_URL, key, formatPaths(coords)), null,
                Response.Listener { response ->
                    val latLngs: MutableList<LatLng?> = ArrayList()
                    try {
                        val snapped = response.getJSONArray("snappedPoints")
                        for (i in 0 until snapped.length()) {
                            val point = snapped.getJSONObject(i)
                            val location = point.getJSONObject("location")
                            latLngs.add(LatLng(location.getDouble("latitude"), location.getDouble("longitude")))
                        }
                    } catch (e: JSONException) {
                        handleError(callback!!, e.message, R.string.error_json_parse)
                    }
                    callback!!.onSuccessResponse(latLngs)
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "HTTP call didn't work: $error")
                    handleError(callback!!, "HTTP call didn't work: $error", R.string.error_http_call)
                })
        requestQueue!!.add(request)
    }

    fun formatPaths(latLngs: List<LatLng?>?): String {
        return latLngs!!.stream()
                .map { l: LatLng? -> l!!.latitude.toString() + "," + l.longitude }
                .collect(Collectors.joining("|"))
    }

    companion object {
        private val TAG = GoogleSnapToRoad::class.java.simpleName
        private const val SNAP_URL = "https://roads.googleapis.com/v1/snapToRoads?interpolate=true&key=%s&path=%s"
    }
}