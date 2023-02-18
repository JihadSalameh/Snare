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

    private DatabaseReference mDatabase;
    private FirebaseAuth firebaseAuth;
    private String userID ;

    public FirebaseNotes() {
        mDatabase = FirebaseDatabase.getInstance().getReference("notes");
        firebaseAuth = FirebaseAuth.getInstance();
        userID = firebaseAuth.getCurrentUser().getUid();
    }

    public void save(Note note) {
        mDatabase.child(userID).child(note.getIdFirebase()).setValue(note);
    }

    public void update(Note note) {
        mDatabase.child(userID).child(note.getIdFirebase()).setValue(note);
    }

    public void delete(Note note) {
        mDatabase.child(userID).child(note.getIdFirebase()).removeValue();
    }

    public void getAllNotes(final NotesCallback callback) {
        DatabaseReference notesRef = FirebaseDatabase.getInstance().getReference("notes").child(userID);

        notesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Note> notes = new ArrayList<>();
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
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