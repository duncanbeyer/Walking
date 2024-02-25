package com.example.walking;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.walking.databinding.ActivityFenceInfoBinding;

public class FenceInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFenceInfoBinding binding = ActivityFenceInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (getIntent().hasExtra("FENCE_ID")) {
            String id = getIntent().getStringExtra("FENCE_ID");
            binding.fenceId.setText(id);
        }
        if (getIntent().hasExtra("FENCE_TRANS")) {
            String id = getIntent().getStringExtra("FENCE_TRANS");
            binding.transType.setText(id);
        }
    }
}