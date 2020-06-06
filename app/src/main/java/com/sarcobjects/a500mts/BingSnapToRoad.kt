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

class BingSnapToRoad(context: Context) : SnapToRoad() {
    private val context: Context
    override fun snapToRoad(coords: List<LatLng?>?, requestQueue: RequestQueue?, callback: VolleyCallback<List<LatLng?>?>?) {
        val key = context.getString(R.string.bing_maps_key)
        val request = JsonObjectRequest(Request.Method.GET, String.format(SNAP_URL, key, formatPaths(coords)), null,
                Response.Listener { response ->
                    Log.i(TAG, response.toString())
                    val latLngs: MutableList<LatLng?> = ArrayList()
                    try {
                        val snapped = response.getJSONArray("resourceSets")
                        for (i in 0 until snapped.length()) {
                            val set = snapped.getJSONObject(i)
                            val resources = set.getJSONArray("resources")
                            for (j in 0 until resources.length()) {
                                val snappedPoints = resources.getJSONObject(j)
                                val points = snappedPoints.getJSONArray("snappedPoints")
                                for (k in 0 until points.length()) {
                                    val point = points.getJSONObject(k)
                                    val coordinate = point.getJSONObject("coordinate")
                                    latLngs.add(LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")))
                                }
                            }
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
                .collect(Collectors.joining(";"))
    }

    companion object {
        private val TAG = BingSnapToRoad::class.java.simpleName
        private const val SNAP_URL = "https://dev.virtualearth.net/REST/v1/Routes/SnapToRoad?interpolate=true&includeSpeedLimit=false&includeTruckSpeedLimit=false&travelMode=walking&key=%s&points=%s"
    }

    init {
        angles = findAngles(16)
        this.context = context
    }
}