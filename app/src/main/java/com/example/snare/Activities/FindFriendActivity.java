package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.snare.ViewHolders.FriendViewHolder;
import com.example.snare.R;
import com.example.snare.Entities.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity {

    FirebaseRecyclerOptions<User> options;
    FirebaseRecyclerAdapter<User, FriendViewHolder> adapter;

    DatabaseReference ref, friendRef;
    FirebaseAuth auth;
    FirebaseUser user;
    RecyclerView recyclerView;

    ArrayList<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        ref = FirebaseDatabase.getInstance().getReference().child("Users");
        friendRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadUsers("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        LoadUsers("");
    }

    private void LoadUsers(String s) {
        friendRef.child(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                StoreFriends(dataSnapshot);
            }
        });
        Query query = ref.orderByChild("name").startAt(s).endAt(s+"\uf8ff");
        options = new FirebaseRecyclerOptions.Builder<User>().setQuery(query, User.class).build();
        adapter = new FirebaseRecyclerAdapter<User, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder holder, @SuppressLint("RecyclerView") int position, @NonNull User model) {
                if(!user.getUid().equals(getRef(position).getKey()) && !CheckIfFriend(getRef(position).getKey())) {
                    holder.profile.setBackground(null);
                    Picasso.get().load(model.getProfilePic()).into(holder.profile);
                    holder.name.setText(model.getName());
                } else {
                    holder.itemView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(FindFriendActivity.this, ViewFriendActivity.class);
                        intent.putExtra("userKey", getRef(position).getKey());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_friend, parent, false);
                return new FriendViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private boolean CheckIfFriend(String s) {
        for(int i = 0; i < list.size(); i++) {
            if(list.get(i).equals(s)) {
                return true;
            }
        }
        return false;
    }

    private void StoreFriends(DataSnapshot dataSnapshot) {
        for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
            list.add(snapshot.getKey());
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                LoadUsers(newText);
                return false;
            }
        });

        return true;
    }
}