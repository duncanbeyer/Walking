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
import android.app.NotificationManager;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
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

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private FenceManager fenceMgr;
    private Polyline polyline1;
    private Polyline polyline2;
    private boolean travelVisibility = true;
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
    private boolean destroyed = false;
    boolean showAddress = true;
    ArrayList<Circle> circles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getScreenDimensions();

        locationText = findViewById(R.id.address);


        setCheckBoxes();

        geocoder = new Geocoder(this);
        initMap();
    }


    public void initMap() {

        fenceMgr = new FenceManager(this);

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
                    showCircles();
                } else {
                    clearCircles();
                }
            }
        });

        addressCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    showAddress();
                } else {
                    clearAddress();
                }
            }
        });

        tourPathCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    polyline2.setVisible(true);
                } else {
                    polyline2.setVisible(false);
                }
            }
        });

        travelPathCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (llHistoryPolyline != null) {
                        llHistoryPolyline.setVisible(true);
                        travelVisibility = true;
                    }
                } else {
                    if (llHistoryPolyline != null) {
                        llHistoryPolyline.setVisible(false);
                        travelVisibility = false;
                    }
                }
            }
        });
    }

    private void clearCircles() {
        for (Circle c : circles) {
            c.setVisible(false);
        }
    }

    private void showCircles() {
        for (Circle c : circles) {
            c.setVisible(true);
        }
    }

    private void showAddress() {
        showAddress = true;
    }

    private void clearAddress() {
        showAddress = false;
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

        if (checkPermission() && locationManager != null)
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    private void addFence(JSONObject j) {
        FenceData fd = new FenceData(j);
        fenceMgr.addFence(fd);
        fences.add(fd);
        fencesHash.put(fd.getId(),fd);

        int line = fd.getFenceColor();
        int fill = ColorUtils.setAlphaComponent(line, 50);

        circles.add(mMap.addCircle(new CircleOptions()
                .center(fd.getLatLng())
                .radius(fd.getRadius())
                .strokePattern(pattern)
                .strokeColor(line)
                .fillColor(fill)));
    }




    public void downloadFailed(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Geofence Download Failed");
        builder.setMessage(s);
        builder.setMessage(s);
        builder.setPositiveButton("ok", (dialog, id) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void drawTourPath() {

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

        if (destroyed) {
        }

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        doAddress(latLng);
        latLonHistory.add(latLng);

        if (llHistoryPolyline != null) {
            llHistoryPolyline.remove();
        }

        if (latLonHistory.size() == 1) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            return;
        }

        if (latLonHistory.size() > 1) {
            PolylineOptions polylineOptions = new PolylineOptions();

            for (LatLng ll : latLonHistory) {
                polylineOptions.add(ll);
            }
            llHistoryPolyline = mMap.addPolyline(polylineOptions);
            llHistoryPolyline.setEndCap(new RoundCap());
            llHistoryPolyline.setWidth(8);
            llHistoryPolyline.setColor(getColor(R.color.dark_green));
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

        prevLocation = location;

        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private float getRadius() {
        float z = mMap.getCameraPosition().zoom;
        float factor = (float) ((35.0 / 2.0 * z) - (355.0 / 2.0));
        float multiplier = ((7.0f / 7200.0f) * screenWidth) - (1.0f / 20.0f);
        return factor * multiplier;
    }

    private void doAddress(LatLng ll) {
        if (!showAddress) {
            if (locationText.getText().length() > 0) {
                locationText.setText("");
            }
            return;
        }
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

            if (ad.getSubThoroughfare() == null | ad.getThoroughfare() == null) {
                locationText.setText(ll.toString());
            }
            else {
                locationText.setText(a);
            }

        } catch (Exception e) {
            Log.d(TAG,"Error in doAddress: ", e);
            locationText.setText(ll.toString());
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

            for (int i = 0;i < fencesArr.length();i++) {
                addFence(fencesArr.getJSONObject(i));
            }
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
            drawTourPath();


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

        destroyed = true;
        stopGeoService();
        GeoReceiver.clearAllNotifications(this);

        super.onDestroy();

    }


    private void getBackgroundLocPerm() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                    BACKGROUND_LOCATION_REQUEST);
        } else {
        }
    }

    private void startGeoService() {

        Intent intent = new Intent(this, GeofenceService.class);
        intent.putExtra("FENCES", fencesHash);
        ContextCompat.startForegroundService(this, intent);
    }

    private void getScreenDimensions() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }

    private void stopGeoService() {
        Intent intent = new Intent(this, GeofenceService.class);
        intent.setAction(GeofenceService.STOP);
        stopService(intent);
    }


}