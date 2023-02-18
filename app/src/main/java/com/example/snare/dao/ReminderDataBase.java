package com.example.snare.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snare.Entities.Reminder;

@Database(entities = Reminder.class ,version = 2 ,exportSchema = false)
public abstract class ReminderDataBase extends RoomDatabase {

    private static ReminderDataBase reminderDataBase;

    public static synchronized ReminderDataBase getDatabase(Context context){

        if(reminderDataBase == null){
            reminderDataBase = Room.databaseBuilder(
                    context,
                    ReminderDataBase.class,
                    "reminders_db"
            ).build();
        }

        return reminderDataBase;
    }

    public abstract ReminderDao reminderDao();

}
