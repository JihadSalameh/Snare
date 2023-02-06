package com.example.snare.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snare.R;
import com.example.snare.Entities.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class RegistrationContActivity extends AppCompatActivity {

    ImageView profileImg;
    TextView username = null;
    TextView dob = null;
    TextView phoneNum = null;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrationcont);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        profileImg = findViewById(R.id.profileImgSettings);
        username = findViewById(R.id.usernameTxt);
        dob = findViewById(R.id.dobTxt);
        phoneNum = findViewById(R.id.phoneNumTxt);
    }

    public void EditImage(View view) {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 2 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImg.setImageURI(imageUri);
        }
    }

    public void Save(View view) {
        if(imageUri != null && !username.getText().toString().equals("") && !dob.getText().toString().equals("") && !phoneNum.getText().toString().equals("")) {
            uploadToFirebase(imageUri, username, dob, phoneNum);
        } else {
            Toast.makeText(this, "Please Enter Your information", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadToFirebase(Uri imageUri, TextView username, TextView dob, TextView phoneNum) {
        StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            User user = new User(uri.toString(), username.getText().toString(), dob.getText().toString(), phoneNum.getText().toString());
            databaseReference.child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).setValue(user);

            Toast.makeText(RegistrationContActivity.this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();

            //Uploaded then moving to next screen
            startActivity(new Intent(RegistrationContActivity.this, NotesActivity.class));
            finish();
        })).addOnFailureListener(e -> Toast.makeText(RegistrationContActivity.this, "Uploading Failed!", Toast.LENGTH_SHORT).show());
    }

    private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }
}