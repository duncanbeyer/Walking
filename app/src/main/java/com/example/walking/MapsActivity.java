package com.example.walking;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.FragmentActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.Manifest;
import android.location.Address;
import android.location.Geocoder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    // Needs:
    //      implementation 'com.google.android.gms:play-services-maps:17.0.0'
    //      implementation 'com.google.android.gms:play-services-location:17.0.0'

    //      ACCESS_FINE_LOCATION
    //      ACCESS_BACKGROUND_LOCATION (with Allow in Settings => Allow all the time

    // To test, set location to: 700 S Wabash
    // Then run app
    // Make route from 700 S Wabash
    // to 189 N Michigan Ave

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FenceManager fenceMgr;
    private Polyline polyline1;
    private Polyline polyline2;
    private boolean travelVisibility = false;
    private Location prevLocation;
    private static final int LOCATION_REQUEST = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 222;
    private final List<PatternItem> pattern = Collections.singletonList(new Dot());
    private final ArrayList<LatLng> latLonHistory = new ArrayList<>();
    private final ArrayList<LatLng> points = new ArrayList<>();
    public static int screenHeight;
    public static int screenWidth;
    private Polyline llHistoryPolyline;
    private Marker marker;
    private LocationManager locationManager;
    private ArrayList<FenceData> fences = new ArrayList<>();
    private static HashMap<String, FenceData> fencesHash = new HashMap<>();
    private TextView locationText;
    private LocationListener locationListener;
    private Geocoder geocoder;
    CheckBox geofenceCheckBox;
    CheckBox tourPathCheckBox;
    CheckBox addressCheckBox;
    CheckBox travelPathCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Log.d(TAG,"onCreate: ");
        getScreenDimensions();

        locationText = findViewById(R.id.address);


        setCheckBoxes();

        geocoder = new Geocoder(this);
        initMap();
    }


    public void initMap() {

        fenceMgr = new FenceManager(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setCheckBoxes() {

        geofenceCheckBox = findViewById(R.id.geofence_text_checkBox);
        addressCheckBox = findViewById(R.id.address_text_checkBox);
        tourPathCheckBox = findViewById(R.id.tour_path_text_checkBox);
        travelPathCheckBox = findViewById(R.id.travel_path_text_checkBox);

        geofenceCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG,"checked geofenceCheckBox");
                } else {
                    Log.d(TAG,"UNchecked geofenceCheckBox");
                }
            }
        });

        addressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG,"checked addressCheckBox");
                } else {
                    Log.d(TAG,"UNchecked addressCheckBox");
                }
            }
        });

        tourPathCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG,"checked tourPathCheckBox");
                    if (polyline2 == null) {
                        drawTourPath();
                    }
                    else {
                        polyline2.setVisible(true);
                    }
                } else {
                    Log.d(TAG,"UNchecked tourPathCheckBox");
                    if (polyline2 != null) {
                        polyline2.setVisible(false);
                    }
                }
            }
        });

        travelPathCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.d(TAG,"checked travelPathCheckBox");
                    if (llHistoryPolyline != null) {
                        llHistoryPolyline.setVisible(true);
                        travelVisibility = true;
                    }
                } else {
                    Log.d(TAG,"UNchecked travelPathCheckBox");
                    if (llHistoryPolyline != null) {
                        llHistoryPolyline.setVisible(false);
                        travelVisibility = false;
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        if (checkPermission()) {
            setupLocationListener();
            downloadData();
        }
    }



    private boolean checkPermission() {
        ArrayList<String> perms = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            perms.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                perms.add(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!perms.isEmpty()) {
            String[] array = perms.toArray(new String[0]);
            ActivityCompat.requestPermissions(this,
                    array, LOCATION_REQUEST);
            return false;
        }

        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void setupLocationListener() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener(this);

        //minTime	    long: minimum time interval between location updates, in milliseconds
        //minDistance	float: minimum distance between location updates, in meters
        if (checkPermission() && locationManager != null)
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private void addFence(JSONObject j) {
        FenceData fd = new FenceData(j);
        fenceMgr.addFence(fd);
        fences.add(fd);
        fencesHash.put(fd.getId(),fd);

        // Just to see the fence
        int line = fd.getFenceColor();
        int fill = ColorUtils.setAlphaComponent(line, 50);

        mMap.addCircle(new CircleOptions()
                .center(fd.getLatLng())
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill));
    }




    public void downloadFailed(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Geofence Download Failed");
        builder.setMessage(s);
        builder.setPositiveButton("ok", (dialog, id) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void drawTourPath() {
//        findViewById(R.id.progressBar2).setVisibility(View.INVISIBLE);

        if (polyline2 != null) {
            return;
        }
        PolylineOptions polylineOptions = new PolylineOptions();

        for (LatLng ll : points) {
            polylineOptions.add(ll);
        }
        polyline2 = mMap.addPolyline(polylineOptions);
        polyline2.setEndCap(new RoundCap());
        polyline2.setWidth(15);
        polyline2.setColor(getColor(R.color.yellow));

    }

    public void updateLocation(Location location) {

        if (prevLocation == null) {
            prevLocation = location;
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        doAddress(latLng);
        latLonHistory.add(latLng); // Add the LL to our location history

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove(); // Remove old polyline
        }

        if (latLonHistory.size() == 1) { // First update
            mMap.addMarker(new MarkerOptions().alpha(0.5f).position(latLng).title("My Origin"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            return;
        }

        if (latLonHistory.size() > 1) { // Second (or more) update
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(Color.BLUE);
            if (travelVisibility) {
                llHistoryPolyline.setVisible(true);
            }
            else {
                llHistoryPolyline.setVisible(false);

            }

            float r = getRadius();
            if (r > 0) {

                double lonDiff = location.getLongitude() - prevLocation.getLongitude();
                double latDiff = location.getLatitude() - prevLocation.getLatitude();
                double angle = Math.atan2(lonDiff,latDiff);
                angle = Math.toDegrees(angle) + 90;
                Bitmap icon;

                if (angle > 45.0 && angle <= 135.0) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_up);
                }
                else if (angle > 135 && angle <= 225.0) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_right);
                }
                else if (angle > 225.0 && angle <= 315.0) {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_down);
                }
                else {
                    icon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_left);
                }

                Bitmap resized = Bitmap.createScaledBitmap(icon, (int) r, (int) r, false);

                BitmapDescriptor iconBitmap = BitmapDescriptorFactory.fromBitmap(resized);

                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.icon(iconBitmap);
                options.anchor(0.5f,0.5f);

                if (marker != null) {
                    marker.remove();
                }

                marker = mMap.addMarker(options);
            }
        }
