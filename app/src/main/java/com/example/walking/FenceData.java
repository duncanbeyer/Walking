package com.example.walking;

import android.graphics.Color;
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
    private int fenceColor;
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
            fenceColor = hexToColor(j.getString("fenceColor"));
        } catch (Exception e) {}
        try {
            image = j.getString("image");
        } catch (Exception e) {}

    }

    protected FenceData(Parcel in) {
        id = in.readString();
        address = in.readString();
        description = in.readString();
        fenceColor = Integer.parseInt(in.readString());
        image = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        radius = in.readDouble();
    }

    public static final Creator<FenceData> CREATOR = new Creator<FenceData>() {
        @Override
        public FenceData createFromParcel(Parcel in) {
            return new FenceData(in);
        }

        @Override
        public FenceData[] newArray(int size) {
            return new FenceData[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public int getFenceColor() {
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

    public static int hexToColor(String hex) {
        if (hex != null && hex.length() == 7 && hex.startsWith("#")) {
            try {
                int rgb = Integer.parseInt(hex.substring(1), 16);
                return rgb;
            } catch (Exception e) {
                Log.d(TAG,"Error converting color: ", e);
            }
        }
        return Color.BLACK;
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
        parcel.writeString(String.valueOf(fenceColor));
        parcel.writeString(image);
        parcel.writeParcelable(latLng, i);
        parcel.writeDouble(radius);
    }
}
