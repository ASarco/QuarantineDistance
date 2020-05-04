package com.sarcobjects.a500mts;

import com.android.volley.RequestQueue;
import com.google.android.gms.maps.model.LatLng;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

public abstract class SnapToRoad {

    double[] angles;
    static final double EARTH_RADIUS = 6378137;

    abstract void snapToRoad(List<LatLng> coords, RequestQueue requestQueue, VolleyCallback<List<LatLng>> callback);

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    List<LatLng> calculatePaths(LatLng latLng, double outRadius) {
        double lat = toRadians(latLng.latitude);
        double lon = toRadians(latLng.longitude);

        List<LatLng> latLngs = new ArrayList<>();
        final double d = (outRadius-10) / SnapToRoad.EARTH_RADIUS;
        for (double angle : angles) {
            LatLng offsetLatLng = calculateLatLong(lat, lon, d, angle);
            latLngs.add(offsetLatLng);
        }
        //Add the first one again at the end to close the circle.
        latLngs.add(latLngs.get(0));
        return latLngs;
    }

    private LatLng calculateLatLong(double lat, double lon, double d, double angle) {
        double lat2 = asin( sin(lat)* cos(d) + cos(lat)* sin(d)* cos(angle) );
        double lon2 = lon + atan2(sin(angle)* sin(d)* cos(lat), cos(d)- sin(lat)* sin(lat2));
        return new LatLng(round(toDegrees(lat2), 5), round(toDegrees(lon2), 5));
    }

    double[] findAngles(int slices) {
        double[] angles = new double[slices];
        if(slices == 0) {
            slices = 10;
        }
        int each = 360 / slices;
        int degrees = 0;
        for (int i = 0; i < slices; i++) {
            angles[i] = (toRadians(degrees));
            degrees += each;
        }
        return angles;
    }

    protected void handleError(VolleyCallback callback, String msg, int stringKey) {
        callback.onErrorResponse(msg, stringKey);
    }
}
