package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.snare.Entities.Group;
import com.example.snare.Entities.Shout;
import com.example.snare.NotificationsPkg.FCMSend;
import com.example.snare.R;
import com.example.snare.dao.NotesDataBase;
import com.example.snare.dao.NotificationsDataBase;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.listeners.GroupListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ShoutsActivity extends AppCompatActivity implements GroupListener, OnMapReadyCallback {

    private EditText shoutText;
    private Button send;
    private Button cancel;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    private GroupLayout popupGroup;

    private DatabaseReference databaseReference;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseShouts;
    private FirebaseUser user;

    private LatLng chosenLocation = null;

    private List<String> shoutsUsers;
    private String name = "";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final float DEFAULT_ZOOM = 15;
    Boolean locationPermissionGranted = false;
    GoogleMap map;
    SupportMapFragment mapFragment;
    private final LatLng defaultLocation  =new LatLng(31.898043, 35.204269);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shouts);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        navigationView = findViewById(R.id.nav_menu);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(user).getUid());
        databaseReference.get().addOnSuccessListener(dataSnapshot -> name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());
        mDatabase = FirebaseDatabase.getInstance().getReference("Users");
        mDatabaseShouts = FirebaseDatabase.getInstance().getReference("Shouts");

        fillNavDrawer();
        navOnClickAction();
    }

    @Override
    protected void onStart() {
        super.onStart();

        fillNavDrawer();
    }

    private void navOnClickAction() {
        navigationView.setNavigationItemSelectedListener(item -> {
            if(item.getTitle().toString().equals("Logout")) {
                logout();
            } else if(item.getTitle().toString().equals("Profile")) {
                startActivity(new Intent(ShoutsActivity.this, ProfileActivity.class));
            } else if(item.getTitle().toString().equals("Friends")) {
                startActivity(new Intent(ShoutsActivity.this, FriendsActivity.class));
            } else if(item.getTitle().toString().equals("Notes")) {
                startActivity(new Intent(ShoutsActivity.this, NotesActivity.class));
                finish();
            } else if(item.getTitle().toString().equals("Reminders")) {
                startActivity(new Intent(ShoutsActivity.this, ReminderActivity.class));
                finish();
            } else if(item.getTitle().toString().equals("Notifications")) {
                startActivity(new Intent(ShoutsActivity.this, NotificationsActivity.class));
            } else if(item.getTitle().toString().equals("Pinned Locations")) {
                startActivity(new Intent(ShoutsActivity.this, PinnedLocationsActivity.class));
            }

            return true;
        });
    }

    private void fillNavDrawer() {
        databaseReference.get().addOnSuccessListener(snapshot -> {
            ImageView imageView = findViewById(R.id.profileImg);
            TextView name = findViewById(R.id.nameTxt);
            TextView email = findViewById(R.id.emailTxtNav);

            Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
            name.setText(snapshot.child("name").getValue(String.class));
            email.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();

        //delete all tables
        NotesDataBase.getDatabase(getApplicationContext()).clearAllTables();
        PinnedLocationsDataBase.getDatabase(getApplicationContext()).clearAllTables();
        NotificationsDataBase.getDatabase(getApplicationContext()).clearAllTables();
        ReminderDataBase.getDatabase(getApplicationContext()).clearAllTables();
        deleteDatabase("notes_db");
        deleteDatabase("notifications_db");
        deleteDatabase("pinnedLocations_db");
        deleteDatabase("reminders_db");

        Toast.makeText(ShoutsActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ShoutsActivity.this, LoginActivity.class));
        finish();
    }

    public void shoutToAll(LatLng latLng) {
        if(latLng != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            final View ShoutPopUp = getLayoutInflater().inflate(R.layout.shout, null);

            shoutText = ShoutPopUp.findViewById(R.id.shout_text);
            send = ShoutPopUp.findViewById(R.id.send);
            cancel = ShoutPopUp.findViewById(R.id.cancel);

            dialogBuilder.setView(ShoutPopUp);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            send.setOnClickListener(view -> {
                mDatabase.get().addOnSuccessListener(dataSnapshot -> {
                    for(DataSnapshot users: dataSnapshot.getChildren()) {
                        if(!Objects.requireNonNull(users.getKey()).equals(user.getUid())) {
                            FCMSend.pushNotification(ShoutsActivity.this, Objects.requireNonNull(users.child("token").getValue()).toString(), "Shout from " + name, shoutText.getText().toString());
                        }

                        try {
                            Thread.sleep(100); // Delay for 0.1 second
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                //save shouts to firebase
                LatLng loc = new LatLng(latLng.latitude, latLng.longitude);
                Map<String, Object> s = new HashMap<>();
                Map<String, Object> s1 = new HashMap<>();
                s1.put(name, loc);
                s.put(shoutText.getText().toString(), s1);

                mDatabase.get().addOnSuccessListener(dataSnapshot -> {
                    for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        if (!Objects.requireNonNull(snapshot.getKey()).equals(user.getUid())) {
                            mDatabaseShouts.child(Objects.requireNonNull(snapshot.getKey())).updateChildren(s).addOnFailureListener(System.out::println);
                        }
                    }
                });

                shoutText.setText("");
                dialog.dismiss();
            });

            cancel.setOnClickListener(view -> dialog.dismiss());
        } else {
            Toast.makeText(this, "Select a location first!", Toast.LENGTH_SHORT).show();
        }
    }

    public void shoutToGroups() {
        popupGroup = new GroupLayout(ShoutsActivity.this);
        popupGroup.setDialog(ShoutsActivity.this);
        Window window = popupGroup.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
        popupGroup.show();
    }

    @Override
    public void onGroupClick(Group group, int position) {
        if(shoutsUsers == null){
            shoutsUsers = new ArrayList<>();
        } else {
            shoutsUsers.clear();
        }

        shoutsUsers.addAll(group.getGroupMembers());
        popupGroup.dismiss();
        createNewShoutToGroupsDialog(chosenLocation);
    }

    private void createNewShoutToGroupsDialog(LatLng latLng) {
        if(latLng != null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            final View ShoutPopUp = getLayoutInflater().inflate(R.layout.shout, null);

            shoutText = ShoutPopUp.findViewById(R.id.shout_text);
            send = ShoutPopUp.findViewById(R.id.send);
            cancel = ShoutPopUp.findViewById(R.id.cancel);

            dialogBuilder.setView(ShoutPopUp);
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();

            send.setOnClickListener(view -> {
                mDatabase.get().addOnSuccessListener(dataSnapshot -> {
                    for(DataSnapshot users: dataSnapshot.getChildren()) {
                        if(!Objects.requireNonNull(users.getKey()).equals(user.getUid()) && shoutsUsers.contains(users.getKey())) {
                            FCMSend.pushNotification(ShoutsActivity.this, Objects.requireNonNull(users.child("token").getValue()).toString(), "Shout from " + name, shoutText.getText().toString());
                        }

                        try {
                            Thread.sleep(100); // Delay for 0.1 second
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                //save shouts to firebase
                LatLng loc = new LatLng(latLng.latitude, latLng.longitude);
                Map<String, Object> s = new HashMap<>();
                Map<String, Object> s1 = new HashMap<>();
                s1.put(name, loc);
                s.put(shoutText.getText().toString(), s1);
                for(String token: shoutsUsers) {
                    mDatabaseShouts.child(token).updateChildren(s).addOnFailureListener(System.out::println);
                }

                shoutText.setText("");
                dialog.dismiss();
            });

            cancel.setOnClickListener(view -> dialog.dismiss());
        } else {
            Toast.makeText(this, "Select a location first!", Toast.LENGTH_SHORT).show();
        }

    }

    public void addShout(View view) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View ShoutPopUp1 = getLayoutInflater().inflate(R.layout.shoutpopup, null);

        Button toAll = ShoutPopUp1.findViewById(R.id.shoutAll);
        Button toGroups = ShoutPopUp1.findViewById(R.id.shoutGroup);
        Button cancel1 = ShoutPopUp1.findViewById(R.id.cancel);

        dialogBuilder.setView(ShoutPopUp1);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        toAll.setOnClickListener(view12 -> {
            shoutToAll(chosenLocation);
            dialog.dismiss();
        });

        toGroups.setOnClickListener(view13 -> {
            shoutToGroups();
            dialog.dismiss();
        });

        cancel1.setOnClickListener(view1 -> dialog.dismiss());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map=googleMap;

        ArrayList<Shout> shouts = new ArrayList<>();
        String[] temp_name = new String[1];
        String[] text = new String[1];

        mDatabaseShouts.child(user.getUid()).get().addOnSuccessListener(dataSnapshot -> {
            for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                LatLng temp = null;
                for(DataSnapshot snapshot1: snapshot.getChildren()) {
                    double x = Double.parseDouble(Objects.requireNonNull(snapshot1.child("latitude").getValue()).toString());
                    double y = Double.parseDouble(Objects.requireNonNull(snapshot1.child("longitude").getValue()).toString());
                    temp = new LatLng(x, y);
                    temp_name[0] = snapshot1.getKey();
                }
                text[0] = snapshot.getKey();
                Shout shout = new Shout(text[0], temp_name[0], temp);

                //save all shouts
                shouts.add(shout);

                //keeps changing when moving on the Screen (need to make it fixed) -> might be fixed
                map.addMarker(new MarkerOptions().position(Objects.requireNonNull(temp)).title(temp_name[0] + ": " + text[0]));
            }
        });

        googleMap.setOnMapLongClickListener(latLng -> {
            //to clear map when long clicking to choose a location
            map.clear();

            chosenLocation = latLng;
            map.addMarker(new MarkerOptions().position(latLng));
        });

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(defaultLocation.latitude,
                        defaultLocation.longitude), DEFAULT_ZOOM));

        getLocationPermission();
        updateLocationUI();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

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
                Geocoder geocoder = new Geocoder(ShoutsActivity.this);

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