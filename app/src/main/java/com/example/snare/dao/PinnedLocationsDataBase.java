package com.example.snare.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snare.Entities.PinnedLocations;

@Database(entities = PinnedLocations.class ,version = 6, exportSchema = false)
public abstract class PinnedLocationsDataBase extends RoomDatabase {

    private static PinnedLocationsDataBase pinnedLocationsDataBase;

    public static synchronized PinnedLocationsDataBase getDatabase(Context context) {
        if(pinnedLocationsDataBase == null) {
            pinnedLocationsDataBase = Room.databaseBuilder(
                    context,
                    PinnedLocationsDataBase.class,
                    "pinnedLocations_db"
            ).allowMainThreadQueries().build();
        }

        return pinnedLocationsDataBase;
    }

    public abstract PinnedLocationsDao pinnedLocationsDao();

}
