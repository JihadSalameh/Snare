package com.example.snare.firebase;

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

    private final DatabaseReference mDatabase;
    private final String userID ;

    public FirebaseReminders() {
        mDatabase = FirebaseDatabase.getInstance().getReference("reminders");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    public void save(Reminder reminder) {
        List<String> ids = reminder.getGroup();

        if(ids != null && ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< reminder.getGroup().size() ; i++){
                mDatabase.child(reminder.getGroup().get(i)).child(reminder.getIdFirebase()).setValue(reminder);
            }
            return;
        }

        mDatabase.child(userID).child(reminder.getIdFirebase()).setValue(reminder);
    }

    public void update(Reminder reminder) {
        List<String> ids = reminder.getGroup();

        if(ids != null && ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< reminder.getGroup().size() ; i++){
                mDatabase.child(reminder.getGroup().get(i)).child(reminder.getIdFirebase()).setValue(reminder);
            }
            return;
        }

        mDatabase.child(userID).child(reminder.getIdFirebase()).setValue(reminder);
    }

    public void delete(Reminder reminder) {
        List<String> ids = reminder.getGroup();

        if(ids != null && ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< reminder.getGroup().size() ; i++){
                mDatabase.child(reminder.getGroup().get(i)).child(reminder.getIdFirebase()).removeValue();
            }
            return;
        }

        mDatabase.child(userID).child(reminder.getIdFirebase()).removeValue();
    }

    public void getAllReminders(RemindersCallback callback) {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("reminders").child(userID);

        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!(dataSnapshot == null)) {
                    ArrayList<Reminder> reminders = new ArrayList<>();
                    // Get a list of the child nodes
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                    // Reverse the order of the child nodes
                    ArrayList<DataSnapshot> reversedChildren = new ArrayList<>();
                    for (DataSnapshot child : children) {
                        reversedChildren.add(0, child);
                    }

                    // Loop through each note in the reversed list of child nodes
                    for (DataSnapshot noteSnapshot : reversedChildren) {
                        Reminder reminder = noteSnapshot.getValue(Reminder.class);
                        reminders.add(reminder);
                    }

                    callback.onRemindersRetrieved(reminders);
                }
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