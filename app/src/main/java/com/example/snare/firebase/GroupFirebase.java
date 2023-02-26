package com.example.snare.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.snare.Entities.Group;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupFirebase {

    private final DatabaseReference mDatabase;
    private final String userID ;

    public GroupFirebase() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("groups");
    }

    public void save(Group group) {
        mDatabase.child(userID).child(group.getName()).setValue(group);
    }

    public void update(Group group) {
        mDatabase.child(userID).child(group.getName()).setValue(group);
    }

    public void delete(Group group) {

        mDatabase.child(userID).child(group.getName()).removeValue();

    }

    public void getAllGroups(final GroupCallback callback) {
        mDatabase.child(userID).orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Group> groups = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // Reverse the order of the child nodes
                ArrayList<DataSnapshot> reversedChildren = new ArrayList<>();
                for (DataSnapshot child : children) {
                    reversedChildren.add(0, child);
                }

                // Loop through each note in the reversed list of child nodes
                for (DataSnapshot groupSnapshot : reversedChildren) {
                    Group group = groupSnapshot.getValue(Group.class);
                    groups.add(group);
                }
                callback.onGroupRetrieved(groups);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onGroupRetrieveError(databaseError.getMessage());
            }
        });
    }

    public interface GroupCallback {
        void onGroupRetrieved(List<Group> groups);
        void onGroupRetrieveError(String error);
    }
}
