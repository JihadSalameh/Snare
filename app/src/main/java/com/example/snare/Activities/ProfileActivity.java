package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snare.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImg;
    private EditText username = null;
    private EditText dob = null;
    private EditText phoneNum = null;
    private TextView blocked_users;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReference1;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileImg = findViewById(R.id.profileImgSettings);
        username = findViewById(R.id.usernameTxt);
        dob = findViewById(R.id.dobTxt);
        phoneNum = findViewById(R.id.phoneNumTxt);
        blocked_users = findViewById(R.id.blockedUsers);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference().child("Users");
        databaseReference1 = firebaseDatabase.getReference("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        storageReference = FirebaseStorage.getInstance().getReference();

        databaseReference1.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {
                Picasso.get().load(snapshot.child("profilePic").getValue(String.class)).into(profileImg);
                username.setText(snapshot.child("name").getValue(String.class));
                dob.setText(snapshot.child("dob").getValue(String.class));
                phoneNum.setText(snapshot.child("phoneNumber").getValue(String.class));
            }
        });
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
        if(/*profileImg == null || */username.getText().toString().equals("") || dob.getText().toString().equals("") || phoneNum.getText().toString().equals("")) {
            Toast.makeText(this, "Cant Remove Information", Toast.LENGTH_SHORT).show();
        } else {
            uploadToFirebase(/*imageUri, */username, dob, phoneNum);
        }
    }

    private void uploadToFirebase(/*Uri imageUri, */TextView username, TextView dob, TextView phoneNum) {

        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("dob").setValue(dob.getText().toString());
        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("name").setValue(username.getText().toString());
        databaseReference.child(FirebaseAuth.getInstance().getUid()).child("phoneNumber").setValue(phoneNum.getText().toString());
        Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();

        /*StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        User user = new User(FirebaseAuth.getInstance().getUid(), uri.toString(), username.getText().toString(), dob.getText().toString(), phoneNum.getText().toString());
                        databaseReference.child(user.getId()).setValue(user);

                        Toast.makeText(ProfileActivity.this, "Uploaded Successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, "Uploading Failed!", Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    public void ViewBlockedUsers(View view) {
        startActivity(new Intent(ProfileActivity.this, BlockedUsersActivity.class));
    }

    /*private String getFileExtension(Uri imageUri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }*/
}