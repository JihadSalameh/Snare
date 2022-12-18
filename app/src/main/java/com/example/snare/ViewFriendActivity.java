package com.example.snare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ViewFriendActivity extends AppCompatActivity {

    DatabaseReference ref, requestRef, friendRef;
    FirebaseAuth auth;
    FirebaseUser user;

    String profileImageUrl, name;

    ImageView profileImg;
    TextView username;
    Button perform, decline;

    String userId;
    String currentState = "nothing_happened";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_friend);

        userId = getIntent().getStringExtra("userKey");

        ref = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        requestRef = FirebaseDatabase.getInstance().getReference().child("Requests");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        profileImg = findViewById(R.id.profileImage);
        username = findViewById(R.id.userName);
        perform = findViewById(R.id.performBtn);
        decline = findViewById(R.id.declineBtn);

        LoadUserData();
        CheckUserExistance(userId);
    }

    private void CheckUserExistance(String userId) {
        friendRef.child(user.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    currentState = "friend";
                    perform.setVisibility(View.GONE);
                    decline.setText("UNFRIEND");
                    decline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        friendRef.child(userId).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    currentState = "friend";
                    perform.setVisibility(View.GONE);
                    decline.setText("UNFRIEND");
                    decline.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestRef.child(user.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if(snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "I_sent_pending";
                        perform.setText("Cancel Friend Request");
                        decline.setVisibility(View.GONE);
                    }

                    if(snapshot.child("status").getValue().toString().equals("request_declined")) {
                        currentState = "I_sent_declined";
                        perform.setText("Cancel Friend Request");
                        decline.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        requestRef.child(userId).child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    if(snapshot.child("status").getValue().toString().equals("pending")) {
                        currentState = "he_sent_pending";
                        perform.setText("Accept");
                        decline.setText("Decline");
                        decline.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(currentState.equals("nothing_happened")) {
            currentState = "nothing_happened";
            perform.setText("SEND REQUEST");
            decline.setVisibility(View.GONE);
        }
    }

    private void LoadUserData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    profileImageUrl = snapshot.child("profilePic").getValue().toString();
                    name = snapshot.child("name").getValue().toString();

                    Picasso.get().load(profileImageUrl).into(profileImg);
                    username.setText(name);
                } else {
                    Toast.makeText(ViewFriendActivity.this, "Data Not Found!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, ""+error.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SendRequest(View view) {
        performAction(userId);
    }

    private void performAction(String userId) {
        if(currentState.equals("nothing_happened")) {
            HashMap hashMap = new HashMap();
            hashMap.put("status", "pending");
            requestRef.child(user.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friend Request Sent!", Toast.LENGTH_SHORT).show();
                        decline.setVisibility(View.GONE);
                        currentState = "I_sent_pending";
                        perform.setText("Cancel Friend Request");
                    } else {
                        Toast.makeText(ViewFriendActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if(currentState.equals("I_sent_pending") || currentState.equals("request_declined")) {
            requestRef.child(user.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        Toast.makeText(ViewFriendActivity.this, "Friend Request Cancelled!", Toast.LENGTH_SHORT).show();
                        currentState = "nothing_happened";
                        perform.setText("SEND REQUEST");
                        decline.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(ViewFriendActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if(currentState.equals("he_sent_pending")) {
            requestRef.child(user.getUid()).child(userId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        HashMap hashMap = new HashMap();
                        hashMap.put("status", "friend");
                        hashMap.put("name", username);
                        hashMap.put("profilePic", profileImageUrl);
                        friendRef.child(user.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if(task.isSuccessful()) {
                                    friendRef.child(userId).child(user.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            Toast.makeText(ViewFriendActivity.this, "Friend Added!", Toast.LENGTH_SHORT).show();
                                            currentState = "friend";
                                            perform.setVisibility(View.GONE);
                                            decline.setText("UNFRIEND");
                                            decline.setVisibility(View.VISIBLE);
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            });
        }

        if(currentState.equals("friend")) {
            //DO LATER
        }
    }

    public void DeclineRequest(View view) {
    }
}