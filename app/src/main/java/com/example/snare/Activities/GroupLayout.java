package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.example.snare.Entities.Friends;
import com.example.snare.Entities.Note;
import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.adapters.GroupAdapter;
import com.example.snare.listeners.GroupListeners;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import kotlin.jvm.internal.PropertyReference0Impl;

public class GroupLayout extends Dialog{

    private RecyclerView groupRecycleView;
    private List<WrappingFriends> friends;
    private GroupAdapter groupAdapter;
    public static String partner ;

    public GroupLayout(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_group_layout);


    }

    public void setDialog(GroupListeners groupListeners){
        groupRecycleView = findViewById(R.id.groupRecycleView);
        groupRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        friends = new ArrayList<>();
        getFriends();
        groupAdapter = new GroupAdapter(friends,groupListeners);
        groupRecycleView.setAdapter(groupAdapter);
    }

    private void getFriends() {
        GroupFireBase groupFireBase = new GroupFireBase();
        groupFireBase.getAllFriends(new GroupFireBase.GroupCallback() {
            @Override
            public void onGroupRetrieved(List<WrappingFriends> friends) {
                GroupLayout.this.friends.addAll(friends);
                groupAdapter.notifyDataSetChanged(); // notify the adapter that the data has changed
            }

            @Override
            public void onGroupRetrieveError(String error) {

            }
        });
    }

}