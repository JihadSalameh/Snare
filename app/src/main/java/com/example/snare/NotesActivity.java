package com.example.snare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class NotesActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        drawerLayout = findViewById(R.id.my_drawer_layout);
        navigationView = findViewById(R.id.nav_menu);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fillNavDrawer();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getTitle().toString().equals("Logout")) {
                    logout();
                } else if(item.getTitle().toString().equals("Profile")) {
                    startActivity(new Intent(NotesActivity.this, ProfileActivity.class));
                } else if(item.getTitle().toString().equals("Friends")) {
                    startActivity(new Intent(NotesActivity.this, FriendsActivity.class));
                }

                return true;
            }
        });
    }

    private void fillNavDrawer() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                ImageView imageView = findViewById(R.id.profileImg);
                TextView name = findViewById(R.id.nameTxt);
                TextView email = findViewById(R.id.emailTxtNav);

                Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
                name.setText(snapshot.child("name").getValue(String.class));
                email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fillNavDrawer();
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
        Toast.makeText(NotesActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NotesActivity.this, LoginActivity.class));
        finish();
    }
}