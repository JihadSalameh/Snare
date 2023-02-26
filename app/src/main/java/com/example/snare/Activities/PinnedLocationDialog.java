package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.adapters.ViewPinnedAdapter;
import com.example.snare.listeners.PinnedLocationListener;

import java.util.ArrayList;
import java.util.List;

public class PinnedLocationDialog extends Dialog {

    private RecyclerView pinnedRecycleView;
    private List<PinnedLocations> pinnedLocations;
    private ViewPinnedAdapter pinnedLocationsAdapter;

    public PinnedLocationDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_pinned_location_dialg);
    }

    public void setDialog(PinnedLocationListener pinnedLocationListener){
        pinnedRecycleView = findViewById(R.id.pinnedRecycleView);
        pinnedRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        pinnedLocations = new ArrayList<>();
        getLocations();
        pinnedLocationsAdapter = new ViewPinnedAdapter(pinnedLocations,pinnedLocationListener);
        pinnedRecycleView.setAdapter(pinnedLocationsAdapter);
    }

    private void getLocations() {
        PinnedLocationFirebase pinnedLocationFirebase = new PinnedLocationFirebase();
        pinnedLocationFirebase.getAllLocations(new PinnedLocationFirebase.PinnedCallback() {

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onPinnedRetrieved(List<PinnedLocations> pinnedLocations) {
                PinnedLocationDialog.this.pinnedLocations.addAll(pinnedLocations);
                pinnedLocationsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onPinnedRetrieveError(String error) {

            }
        });
    }
}