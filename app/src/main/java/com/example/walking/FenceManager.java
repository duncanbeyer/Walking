package com.example.walking;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

class FenceManager {

    private static final String TAG = "FenceManager";
    private final MapsActivity mapsActivity;
    private final GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;


    FenceManager(final MapsActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
        geofencingClient = LocationServices.getGeofencingClient(mapsActivity);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(mapsActivity, aVoid -> Log.d(TAG, "onSuccess: removeGeofences"))
                .addOnFailureListener(mapsActivity, e -> {
                    Log.d(TAG, "onFailure: removeGeofences");
                });
    }

    void addFence(FenceData fd) {

        if (ActivityCompat.checkSelfPermission(mapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(fd.getId())
                .setCircularRegion(
                        fd.getLatLng().latitude,
                        fd.getLatLng().longitude,
                        fd.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = getGeofencePendingIntent();

        geofencingClient
                .addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "onSuccess: addGeofences"))
                .addOnFailureListener(e -> {
                    Log.d(TAG, "onFailure: addGeofences: " + e.getMessage());
                });
    }

    private PendingIntent getGeofencePendingIntent() {

        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(mapsActivity, GeoReceiver.class);

        geofencePendingIntent = PendingIntent.getBroadcast(
                mapsActivity, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return geofencePendingIntent;
    }


}
