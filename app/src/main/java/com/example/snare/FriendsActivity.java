package com.example.snare;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snare.Utills.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

public class FriendsActivity extends AppCompatActivity {

    FirebaseRecyclerOptions<Friends> options;
    FirebaseRecyclerAdapter<Friends, FriendMyViewHolder> adapter;

    RecyclerView recyclerView;
    FloatingActionButton addFriends;

    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        addFriends = findViewById(R.id.addFriends);
        recyclerView = findViewById(R.id.friendsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("Friends");

        LoadFriends("");
    }

    private void LoadFriends(String s) {
        /*Friends friends = new Friends("https://firebasestorage.googleapis.com/v0/b/snare-e1afc.appspot.com/o/1670603990682.jpg?alt=media&token=5bb0e686-3249-4705-9585-8c729c8ed479", "khalil");
        ref.child(user.getUid()).child("JQguniqmuiZqJFG9ZsotHG8aJlK2").setValue(friends);*/

        Query query = ref.child(user.getUid()).orderByChild("name");
        options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(query, Friends.class).build();
        adapter = new FirebaseRecyclerAdapter<Friends, FriendMyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendMyViewHolder holder, int position, @NonNull Friends model) {
                holder.profile.setBackground(null);
                Picasso.get().load(model.getProfilePic()).into(holder.profile);
                holder.name.setText(model.getName());
            }

            @NonNull
            @Override
            public FriendMyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);

                return new FriendMyViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    public void LoadAddFriend(View view) {
        startActivity(new Intent(FriendsActivity.this, FindFriendActivity.class));
    }
}