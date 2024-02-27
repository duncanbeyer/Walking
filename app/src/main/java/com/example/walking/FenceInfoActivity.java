package com.example.walking;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walking.databinding.ActivityFenceInfoBinding;
import com.squareup.picasso.Picasso;

public class FenceInfoActivity extends AppCompatActivity {
    
    private FenceData fd;
    private static final String TAG = "FenceInfoActivity";
    private ImageView pic;
    private TextView name;
    private TextView address;
    private TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFenceInfoBinding binding = ActivityFenceInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        pic = binding.image;
        name = binding.name;
        address = binding.address;
        content = binding.content;

        if (getIntent().hasExtra("BUILDING")) {
            fd = getIntent().getParcelableExtra("BUILDING");
        }
        else {
            Log.d(TAG,"no fence data.");
            return;
        }

        Picasso.get().load(fd.getImage()).into(pic);

        name.setText(fd.getId());
        address.setText(fd.getAddress());
        content.setText(fd.getDescription());

    }
}