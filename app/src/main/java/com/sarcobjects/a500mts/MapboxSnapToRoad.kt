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

class MapboxSnapToRoad(context: Context) : SnapToRoad() {
    private val context: Context
    override fun snapToRoad(coords: List<LatLng?>?, requestQueue: RequestQueue?, callback: VolleyCallback<List<LatLng?>?>?) {
        val key = context.resources.getString(R.string.mapbox_maps_key)

        val url = String.format(SNAP_URL, formatPaths(coords), key, radiuses(coords!!.size), timestamps(coords.size), waypoints(coords.size))
        val request = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.i(TAG, "Response -> $response")
                    Log.i(TAG, "Request -> $url")
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
                    handleError(callback!!, "HTTP call didn't work: ${error.message}", R.string.error_http_call)
                })
        requestQueue!!.add(request)
    }

    fun formatPaths(latLngs: List<LatLng?>?): String {

        return latLngs!!.stream()
                    .map { l: LatLng? -> l!!.latitude.toString() + "," + l.longitude }
                .collect(Collectors.joining(";"))
    }

    fun radiuses(number: Int): String {
        return Array<String>(number) { "50"}.joinToString(separator=";")
    }

    fun timestamps(number: Int): String {
        return Array<String>(number) {i ->  (i * 5).toString()}.joinToString(separator =";" )
    }

    fun waypoints(number: Int): String {
        return Array<String>(number) {i ->  i.toString()}.joinToString(separator =";" )
    }

    companion object {
        private val TAG = MapboxSnapToRoad::class.java.simpleName
        private const val SNAP_URL = "https://api.mapbox.com/matching/v5/mapbox/walking/%s?access_token=%s&radiuses=%s&timestamps=%s&waypoints=%s&steps=false&tidy=true&geometries=polyline&overview=false";
    }

    init {
        angles = findAngles(16)
        this.context = context
    }
}