//        Log.d(TAG, "updateLocation: " + mMap.getCameraPosition().zoom);

        prevLocation = location;

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        sumIt();
    }

    private void sumIt() {
        double sum = 0;
        LatLng last = latLonHistory.get(0);
        for (int i = 1; i < latLonHistory.size(); i++) {
            LatLng current = latLonHistory.get(i);
            sum += SphericalUtil.computeDistanceBetween(current, last);
            last = current;
        }
//        Log.d(TAG, "sumIt: " + String.format("%.3f km", sum/1000.0));

    }
    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        return factor * multiplier;
    }

    private void doAddress(LatLng ll) {
        try {

            List<Address> addresses;
            addresses = geocoder.getFromLocation(ll.latitude, ll.longitude, 10);
            Address ad = addresses.get(0);
            String a = String.format("%s %s %s %s %s %s",
                    (ad.getSubThoroughfare() == null ? "" : ad.getSubThoroughfare()),
                    (ad.getThoroughfare() == null ? "" : ad.getThoroughfare()),
                    (ad.getLocality() == null ? "" : ad.getLocality()),
                    (ad.getAdminArea() == null ? "" : ad.getAdminArea()),
                    (ad.getPostalCode() == null ? "" : ad.getPostalCode()),
                    (ad.getCountryName() == null ? "" : ad.getCountryName()));
            locationText.setText(a);
        } catch (Exception e) {
            Log.d(TAG,"Error in doAddress: ", e);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int permNum = permissions.length + 1;
        int permCount = 0;

        if (requestCode == LOCATION_REQUEST) {

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)
                permCount++;

            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getBackgroundLocPerm();
                permCount++;
            }

            if (permissions.length == 2) {
                if (permissions[1].equals(Manifest.permission.POST_NOTIFICATIONS) &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permCount++;
                }
            }

            if (permCount == permNum) {
                setupLocationListener();
                downloadData();
            }

        }
        else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setupLocationListener();
                downloadData();
            }
        }
    }

    public void downloadData() {

        RequestQueue queue = Volley.newRequestQueue(this);

        String ex = "https://www.christopherhield.com/data/WalkingTourContent.json";

        JsonObjectRequest jsonObjectRequest =
                new JsonObjectRequest(Request.Method.GET, ex,
                        null,
                        response -> {
                            jsonToArr(response);
                        },
                        error -> {
                            Log.e(TAG, "Exception getting JSON data: " + error.getMessage());
                            downloadFailed("download sucky");
                }) {
                };

        queue.add(jsonObjectRequest);

    }

    public static FenceData getFenceData(String fenceId) {
        return fencesHash.get(fenceId);
    }


    private void jsonToArr(JSONObject j) {

        try {

            JSONArray fencesArr;
            JSONArray pathArr;

            fencesArr = j.getJSONArray("fences");
            pathArr = j.getJSONArray("path");


            Log.d(TAG,"fencesArr size: " + fencesArr.length());
            Log.d(TAG,"pathArr size: " + pathArr.length());

            for (int i = 0;i < fencesArr.length();i++) {
                addFence(fencesArr.getJSONObject(i));
            }
            Log.d(TAG, "just added fences");
            String s;
            String[] parts;
            LatLng l;
            for (int i = 0;i < pathArr.length();i++) {
                s = pathArr.getString(i);
                parts = s.split(", ");
                l = new LatLng(Double.parseDouble(parts[1]), Double.parseDouble(parts[0]));
                points.add(l);
            }

            startGeoService();

        } catch (Exception e) {
            Log.d(TAG, "Exception loading JSON: " + e);
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

    @Override
    protected void onDestroy() {

        locationManager.removeUpdates(locationListener);
        super.onDestroy();

    }


    private void getBackgroundLocPerm() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "NEED BASIC PERMS FIRST!", Toast.LENGTH_LONG).show();
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
            Log.d(TAG,"just requested background");
        } else {
            Toast.makeText(this, "ALREADY HAS BACKGROUND LOC PERMS", Toast.LENGTH_LONG).show();
        }
    }

    private void startGeoService() {

        //starting service
        Intent intent = new Intent(this, GeofenceService.class);
        intent.putExtra("FENCES", fencesHash);
        Log.d(TAG,"STARTING GEOFENCE SERVICE.");
        Log.d(TAG,"sending fencesHash: " + fencesHash.toString());
        ContextCompat.startForegroundService(this, intent);
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }
}