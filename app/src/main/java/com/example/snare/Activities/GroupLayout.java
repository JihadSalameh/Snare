package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.adapters.FriendsAdapter;
import com.example.snare.firebase.FriendsFireBase;
import com.example.snare.listeners.FriendListeners;

import java.util.ArrayList;
import java.util.List;

public class GroupLayout extends Dialog{

    private List<WrappingFriends> friends;
    private FriendsAdapter friendsAdapter;
    public static String partner ;

    public GroupLayout(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_group_layout);
    }

    public void setDialog(FriendListeners friendListeners){
        RecyclerView groupRecycleView = findViewById(R.id.groupRecycleView);
        groupRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        friends = new ArrayList<>();
        getFriends();
        friendsAdapter = new FriendsAdapter(friends, friendListeners);
        groupRecycleView.setAdapter(friendsAdapter);
    }

    private void getFriends() {
        FriendsFireBase friendsFireBase = new FriendsFireBase();
        friendsFireBase.getAllFriends(new FriendsFireBase.GroupCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onGroupRetrieved(List<WrappingFriends> friends) {
                GroupLayout.this.friends.addAll(friends);
                friendsAdapter.notifyDataSetChanged(); // notify the adapter that the data has changed
            }

            @Override
            public void onGroupRetrieveError(String error) {

            }
        });
    }

}