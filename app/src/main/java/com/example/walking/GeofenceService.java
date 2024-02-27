package com.example.walking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import android.app.PendingIntent;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Collection;
import java.util.HashMap;

public class GeofenceService extends Service {

    private static final String TAG = "GeofenceService";
    private Notification notification;
    private final String channelId = "FENCE_CHANNEL";
    private PendingIntent geofencePendingIntent;
    private GeofencingClient geofencingClient;
    public static HashMap<String, FenceData> fences = new HashMap<String, FenceData>();
    public static final String STOP = "STOP";

    @Override
    public void onCreate() {
        super.onCreate();

        geofencingClient = LocationServices.getGeofencingClient(this);

        geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnSuccessListener(this.getMainExecutor(),
                        aVoid -> Log.d(TAG, "onSuccess: removeGeofences"))
                .addOnFailureListener(this.getMainExecutor(),
                        e -> {
                            Log.d(TAG, "onFailure: removeGeofences: ", e);
                        });

        // Since we need to post a notification, we first create a channel
        createNotificationChannel();

        // Create a notification required when running a foreground service.
        notification = new NotificationCompat.Builder(this, channelId)
                .build();
    }

    private void createNotificationChannel() {
        Uri soundUri = Uri.parse("android.resource://" +
                this.getPackageName() + "/" +
                R.raw.notify_sound);
        AudioAttributes att = new AudioAttributes.Builder().
                setUsage(AudioAttributes.USAGE_NOTIFICATION).build();


        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(channelId, channelId, importance);
        mChannel.setSound(soundUri, att);
        mChannel.setLightColor(Color.YELLOW);
        mChannel.setVibrationPattern(new long[]{0, 300, 100, 300});

        NotificationManager mNotificationManager = getSystemService(NotificationManager.class);
        mNotificationManager.createNotificationChannel(mChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called in GeofenceService.");

        if (intent != null) {
            String action = intent.getAction();
            if (GeofenceService.STOP.equals(action)) {
                Log.d(TAG,"stopping GeofenceService.");
                NotificationManager notificationManager = (NotificationManager) this
                        .getSystemService(Context.NOTIFICATION_SERVICE);

                if (notificationManager != null) {
                    notificationManager.cancelAll();
                }
                onDestroy();
            }
        }


        fences = (HashMap<String, FenceData>) intent.getSerializableExtra("FENCES");
        Log.d(TAG,"just init fences in GeofenceService");
        Log.d(TAG,"Fences is: " + fences.toString());
        if (fences != null)
            makeFences(fences.values());

        // Start the service in the foreground
        startForeground(1, notification);

        // If the service is killed, restart it
        return Service.START_STICKY;
    }

    private void makeFences(Collection<FenceData> fences) {
        for (FenceData fd : fences) {
            Log.d(TAG, "makeFences: " + fd);
            addFence(fd);
        }
    }

    public void addFence(FenceData fd) {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Geofence geofence = new Geofence.Builder()
                .setRequestId(fd.getId())
                .setCircularRegion(
                        fd.getLatLng().latitude,
                        fd.getLatLng().longitude,
                        fd.getRadius())
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setExpirationDuration(Geofence.NEVER_EXPIRE) //Fence expires after N millis  -or- Geofence.NEVER_EXPIRE
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .build();

        geofencePendingIntent = getGeofencePendingIntent();


        geofencingClient
                .addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Log.d(TAG, "onFailure: addGeofences: " + e.getMessage());
                });
    }

    private PendingIntent getGeofencePendingIntent() {

        Log.d(TAG,"in getGeofencePendingIntent()");

        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent intent = new Intent(this, GeoReceiver.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().

        geofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


        return geofencePendingIntent;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy in GeofenceService");

        super.onDestroy();
    }
}