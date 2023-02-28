package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.R;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.firebaseRef.FirebasePinnedLocations;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    EditText locationName;
    Button save, cancel;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final float DEFAULT_ZOOM = 15;
    Boolean locationPermissionGranted = false;
    GoogleMap map;
    SupportMapFragment mapFragment;
    private final LatLng defaultLocation  =new LatLng(31.898043, 35.204269);
    Map<String, Object> places = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    public void createNewLocationDialog(LatLng loc) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View locationPopUp = getLayoutInflater().inflate(R.layout.popup, null);

        locationName = locationPopUp.findViewById(R.id.locationName);
        save = locationPopUp.findViewById(R.id.save);
        cancel = locationPopUp.findViewById(R.id.cancel);

        dialogBuilder.setView(locationPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        save.setOnClickListener(view -> {
            PinnedLocations location = new PinnedLocations();
            location.setName(locationName.getText().toString().trim());
            location.setLat(String.valueOf(loc.latitude));
            location.setLng(String.valueOf(loc.longitude));

            savePinnedLocation(location);

            locationName.setText("");
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
    }

    private void savePinnedLocation(PinnedLocations location) {

        class SavePinnedLocationTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                PinnedLocationsDataBase.getDatabase(getApplicationContext()).pinnedLocationsDao().Insert(location);
                FirebasePinnedLocations firebasePinnedLocations = new FirebasePinnedLocations();
                firebasePinnedLocations.save(location);
                return null;
            }
        }

        new SavePinnedLocationTask().execute();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map=googleMap;
        googleMap.setOnCameraMoveListener(() -> {
            map.clear();
            LatLng latLng = new LatLng(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
            map.addMarker(new MarkerOptions().position(latLng));
        });

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(defaultLocation.latitude,
                        defaultLocation.longitude), DEFAULT_ZOOM));

        getLocationPermission();
        updateLocationUI();
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            System.out.println(e.getMessage());
        }
    }

    public void addPlace(View view) {
        LatLng loc = new LatLng(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
        createNewLocationDialog(loc);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Address> addressList=null;
                //Need to add auto filler
                //error if location entered
                Geocoder geocoder = new Geocoder(MapActivity.this);

                try{
                    addressList = geocoder.getFromLocationName(query,1);
                } catch(IOException e){
                    e.printStackTrace();
                }

                assert addressList != null;
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                map.addMarker(new MarkerOptions().position(latLng).title(query));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

}