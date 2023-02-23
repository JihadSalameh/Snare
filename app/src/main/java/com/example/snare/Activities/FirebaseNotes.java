package com.example.snare.Activities;

import androidx.annotation.NonNull;

import com.example.snare.Entities.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FirebaseNotes {

    private final DatabaseReference mDatabase;
    private final String userID ;

    public FirebaseNotes() {
        mDatabase = FirebaseDatabase.getInstance().getReference("notes");
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        userID = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
    }

    public void save(Note note) {

        List<String> ids = note.getGroup();

        if(ids != null && ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< note.getGroup().size() ; i++){
                mDatabase.child(note.getGroup().get(i)).child(note.getIdFirebase()).setValue(note);
            }
            return;
        }

        mDatabase.child(userID).child(note.getIdFirebase()).setValue(note);
    }

    public void update(Note note) {
        List<String> ids = note.getGroup();

        if(ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< note.getGroup().size() ; i++){
                mDatabase.child(note.getGroup().get(i)).child(note.getIdFirebase()).setValue(note);
            }
            return;
        }

        mDatabase.child(userID).child(note.getIdFirebase()).setValue(note);
    }

    public void delete(Note note) {

        List<String> ids = note.getGroup();

        if(ids != null && ids.size() !=0){
            if(!ids.contains(userID)){
                ids.add(userID);
            }
            for(int i=0 ; i< note.getGroup().size() ; i++){
                mDatabase.child(note.getGroup().get(i)).child(note.getIdFirebase()).removeValue();
            }
            return;
        }

        mDatabase.child(userID).child(note.getIdFirebase()).removeValue();

    }

    public void getAllNotes(final NotesCallback callback) {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(userID);

        notesRef.orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Note> notes = new ArrayList<>();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // Reverse the order of the child nodes
                ArrayList<DataSnapshot> reversedChildren = new ArrayList<>();
                for (DataSnapshot child : children) {
                    reversedChildren.add(0, child);
                }

                // Loop through each note in the reversed list of child nodes
                for (DataSnapshot noteSnapshot : reversedChildren) {
                    Note note = noteSnapshot.getValue(Note.class);
                    notes.add(note);
                }

                callback.onNotesRetrieved(notes);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onNotesRetrieveError(databaseError.getMessage());
            }
        });
    }

    public interface NotesCallback {
        void onNotesRetrieved(List<Note> notes);
        void onNotesRetrieveError(String error);
    }

}