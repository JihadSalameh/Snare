package com.example.snare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SyncAdapterType;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotesActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;
    //User user = new User();

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

        //System.out.println(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        databaseReference.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                //user.setName(snapshot.child("name").getValue(String.class));
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ImageView imageView = findViewById(R.id.profileImg);
                    TextView name = findViewById(R.id.nameTxt);
                    TextView email = findViewById(R.id.emailTxtNav);

                    Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(imageView);
                    name.setText(snapshot.child("name").getValue(String.class));
                    email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

                    /*String key = dataSnapshot.getKey();
                    String value = (String) dataSnapshot.getValue();

                    System.out.println(key + " " + value);
                    if(key.equals("dob")) {
                        user.setDob(value);
                    } else if(key.equals("id")) {
                        user.setId(value);
                    } else if(key.equals("name")) {
                        user.setName(value);
                    } else if(key.equals("profilePic")) {
                        user.setProfilePic(value);
                    }*/
                }
            }
        });
        //System.out.println("**********************************" + user.getName());

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getTitle().toString().equals("Logout")) {
                    logout();
                }
                /*else if(item.getTitle().toString().equals("Settings")) {
                    startActivity(new Intent(NotesActivity.this, RegistrationContActivity.class));
                }*/

                return true;
            }
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
        Toast.makeText(NotesActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(NotesActivity.this, MainActivity.class));
        finish();
    }
}