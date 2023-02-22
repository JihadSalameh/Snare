package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.snare.LocationService.LocationForegroundService;
import com.example.snare.R;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class ShoutsActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    private LocationThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shouts);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        navigationView = findViewById(R.id.nav_menu);
        Button service = findViewById(R.id.startService);

        service.setOnClickListener(view -> {
            thread = new LocationThread();
            thread.start();
        });

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.get().addOnSuccessListener(snapshot -> {
            ImageView imageView = findViewById(R.id.profileImg);
            TextView name = findViewById(R.id.nameTxt);
            TextView email = findViewById(R.id.emailTxtNav);

            Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
            name.setText(snapshot.child("name").getValue(String.class));
            email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
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
        deleteDatabase("notes_db");
        deleteDatabase("notifications_db");
        deleteDatabase("pinnedLocations_db");
        deleteDatabase("reminders_db");

        Toast.makeText(ShoutsActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ShoutsActivity.this, LoginActivity.class));
        finish();
    }

    class LocationThread extends Thread {
        @Override
        public void run() {
            startService(new Intent(ShoutsActivity.this, LocationForegroundService.class));
        }
    }

}