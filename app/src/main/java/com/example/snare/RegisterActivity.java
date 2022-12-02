package com.example.snare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText txt_email;
    private EditText txt_password;
    private EditText txt_confirmPassword;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txt_email = findViewById(R.id.emailTxt);
        txt_password = findViewById(R.id.passwordTxt);
        txt_confirmPassword = findViewById(R.id.confirmPasswordTxt);

        auth = FirebaseAuth.getInstance();
    }

    private void registerUser(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(RegisterActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, RegistrationContActivity.class));
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void HaveAccount(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        finish();
    }

    public void Register(View view) {
        String email = txt_email.getText().toString();
        String password = txt_password.getText().toString();
        String confirmPassword = txt_confirmPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
        } else if(!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Password Doesn't match!", Toast.LENGTH_SHORT).show();
        } else if(password.length() < 8 ) {
            Toast.makeText(RegisterActivity.this, "Password Too Short!", Toast.LENGTH_SHORT).show();
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Wrong Email Format!", Toast.LENGTH_SHORT).show();
        } else {
            registerUser(email, password);
        }
    }
}