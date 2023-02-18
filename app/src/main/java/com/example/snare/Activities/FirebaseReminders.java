package com.example.snare.Activities;

import androidx.annotation.NonNull;

import com.example.snare.Entities.Reminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebaseReminders {

    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private String userID ;

    public FirebaseReminders() {
        mDatabase = FirebaseDatabase.getInstance().getReference("reminders");
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
    }

    public void save(Reminder reminder) {
        mDatabase.child(userID).child(reminder.getIdFirebase()).setValue(reminder);
    }

    public void update(Reminder reminder) {
        mDatabase.child(userID).child(reminder.getIdFirebase()).setValue(reminder);
    }

    public void delete(Reminder reminder) {
        mDatabase.child(userID).child(reminder.getIdFirebase()).removeValue();
    }

    public void getAllReminders(RemindersCallback callback) {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("reminders").child(userID);

        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Reminder> reminders = new ArrayList<>();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Reminder reminder = noteSnapshot.getValue(Reminder.class);
                    reminders.add(reminder);
                }

                callback.onRemindersRetrieved(reminders);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onRemindersRetrieveError(databaseError.getMessage());
            }
        });
    }

    public interface RemindersCallback {
        void onRemindersRetrieved(List<Reminder> reminders);
        void onRemindersRetrieveError(String error);
    }

}