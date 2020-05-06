package com.sarcobjects.a500mts;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class BingSnapToRoad extends SnapToRoad {

    private static final String TAG = BingSnapToRoad.class.getSimpleName();
    private static final String SNAP_URL =
            "https://dev.virtualearth.net/REST/v1/Routes/SnapToRoad?interpolate=true&includeSpeedLimit=false&includeTruckSpeedLimit=false&travelMode=walking&key=%s&points=%s";
    private final Context context;

    public BingSnapToRoad(Context context) {
        this.angles = findAngles(16);
        this.context = context;
    }

    @Override
    void snapToRoad(List<LatLng> coords, RequestQueue requestQueue, VolleyCallback<List<LatLng>> callback) {

        String key = context.getString(R.string.bing_maps_key);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, format(SNAP_URL, key, formatPaths(coords)), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, response.toString());
                        List<LatLng> latLngs = new ArrayList<>();
                        try {
                            JSONArray snapped = response.getJSONArray("resourceSets");
                            for (int i = 0; i < snapped.length(); i++) {
                                JSONObject set = snapped.getJSONObject(i);
                                JSONArray resources = set.getJSONArray("resources");
                                for (int j = 0; j < resources.length(); j++) {
                                    JSONObject snappedPoints = resources.getJSONObject(j);
                                    JSONArray points = snappedPoints.getJSONArray("snappedPoints");
                                    for (int k = 0; k < points.length(); k++) {
                                        JSONObject point = points.getJSONObject(k);
                                        JSONObject coordinate = point.getJSONObject("coordinate");
                                        latLngs.add(new LatLng(coordinate.getDouble("latitude"), coordinate.getDouble("longitude")));
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            handleError(callback, e.getMessage(), R.string.error_json_parse);
                        }
                        callback.onSuccessResponse(latLngs);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "HTTP call didn't work: " + error.toString());
                        handleError(callback, "HTTP call didn't work: " + error.toString(), R.string.error_http_call);
                    }
                });

        requestQueue.add(request);
    }


    String formatPaths(List<LatLng> latLngs) {
        return latLngs.stream()
                .map(l -> l.latitude + "," + l.longitude)
                .collect(Collectors.joining(";"));

    }
}
