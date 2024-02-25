package com.example.walking;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

public class FenceData {

    private final String id;
    private final LatLng latLng;
    private final double radius;
    private final int type = Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT;

    FenceData(String id, LatLng latLng, double radius) {
        this.id = id;
        this.latLng = latLng;
        this.radius = radius;
    }

    String getId() {
        return id;
    }

    LatLng getLatLng() {
        return latLng;
    }

    float getRadius() {
        return (float) radius;
    }

    int getType() {
        return type;
    }


    @NonNull
    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", latLng=" + latLng +
                ", radius=" + radius +
                ", type=" + type +
                '}';
    }
}
