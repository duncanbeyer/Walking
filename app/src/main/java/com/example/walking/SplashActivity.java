package com.example.walking;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SplashActivity extends AppCompatActivity {
    private boolean keepOn = true;
    private static final int LOCATION_REQUEST = 111;
    private static final int BACKGROUND_LOCATION_REQUEST = 222;
    private static final String TAG = "SplashActivity";
    private boolean hasAlready = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SplashScreen.installSplashScreen(this)
                .setKeepOnScreenCondition(
                        new SplashScreen.KeepOnScreenCondition() {
                            @Override
                            public boolean shouldKeepOnScreen() {
                                return keepOn;
                            }
                        }
                );
        if (checkPermission()) {
            //sleep 2 seconds
            try {
                Log.d(TAG,"about to sleep");
                Thread.sleep(2000); // Sleep for 2 seconds
            } catch (InterruptedException e) {
                Log.d(TAG,"sleep failed ", e);
            }
            goOn();
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
            else {
                goDenied();
            }

            if (permissions.length == 2) {
                if (permissions[1].equals(Manifest.permission.POST_NOTIFICATIONS) &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    permCount++;
                }
                else if (permissions[1].equals(Manifest.permission.POST_NOTIFICATIONS) &&
                        grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    goDenied();
                }
            }

            if (permCount == permNum) {
                goOn();
            }

        }
        else if (requestCode == BACKGROUND_LOCATION_REQUEST) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                goOn();
            }
        }
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
            Log.d(TAG, "just requested background");
        }
    }


    private void goOn() {
        Intent intent = new Intent(this, MapsActivity.class);

        startActivity(intent);
    }


    private void goDenied() {
        keepOn = false;

        String s = "Permissions are needed in order to use the app.";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions Failed");
        builder.setMessage(s);
        builder.setPositiveButton("ok", (dialog, id) -> finish());
        AlertDialog dialog = builder.create();
        dialog.show();

    }


}
