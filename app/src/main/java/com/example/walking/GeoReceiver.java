package com.example.walking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

public class GeoReceiver extends BroadcastReceiver {

    private static final String TAG = "GeoReceiver";
    private static final String CHANNEL_ID = "FENCE_CHANNEL";
    static boolean closed = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"just received geofencing event.");

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (closed) {
            return;
        }
        if (geofencingEvent == null) {
            Log.d(TAG, "onReceive: NULL GeofencingEvent received");
            return;
        }

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Error: " + geofencingEvent.getErrorCode());
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeringGeofences != null) {
                for (Geofence g : triggeringGeofences) {
                    FenceData fd = MapsActivity.getFenceData(g.getRequestId());
                    sendNotification(context, fd.getId(), geofenceTransition);
                }
            }
        }
    }

    public void sendNotification(Context context, String id, int transitionType) {

        Log.d(TAG,"in sendNotification");

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

//        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
//
//            Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notify_sound);
//            AudioAttributes att = new AudioAttributes.Builder().
//                    setUsage(AudioAttributes.USAGE_NOTIFICATION).build();
//
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
//            mChannel.setSound(soundUri, att);
//            mChannel.setLightColor(Color.RED);
//            mChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
//            mChannel.setShowBadge(true);
//
//            notificationManager.createNotificationChannel(mChannel);
//
//        }

        ////
        String transitionString = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                ? "Welcome!" : "Goodbye!";

        FenceData fenceData = GeofenceService.fences.get(id);
        Log.d(TAG,"About to be here we are");
        Log.d(TAG,"Here we are " + fenceData.toString());


        Intent resultIntent = new Intent(context.getApplicationContext(), FenceInfoActivity.class);
        resultIntent.putExtra("FENCE_ID", id);
        resultIntent.putExtra("BUILDING", fenceData);

        resultIntent.putExtra("FENCE_TRANS", transitionString);

        PendingIntent pi = PendingIntent.getActivity(
                context.getApplicationContext(), 0, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);


        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.fence_notif)
                .setContentTitle(fenceData.getId() + "(Tap to see details)") // Bold title
                .setSubText(fenceData.getId()) // Detail info
                .setContentText(fenceData.getAddress()) // Detail info
                .setAutoCancel(true)
                .build();

        notificationManager.notify(getUniqueId(), notification);
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }


    public static void clearAllNotifications(Context context) {

        Log.d(TAG,"clearAllNotifications called");

        closed = true;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

}
