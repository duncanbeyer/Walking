package com.example.walking;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

public class FenceData implements Parcelable {
    private static final String TAG = "FenceData";



    private String id;
    private String address;
    private String description;
    private String fenceColor;
    private String image;
    private LatLng latLng;
    private double radius;
    FenceData(JSONObject j) {
        try {
            id = j.getString("id");
        } catch (Exception e) {}
        try {
            address = j.getString("address");
        } catch (Exception e) {}
        try {
            double latitude = j.getDouble("latitude");
            double longitude = j.getDouble("longitude");
            latLng = new LatLng(latitude,longitude);
        } catch (Exception e) {}
        try {
            radius = j.getDouble("radius");
        } catch (Exception e) {}
        try {
            description = j.getString("description");
        } catch (Exception e) {}
        try {
            fenceColor = j.getString("fenceColor");
        } catch (Exception e) {}
        try {
            image = j.getString("image");
        } catch (Exception e) {}

    }
    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getFenceColor() {
        return fenceColor;
    }

    public String getImage() {
        return image;
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

    @NonNull
    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", latLng=" + latLng +
                ", radius=" + radius +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(address);
        parcel.writeString(description);
        parcel.writeString(fenceColor);
        parcel.writeString(image);
        parcel.writeParcelable(latLng, i);
        parcel.writeDouble(radius);
    }
}
