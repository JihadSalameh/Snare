package com.example.snare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            startActivity(new Intent(MainActivity.this, NotesActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

    public void Signup(View view) {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
        finish();
    }

    public void Login(View view) {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }
}