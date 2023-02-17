package com.example.snare.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snare.Entities.PinnedLocations;

import java.util.List;

@Dao
public interface PinnedLocationsDao {

    @Insert
    void Insert(PinnedLocations pinnedLocations);

    @Query("SELECT * FROM pinnedLocations")
    List<PinnedLocations> GetAllPinnedLocations();

}
