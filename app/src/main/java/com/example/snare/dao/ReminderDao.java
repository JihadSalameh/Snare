package com.example.snare.dao;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snare.Entities.Reminder;

import java.util.List;

@Dao
public interface ReminderDao {
    @Query("Select * FROM reminders ORDER BY count DESC")
    List<Reminder> getAllReminders();

    @Insert(onConflict = REPLACE)
    void insertReminder(Reminder reminder);

    @Delete
    void deleteReminder(Reminder reminder);
}
