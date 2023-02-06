package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.snare.FCMSend;
import com.example.snare.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Objects;

public class ViewFriendActivity extends AppCompatActivity {

    DatabaseReference ref, requestRef, friendRef;
    DatabaseReference userRef;
    FirebaseAuth auth;
    FirebaseUser user;

    String profileImageUrl, name;

    ImageView profileImg;
    TextView username;
    Button perform, decline, block;

    String userId;
    String currentState = "nothing_happened";

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

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
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        profileImg = findViewById(R.id.profileImage);
        username = findViewById(R.id.userName);
        perform = findViewById(R.id.performBtn);
        decline = findViewById(R.id.declineBtn);
        block = findViewById(R.id.blockBtn);

        LoadUserData();
        CheckUserExistence(userId);
    }

    @SuppressLint("SetTextI18n")
    private void CheckUserExistence(String userId) {
        friendRef.child(user.getUid()).child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists() && Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("friend")) {
                    currentState = "friend";
                    perform.setVisibility(View.GONE);
                    decline.setText("UNFRIEND");
                    decline.setVisibility(View.VISIBLE);
                    block.setVisibility(View.VISIBLE);
                } else if(snapshot.exists() && Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("blocked")) {
                    currentState = "blocked";
                    perform.setVisibility(View.GONE);
                    decline.setVisibility(View.GONE);
                    block.setVisibility(View.VISIBLE);
                    block.setText("UNBLOCK");
                } else if(snapshot.exists() && Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("blocked_by")) {
                    currentState = "blocked";
                    perform.setVisibility(View.GONE);
                    decline.setVisibility(View.GONE);
                    block.setVisibility(View.GONE);
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
                    if(Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("pending")) {
                        currentState = "I_sent_pending";
                        perform.setText("Cancel Friend Request");
                        decline.setVisibility(View.GONE);
                        block.setVisibility(View.GONE);
                    }

                    ////DON'T THINK IT'S NEEDED CHECK LATER///////////////////////////////////////////////////
                    if(Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("request_declined")) {
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
                    if(Objects.requireNonNull(snapshot.child("status").getValue()).toString().equals("pending")) {
                        currentState = "he_sent_pending";
                        perform.setText("Accept");
                        decline.setText("Decline");
                        decline.setVisibility(View.VISIBLE);
                        block.setVisibility(View.GONE);
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
            block.setVisibility(View.GONE);
        }
    }

    private void LoadUserData() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    profileImageUrl = Objects.requireNonNull(snapshot.child("profilePic").getValue()).toString();
                    name = Objects.requireNonNull(snapshot.child("name").getValue()).toString();

                    Picasso.get().load(profileImageUrl).into(profileImg);
                    username.setText(name);
                } else {
                    Toast.makeText(ViewFriendActivity.this, "Data Not Found!!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewFriendActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void SendRequest(View view) {
        performAction(userId);
    }

    @SuppressLint("SetTextI18n")
    private void performAction(String userId) {
        if(currentState.equals("nothing_happened")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "pending");

            ref.get().addOnSuccessListener(this::SendNotificationFriendRequest);

            requestRef.child(user.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(ViewFriendActivity.this, "Friend Request Sent!", Toast.LENGTH_SHORT).show();
                    decline.setVisibility(View.GONE);
                    block.setVisibility(View.GONE);
                    currentState = "I_sent_pending";
                    perform.setText("Cancel Friend Request");
                } else {
                    Toast.makeText(ViewFriendActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(currentState.equals("I_sent_pending") || currentState.equals("request_declined")) {
            requestRef.child(user.getUid()).child(userId).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(ViewFriendActivity.this, "Friend Request Cancelled!", Toast.LENGTH_SHORT).show();
                    currentState = "nothing_happened";
                    perform.setText("SEND REQUEST");
                    decline.setVisibility(View.GONE);
                    block.setVisibility(View.GONE);
                } else {
                    Toast.makeText(ViewFriendActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        if(currentState.equals("he_sent_pending")) {
            requestRef.child(userId).child(user.getUid()).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    AddFriend("adding");
                } else {
                    Toast.makeText(ViewFriendActivity.this, Objects.requireNonNull(task.getException()).toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void SendNotificationFriendRequest(DataSnapshot dataSnapshot) {
        String token = Objects.requireNonNull(dataSnapshot.child("token").getValue()).toString();

        userRef.get().addOnSuccessListener(dataSnapshot1 -> {
            String message = Objects.requireNonNull(dataSnapshot1.child("name").getValue()) + " sent you a friend request.";
            String title = "Friend Request";

            FCMSend.pushNotification(ViewFriendActivity.this, token, title, message);
        });
    }

    private void SendNotificationFriendRequestAccepted(DataSnapshot dataSnapshot) {
        String token = Objects.requireNonNull(dataSnapshot.child("token").getValue()).toString();

        userRef.get().addOnSuccessListener(dataSnapshot1 -> {
            String message = Objects.requireNonNull(dataSnapshot1.child("name").getValue()) + " accepted your friend request.";
            String title = "Friends";

            FCMSend.pushNotification(ViewFriendActivity.this, token, title, message);
        });
    }

    @SuppressLint("SetTextI18n")
    private void AddFriend(String status) {
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("status", "friend");
            hashMap.put("name", name);
            hashMap.put("profilePic", profileImageUrl);

            HashMap<String, Object> hashMap1 = new HashMap<>();
            hashMap1.put("status", "friend");
            hashMap1.put("name", dataSnapshot.child("name").getValue(String.class));
            hashMap1.put("profilePic", dataSnapshot.child("profilePic").getValue(String.class));

            friendRef.child(user.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    friendRef.child(userId).child(user.getUid()).updateChildren(hashMap1).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            Toast.makeText(ViewFriendActivity.this, "Friend Added!", Toast.LENGTH_SHORT).show();
                            currentState = "friend";
                            perform.setVisibility(View.GONE);
                            decline.setText("UNFRIEND");
                            decline.setVisibility(View.VISIBLE);
                            block.setText("BLOCK");
                            block.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        });

        if(status.equals("adding")) {
            ref.get().addOnSuccessListener(this::SendNotificationFriendRequestAccepted);
        }
    }

    @SuppressLint("SetTextI18n")
    public void DeclineRequest(View view) {
        if(currentState.equals("friend")) {
            friendRef.child(user.getUid()).child(userId).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    friendRef.child(userId).child(user.getUid()).removeValue().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            Toast.makeText(ViewFriendActivity.this, "You Are Unfriended!", Toast.LENGTH_SHORT).show();
                            currentState = "nothing_happened";
                            perform.setText("SEND REQUEST");
                            perform.setVisibility(View.VISIBLE);
                            decline.setVisibility(View.GONE);
                            block.setVisibility(View.GONE);
                        }
                    });
                }
            });
        }

        if(currentState.equals("he_sent_pending")) {
            requestRef.child(userId).child(user.getUid()).removeValue().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(ViewFriendActivity.this, "Friend Request Denied!", Toast.LENGTH_SHORT).show();
                    currentState = "nothing_happened";
                    perform.setText("SEND REQUEST");
                    perform.setVisibility(View.VISIBLE);
                    decline.setVisibility(View.GONE);
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    public void blockUser(View view) {
        if(block.getText().equals("BLOCK")) {
            currentState = "blocked";
            userRef.get().addOnSuccessListener(dataSnapshot -> {
                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("status", "blocked");
                hashMap.put("name", name);
                hashMap.put("profilePic", profileImageUrl);

                HashMap<String, Object> hashMap1 = new HashMap<>();
                hashMap1.put("status", "blocked_by");
                hashMap1.put("name", dataSnapshot.child("name").getValue(String.class));
                hashMap1.put("profilePic", dataSnapshot.child("profilePic").getValue(String.class));

                friendRef.child(user.getUid()).child(userId).updateChildren(hashMap).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        friendRef.child(userId).child(user.getUid()).updateChildren(hashMap1).addOnCompleteListener(task1 -> {
                            if(task1.isSuccessful()) {
                                Toast.makeText(ViewFriendActivity.this, "Friend Blocked!", Toast.LENGTH_SHORT).show();
                                currentState = "blocked";
                                perform.setVisibility(View.VISIBLE);
                                perform.setText("UNBLOCK");
                                block.setVisibility(View.GONE);
                                decline.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            });
        } else if(block.getText().equals("UNBLOCK")) {
            AddFriend("unblocking");
        }
    }
}