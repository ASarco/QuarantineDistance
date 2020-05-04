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

import javax.inject.Inject;

import static java.lang.String.format;

public class HereSnapToRoad extends SnapToRoad {

    private static final String TAG = HereSnapToRoad.class.getSimpleName();
    private static final String SNAP_URL = "https://m.fleet.ls.hereapi.com/2/matchroute.json/?apiKey=%s&routemode=pedestrian";

    private Context context;

    @Inject
    public HereSnapToRoad(Context context) {
        this.angles = findAngles(12);
        this.context = context;
    }

    //for test
    public HereSnapToRoad(int slices) {
        this.angles = findAngles(slices);
    }

    @Override
    public void snapToRoad(List<LatLng> coords, RequestQueue requestQueue, VolleyCallback<List<LatLng>> callback) {

        String key = context.getString(R.string.here_maps_key);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, format(SNAP_URL, key, formatPaths(coords)), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        List<LatLng> latLngs = new ArrayList<>();
                        try {
                            JSONArray snapped = response.getJSONArray("snappedPoints");
                            for (int i = 0; i < snapped.length(); i++) {
                                JSONObject point = snapped.getJSONObject(i);
                                JSONObject location = point.getJSONObject("location");
                                latLngs.add(new LatLng(location.getDouble("latitude"), location.getDouble("longitude")));
                                Log.i(TAG, "Received point: " + location);
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
                .collect(Collectors.joining("|"));

    }

}
