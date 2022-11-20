package com.example.snare;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private ImageView profileImg;
    private TextView editProfileImg;
    private TextView username;
    private TextView dob;
    private Button save;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        firebaseDatabase = FirebaseDatabase.getInstance();
        reference = firebaseDatabase.getReference();

        profileImg = findViewById(R.id.profileImgSettings);
        editProfileImg = findViewById(R.id.editProfileImg);
        username = findViewById(R.id.usernameLbl);
        dob = findViewById(R.id.dobLbl);
        save = findViewById(R.id.saveBtn);
    }
}