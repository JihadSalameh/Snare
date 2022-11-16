package com.example.snare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class NotesActivity extends AppCompatActivity {

    private Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        signOut = findViewById(R.id.SignOutBtn);

        //String auth = FirebaseAuth.getInstance().getCurrentUser().getIdToken(true).toString();

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(NotesActivity.this, "Signed out!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(NotesActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}