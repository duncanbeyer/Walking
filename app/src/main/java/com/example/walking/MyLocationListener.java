package com.example.walking;

import android.location.Location;
import android.location.LocationListener;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.walking.MapsActivity;

public class MyLocationListener implements LocationListener {

    private final MapsActivity mapsActivity;
    private static final String TAG = "MyLocationListener";

    MyLocationListener(MapsActivity mainActivity) {

        this.mapsActivity = mainActivity;

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        mapsActivity.updateLocation(location);
    }
}
