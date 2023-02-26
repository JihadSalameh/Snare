package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.adapters.PinnedLocationsAdapter;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.firebase.FirebasePinnedLocations;

import java.util.ArrayList;
import java.util.List;

public class PinnedLocationsActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_SHOW_PINNED_LOCATIONS = 1;
    RecyclerView recyclerView;
    PinnedLocationsAdapter adapter;
    List<PinnedLocations> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pinned_locations);

        recyclerView = findViewById(R.id.locationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        list = new ArrayList<>();
        adapter = new PinnedLocationsAdapter(list);
        recyclerView.setAdapter(adapter);
        getAllPinnedLocations(REQUEST_CODE_SHOW_PINNED_LOCATIONS);
    }

    private void getAllPinnedLocations(int requestCode) {
        list.clear();

        class GetPinnedLocationsTask extends AsyncTask<Void, Void, List<PinnedLocations>> {

            @Override
            protected List<PinnedLocations> doInBackground(Void... voids) {
                if(isNetworkAvailable(getApplicationContext())) {
                    FirebasePinnedLocations firebasePinnedLocations = new FirebasePinnedLocations();
                    firebasePinnedLocations.getAllPinnedLocations(new FirebasePinnedLocations.PinnedLocationsCallback() {
                        @SuppressLint("NotifyDataSetChanged")
                        @Override
                        public void onPinnedLocationsRetrieved(List<PinnedLocations> pinnedLocations) {
                            if(pinnedLocations != null) {
                                if(requestCode == REQUEST_CODE_SHOW_PINNED_LOCATIONS) {
                                    list.addAll(pinnedLocations);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }

                        @Override
                        public void onPinnedLocationsRetrieveError(String error) {
                            System.out.println(error);
                        }
                    });

                    return null;
                } else {
                    return PinnedLocationsDataBase.getDatabase(getApplicationContext()).pinnedLocationsDao().GetAllPinnedLocations();
                }
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<PinnedLocations> pinnedLocations) {
                super.onPostExecute(pinnedLocations);

                if(pinnedLocations != null) {
                    if(requestCode == REQUEST_CODE_SHOW_PINNED_LOCATIONS) {
                        list.addAll(pinnedLocations);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }

        new GetPinnedLocationsTask().execute();
    }

    public  boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        getAllPinnedLocations(REQUEST_CODE_SHOW_PINNED_LOCATIONS);
    }

    public void AddLocations(View view) {
        startActivity(new Intent(PinnedLocationsActivity.this, MapActivity.class));
    }
}