package com.sarcobjects.a500mts;

import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.Polygon;

public class PolyCircle {
    Polygon polygon;
    Circle circle;

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public void removeCircle() {
        if (circle != null) {
            circle.remove();
        }
    }

    public void removePolygon() {
        if (polygon != null) {
            polygon.remove();
        }
    }
}
