package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.adapters.PinnedLocationsAdapter;
import com.example.snare.dao.PinnedLocationsDao;
import com.example.snare.dao.PinnedLocationsDataBase;

import java.util.ArrayList;
import java.util.List;

public class PinnedLocationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    PinnedLocationsAdapter adapter;
    PinnedLocationsDao pinnedLocationsDao;
    List<PinnedLocations> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_locations);

        recyclerView = findViewById(R.id.locationsRecyclerView);
        pinnedLocationsDao = PinnedLocationsDataBase.getDatabase(this).pinnedLocationsDao();
        list = pinnedLocationsDao.GetAllPinnedLocations();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PinnedLocationsAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        recyclerView = findViewById(R.id.locationsRecyclerView);
        pinnedLocationsDao = PinnedLocationsDataBase.getDatabase(this).pinnedLocationsDao();
        list = pinnedLocationsDao.GetAllPinnedLocations();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PinnedLocationsAdapter(list);
        recyclerView.setAdapter(adapter);
    }

    public void AddLocations(View view) {
        startActivity(new Intent(PinnedLocationsActivity.this, MapActivity.class));
    }
}