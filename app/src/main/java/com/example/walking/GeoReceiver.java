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

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

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

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) return;

        String transitionString = transitionType == Geofence.GEOFENCE_TRANSITION_ENTER
                ? "Welcome!" : "Goodbye!";

        FenceData fenceData = GeofenceService.fences.get(id);


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
                .setContentTitle(fenceData.getId() + " (Tap to see details)")
                .setSubText(fenceData.getId())
                .setContentText(fenceData.getAddress())
                .setAutoCancel(true)
                .build();

        notificationManager.notify(getUniqueId(), notification);
    }

    private static int getUniqueId() {
        return(int) (System.currentTimeMillis() % 10000);
    }


    public static void clearAllNotifications(Context context) {

        closed = true;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    public static void clearAllNotificationsInit(Context context) {
        
        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

}
