package com.sarcobjects.a500mts

import com.android.volley.RequestQueue
import com.google.android.gms.maps.model.LatLng
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

abstract class SnapToRoad {

    lateinit var angles: DoubleArray
    abstract fun snapToRoad(coords: List<LatLng?>?, requestQueue: RequestQueue?, callback: VolleyCallback<List<LatLng?>?>?)

    fun calculatePaths(latLng: LatLng, outRadius: Double): List<LatLng> {
        val lat = Math.toRadians(latLng.latitude)
        val lon = Math.toRadians(latLng.longitude)
        val latLngs: MutableList<LatLng> = ArrayList()
        val d = (outRadius - 10) / EARTH_RADIUS
        for (angle in angles) {
            val offsetLatLng = calculateLatLong(lat, lon, d, angle)
            latLngs.add(offsetLatLng)
        }
        //Add the first one again at the end to close the circle.
        latLngs.add(latLngs[0])
        return latLngs
    }

    private fun calculateLatLong(lat: Double, lon: Double, d: Double, angle: Double): LatLng {
        val lat2 = asin(sin(lat) * cos(d) + cos(lat) * sin(d) * cos(angle))
        val lon2 = lon + atan2(sin(angle) * sin(d) * cos(lat), cos(d) - sin(lat) * sin(lat2))
        return LatLng(round(Math.toDegrees(lat2), 5), round(Math.toDegrees(lon2), 5))
    }

    fun findAngles(slices: Int): DoubleArray {
        var slices = slices
        val angles = DoubleArray(slices)
        if (slices == 0) {
            slices = 10
        }
        val each = 360 / slices
        var degrees = 0
        for (i in 0 until slices) {
            angles[i] = Math.toRadians(degrees.toDouble())
            degrees += each
        }
        return angles
    }

    protected fun handleError(callback: VolleyCallback<*>, msg: String?, stringKey: Int) {
        callback.onErrorResponse(msg, stringKey)
    }

    companion object {
        const val EARTH_RADIUS = 6378137.0
        private fun round(value: Double, places: Int): Double {
            require(places >= 0)
            var bd = BigDecimal(value.toString())
            bd = bd.setScale(places, RoundingMode.HALF_UP)
            return bd.toDouble()
        }
    }
}