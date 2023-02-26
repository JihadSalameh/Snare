package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.snare.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText txt_email;
    private EditText txt_password;
    private EditText txt_confirmPassword;

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        txt_email = findViewById(R.id.emailTxt);
        txt_password = findViewById(R.id.passwordTxt);
        txt_confirmPassword = findViewById(R.id.confirmPasswordTxt);
    }

    public void Register(View view) {
        String email = txt_email.getText().toString();
        String password = txt_password.getText().toString();
        String confirmPassword = txt_confirmPassword.getText().toString();

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
        } else if(!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Password Doesn't match!", Toast.LENGTH_SHORT).show();
        } else if(!isValid(password)) {
            Toast.makeText(RegisterActivity.this, "Password Too Weak!", Toast.LENGTH_SHORT).show();
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Wrong Email Format!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(RegisterActivity.this, RegistrationContActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
            finish();
        }
    }

    public static boolean isValid(final String password) {
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
}