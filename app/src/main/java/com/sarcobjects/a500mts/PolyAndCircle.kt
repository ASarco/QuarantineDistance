package com.sarcobjects.a500mts

import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.Polygon

/**
 * To simplify the removal of Circle and Polygon in case if they still not declared.
 */
class PolyAndCircle(var circle: Circle?, var polygon: Polygon?) {

    fun removeCircle() {
        circle?.let {it.remove()}
    }

    fun removePolygon() {
        polygon?.let {it.remove()}
    }
}