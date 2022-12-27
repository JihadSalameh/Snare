package com.example.snare.dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snare.Entities.Note;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("Select * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = REPLACE)
    void insertNote(Note note);

    @Delete
    void deleteNote(Note note);
}
