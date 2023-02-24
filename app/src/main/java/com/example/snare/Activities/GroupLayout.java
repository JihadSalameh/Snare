package com.example.snare.Activities;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import com.example.snare.Entities.WrappingFriends;
import com.example.snare.R;
import com.example.snare.adapters.GroupAdapter;
import com.example.snare.listeners.GroupListeners;

import java.util.ArrayList;
import java.util.List;

public class GroupLayout extends Dialog{

    private List<WrappingFriends> friends;
    private GroupAdapter groupAdapter;
    public static String partner ;

    public GroupLayout(@NonNull Context context) {
        super(context);
        setContentView(R.layout.activity_group_layout);
    }

    public void setDialog(GroupListeners groupListeners){
        RecyclerView groupRecycleView = findViewById(R.id.groupRecycleView);
        groupRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        friends = new ArrayList<>();
        getFriends();
        groupAdapter = new GroupAdapter(friends,groupListeners);
        groupRecycleView.setAdapter(groupAdapter);
    }

    private void getFriends() {
        GroupFireBase groupFireBase = new GroupFireBase();
        groupFireBase.getAllFriends(new GroupFireBase.GroupCallback() {
            @SuppressLint("NotifyDataSetChanged")
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