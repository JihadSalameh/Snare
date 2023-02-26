package com.example.snare.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.snare.Entities.Note;
import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.example.snare.dao.NotesDataBase;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.dao.ReminderDataBase;
import com.example.snare.firebaseRef.FirebaseNotes;
import com.example.snare.firebaseRef.FirebasePinnedLocations;
import com.example.snare.firebaseRef.FirebaseReminders;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.List;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    private EditText email;
    private EditText password;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageView googleImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setActivity();
    }

    private void setActivity() {
        initializeActivity();
        setListeners();
        isUserLogin();
    }

    private void setListeners() {
        setGoogleSignInListener();
    }

    private void setGoogleSignInListener() {
        googleImageView.setOnClickListener(v -> signInGoogle());
    }

    private void isUserLogin(){
        if(user != null) {
            startActivity(new Intent(LoginActivity.this, NotesActivity.class));
            finish();
        }
    }

    private void initializeActivity() {
        email = findViewById(R.id.email_Txt);
        password = findViewById(R.id.password_Txt);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        googleImageView = findViewById(R.id.googleImageView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if(task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        assert user != null;
                        Toast.makeText(LoginActivity.this, user.getEmail(), Toast.LENGTH_SHORT).show();
                        updateUI();
                    } else {
                        Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
        updateTables();
        startActivity(new Intent(LoginActivity.this, NotesActivity.class));
        finish();
    }

    private void LoginUser(String email_txt, String password_txt) {
        SharedPreferences sharedPreferences = getSharedPreferences("User", 0);

        auth.signInWithEmailAndPassword(email_txt, password_txt).addOnSuccessListener(authResult -> updateUI()).addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Wrong Username Or Password!", Toast.LENGTH_SHORT).show());
    }

    private void updateTables() {
        FirebaseReminders firebaseReminders = new FirebaseReminders();
        FirebaseNotes firebaseNotes = new FirebaseNotes();
        FirebasePinnedLocations firebasePinnedLocations = new FirebasePinnedLocations();

        firebaseNotes.getAllNotes(new FirebaseNotes.NotesCallback() {
            @Override
            public void onNotesRetrieved(List<Note> notes) {
                for(Note note: notes) {
                    NotesDataBase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                }
            }

            @Override
            public void onNotesRetrieveError(String error) {
                System.out.println(error);
            }
        });

        firebaseReminders.getAllReminders(new FirebaseReminders.RemindersCallback() {
            @Override
            public void onRemindersRetrieved(List<Reminder> reminders) {
                for(Reminder reminder: reminders) {
                    ReminderDataBase.getDatabase(getApplicationContext()).reminderDao().insertReminder(reminder);
                }
            }

            @Override
            public void onRemindersRetrieveError(String error) {
                System.out.println(error);
            }
        });

        firebasePinnedLocations.getAllPinnedLocations(new FirebasePinnedLocations.PinnedLocationsCallback() {
            @Override
            public void onPinnedLocationsRetrieved(List<PinnedLocations> pinnedLocations) {
                for(PinnedLocations pinnedLocations1: pinnedLocations) {
                    PinnedLocationsDataBase.getDatabase(getApplicationContext()).pinnedLocationsDao().Insert(pinnedLocations1);
                }
            }

            @Override
            public void onPinnedLocationsRetrieveError(String error) {
                System.out.println(error);
            }
        });

    }

    public void Sign_in(View view) {
        String email_txt = email.getText().toString();
        String password_txt = password.getText().toString();

        if(TextUtils.isEmpty(email_txt) || TextUtils.isEmpty(password_txt)) {
            Toast.makeText(this, "Empty Credentials!", Toast.LENGTH_SHORT).show();
        } else if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email_txt).matches()) {
            Toast.makeText(this, "Wrong Email Format!", Toast.LENGTH_SHORT).show();
        } else {
            LoginUser(email_txt, password_txt);
        }
    }

    public void GoToRegister(View view) {
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        finish();
    }
}