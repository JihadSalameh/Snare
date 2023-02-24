package com.example.snare.Activities;

import androidx.annotation.NonNull;

import com.example.snare.Entities.Friends;
import com.example.snare.Entities.PinnedLocations;
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

public class PinnedLocationFirebase {
    private final DatabaseReference mDatabase;
    private final String userID ;

    public PinnedLocationFirebase() {
        mDatabase = FirebaseDatabase.getInstance().getReference("Friends");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    public void getAllLocations(final PinnedCallback callback) {
        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference("PinnedLocations").child(userID);

        friendsRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<PinnedLocations> pinnedLocations = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // Reverse the order of the child nodes
                ArrayList<DataSnapshot> reversedChildren = new ArrayList<>();
                for (DataSnapshot child : children) {
                    reversedChildren.add(0, child);
                }

                // Loop through each note in the reversed list of child nodes
                for (DataSnapshot snapshot : reversedChildren) {
                    PinnedLocations pinnedLocation = snapshot.getValue(PinnedLocations.class);
                    pinnedLocations.add(pinnedLocation);
                }

                callback.onPinnedRetrieved(pinnedLocations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onPinnedRetrieveError(databaseError.getMessage());
            }
        });
    }

    public interface PinnedCallback {
        void onPinnedRetrieved(List<PinnedLocations> pinnedLocations);
        void onPinnedRetrieveError(String error);
    }
}
