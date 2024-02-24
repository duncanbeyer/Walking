package com.example.walking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.core.splashscreen.SplashScreen;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.walking.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private boolean keepOn = true;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private ArrayList<String> routeNames = new ArrayList<>();
    private static final int LOCATION_REQUEST = 111;
    private static final int NOTIFICATION_REQUEST = 222;
    private static final int BACKGROUND_REQUEST = 333;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Polyline llHistoryPolyline;
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private Marker walker;
    public static int screenHeight;
    public static int screenWidth;
    private final float zoomDefault = 15.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initSplash();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG,"in onMapReady");

        mMap = googleMap;

        mMap.setBuildingsEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        if (checkPermission()) {
            setupLocationListener();
        }
    }

    private void initSplash() {
        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {
                                return keepOn;
                            }
                        }
                );
    }



    private boolean checkPermission() {
        Log.d(TAG,"in checkPermission");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d(TAG, "tiramisu true");
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "no notifi perm");
                checkPermissionsInit();
                return false;
            }
        }
        if ((ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)
                | (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED)
        ){
            Log.d(TAG,"second false");
            checkPermissionsInit();
            return false;
        }
        Log.d(TAG,"it has permissions in checkPermission()");
        return true;
    }



    private void checkPermissionsInit() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestAppLocationPermissions();
            Log.d(TAG,"just requested location, about to return.");
            return;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestAppBackgroundPermissions();
            Log.d(TAG,"just requested background, about to return.");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestAppNotificationPermissions();
                Log.d(TAG,"just requested notif, about to return.");
                return;
            }
        }
        try {
//            Thread.sleep(2000);
            Log.d(TAG,"sleep here");
        } catch (Exception e) {
            Log.d(TAG,"error sleeping", e);
        }

        setupLocationListener();

    }

    private void requestAppLocationPermissions() {

        Log.d(TAG,"About to request location permission 1");

        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_REQUEST);

        Log.d(TAG,"About to request location permission 2");

    }

    private void requestAppBackgroundPermissions() {

        Log.d(TAG,"About to request background permission 1");

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q)
            return;

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                BACKGROUND_REQUEST);

        Log.d(TAG,"About to request background permission 2");

    }

    private void requestAppNotificationPermissions() {

        Log.d(TAG,"About to request notification permission 1");

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_REQUEST);

        Log.d(TAG,"About to request notification permission 2");

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions.length == 0) {
            Log.d(TAG,"permissions length 0");
            return;
        }

        if (requestCode == LOCATION_REQUEST) {
            if (permissions[0].equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "location permission granted");
                    checkPermissionsInit();
                } else {
                    Log.d(TAG, "location permission denied");
                    permissionDenied();
                }
            }
        }
        else if (requestCode == BACKGROUND_REQUEST) {
            if (permissions[0].equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "background permission granted");
                    checkPermissionsInit();
                } else {
                    Log.d(TAG, "background permission denied");
                    permissionDenied();
                }
            }
        }
        else if (requestCode == NOTIFICATION_REQUEST) {
            if (permissions[0].equals(Manifest.permission.POST_NOTIFICATIONS)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "notification permission granted");
                    checkPermissionsInit();
                } else {
                    Log.d(TAG, "notification permission denied");
                    permissionDenied();
                }
            }
            Log.d(TAG,"Just got notification request result.");
        }
    }

    private void setupLocationListener() {
        keepOn = false;

//        mMap.getUiSettings().setMapToolbarEnabled(false);

        Log.d(TAG, "toolbar enabled: " + String.valueOf(mMap.getUiSettings().isMapToolbarEnabled()));


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (checkPermission() && locationManager != null)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 15, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationManager != null && locationListener != null)
            locationManager.removeUpdates(locationListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission() && (locationManager != null) && (locationListener != null))
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
    }

    public void updateLocation(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        latLonHistory.add(latLng); // Add the LL to our location history

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomDefault));
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(12);
            llHistoryPolyline.setColor(getColor(R.color.dark_green));

            float r = getRadius();
            if (r > 0) {
                Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);
                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.rotation(location.getBearing());

                if (walker != null) {
                    walker.remove();
                }

                walker = mMap.addMarker(options);
            }
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        return (factor * multiplier);
    }

    void permissionDenied() {

        Log.d(TAG,"in permission denied");

//        binding.blackLayout.setVisibility(View.VISIBLE);

        keepOn = false;

        LayoutInflater inflater = getLayoutInflater();
        View dialog = inflater.inflate(R.layout.alert_layout, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialog)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

        builder.show();
    }

    private double getAngle(Location newLocation, Location prevLocation) {
        double lonDiff = newLocation.getLongitude() - prevLocation.getLongitude();
        double latDiff = newLocation.getLatitude() - prevLocation.getLatitude();
        double angle = Math.atan2(lonDiff,latDiff);
        return Math.toDegrees(angle) + 90;
    }

}