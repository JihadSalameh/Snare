package com.example.snare.firebase;

import androidx.annotation.NonNull;

import com.example.snare.Entities.Friends;
import com.example.snare.Entities.WrappingFriends;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsFireBase {

    private final DatabaseReference mDatabase;
    private final String userID ;

    public FriendsFireBase() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("Friends").child(userID);
    }

    public void getAllFriends(final GroupCallback callback) {
        mDatabase.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<WrappingFriends> friends = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // Reverse the order of the child nodes
                ArrayList<DataSnapshot> reversedChildren = new ArrayList<>();
                for (DataSnapshot child : children) {
                    reversedChildren.add(0, child);
                }

                // Loop through each note in the reversed list of child nodes
                for (DataSnapshot friendSnapshot : reversedChildren) {
                    Friends friend = friendSnapshot.getValue(Friends.class);
                    friends.add(new WrappingFriends(friend,friendSnapshot.getKey()));
                }

                callback.onGroupRetrieved(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onGroupRetrieveError(databaseError.getMessage());
            }
        });
    }

    public interface GroupCallback {
        void onGroupRetrieved(List<WrappingFriends> friends);
        void onGroupRetrieveError(String error);
    }

}
