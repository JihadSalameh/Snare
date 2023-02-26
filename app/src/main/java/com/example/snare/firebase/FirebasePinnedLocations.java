package com.example.snare.firebase;

import androidx.annotation.NonNull;

import com.example.snare.Entities.PinnedLocations;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebasePinnedLocations {

    private final DatabaseReference mDatabase;
    private final String userID;

    public FirebasePinnedLocations() {
        mDatabase = FirebaseDatabase.getInstance().getReference("PinnedLocations");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    public void save(PinnedLocations location) {
        mDatabase.child(userID).child(location.getName()).setValue(location);
    }

    public void update(PinnedLocations location) {
        mDatabase.child(userID).child(location.getName()).setValue(location);
    }

    public void delete(PinnedLocations location) {
        mDatabase.child(userID).child(location.getName()).removeValue();
    }

    public void getAllPinnedLocations(final PinnedLocationsCallback callback) {
        DatabaseReference pinnedLocationsRef = FirebaseDatabase.getInstance().getReference("PinnedLocations").child(userID);

        pinnedLocationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PinnedLocations> pinnedLocations = new ArrayList<>();
                for (DataSnapshot pinnedLocationSnapshot : snapshot.getChildren()) {
                    PinnedLocations location = pinnedLocationSnapshot.getValue(PinnedLocations.class);
                    pinnedLocations.add(location);
                }

                callback.onPinnedLocationsRetrieved(pinnedLocations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onPinnedLocationsRetrieveError(error.getMessage());
            }
        });
    }

    public interface PinnedLocationsCallback {
        void onPinnedLocationsRetrieved(List<PinnedLocations> pinnedLocations);
        void onPinnedLocationsRetrieveError(String error);
    }

}
