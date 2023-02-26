package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.snare.Entities.Group;
import com.example.snare.NotificationsPkg.FCMSend;
import com.example.snare.R;
import com.example.snare.listeners.GroupListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoutsActivity extends AppCompatActivity implements GroupListener {

    AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    EditText shoutText;
    Button send, cancel;

    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    public NavigationView navigationView;

    private GroupLayout popupGroup;

    private DatabaseReference databaseReference;
    private DatabaseReference mDatabase;
    private FirebaseAuth auth;
    private FirebaseUser user;

    private List<String> shoutsUsers;
    private String name = "";

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

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        databaseReference.get().addOnSuccessListener(dataSnapshot -> name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString());

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
        deleteDatabase("notes_db");
        deleteDatabase("notifications_db");
        deleteDatabase("pinnedLocations_db");
        deleteDatabase("reminders_db");

        Toast.makeText(ShoutsActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ShoutsActivity.this, LoginActivity.class));
        finish();
    }

    public void shoutToAll(View view) {
        createNewShoutToAllDialog();
    }

    private void createNewShoutToAllDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View ShoutPopUp = getLayoutInflater().inflate(R.layout.shout, null);

        shoutText = ShoutPopUp.findViewById(R.id.shout_text);
        send = ShoutPopUp.findViewById(R.id.send);
        cancel = ShoutPopUp.findViewById(R.id.cancel);

        dialogBuilder.setView(ShoutPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        send.setOnClickListener(view -> {
            mDatabase = FirebaseDatabase.getInstance().getReference("Users");
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

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

            shoutText.setText("");
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
    }

    public void shoutToGroups(View view) {
        selectGroupToShout();
    }

    private void selectGroupToShout() {
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
        createNewShoutToGroupsDialog();
    }

    private void createNewShoutToGroupsDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View ShoutPopUp = getLayoutInflater().inflate(R.layout.shout, null);

        shoutText = ShoutPopUp.findViewById(R.id.shout_text);
        send = ShoutPopUp.findViewById(R.id.send);
        cancel = ShoutPopUp.findViewById(R.id.cancel);

        dialogBuilder.setView(ShoutPopUp);
        dialog = dialogBuilder.create();
        dialog.show();

        send.setOnClickListener(view -> {
            mDatabase = FirebaseDatabase.getInstance().getReference("Users");
            auth = FirebaseAuth.getInstance();
            user = auth.getCurrentUser();

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

            shoutText.setText("");
            dialog.dismiss();
        });

        cancel.setOnClickListener(view -> dialog.dismiss());
    }

}