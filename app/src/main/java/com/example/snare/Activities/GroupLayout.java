package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import com.example.snare.Entities.Group;
import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.adapters.FriendsAdapter;
import com.example.snare.adapters.GroupAdapter;
import com.example.snare.firebaseRef.FriendsFireBase;
import com.example.snare.firebaseRef.GroupFirebase;
import com.example.snare.listeners.FriendListeners;
import com.example.snare.listeners.GroupListener;

import java.util.ArrayList;
import java.util.List;

public class GroupLayout extends Dialog{

    private List<WrappingFriends> friends;
    private FriendsAdapter friendsAdapter;
    private List<Group> groups;
    private GroupAdapter groupAdapter;

    public GroupLayout(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_group_layout);
    }

    public void setDialog(GroupListener groupListener){
        RecyclerView groupRecycleView = findViewById(R.id.groupRecycleView);
        groupRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        groups = new ArrayList<>();
        getGroups();
        groupAdapter = new GroupAdapter(groups,groupListener);
        groupRecycleView.setAdapter(groupAdapter);
    }

    private void getGroups() {
        GroupFirebase groupFirebase = new GroupFirebase();
        groupFirebase.getAllGroups(new GroupFirebase.GroupCallback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onGroupRetrieved(List<Group> groups) {
                GroupLayout.this.groups.addAll(groups);
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onGroupRetrieveError(String error) {

            }
        });
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