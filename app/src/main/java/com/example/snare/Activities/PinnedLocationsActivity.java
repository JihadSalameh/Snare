package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.snare.R;

public class PinnedLocationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_locations);
    }

    public void AddLocations(View view) {
        startActivity(new Intent(PinnedLocationsActivity.this, MapActivity.class));
    }
}