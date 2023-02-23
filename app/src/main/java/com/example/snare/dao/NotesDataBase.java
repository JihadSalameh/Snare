package com.example.snare.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snare.Entities.Note;

@Database(entities = Note.class ,version = 4,exportSchema = false)
public abstract class NotesDataBase extends RoomDatabase{

    private static NotesDataBase notesDataBase;

    public static synchronized NotesDataBase getDatabase(Context context){

        if(notesDataBase == null){
            notesDataBase = Room.databaseBuilder(
                    context,
                    NotesDataBase.class,
                    "notes_db"
                ).allowMainThreadQueries().build();
        }

        return notesDataBase;
    }

    public abstract NoteDao noteDao();

}
