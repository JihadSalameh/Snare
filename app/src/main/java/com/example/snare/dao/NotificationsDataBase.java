package com.example.snare.dao;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.snare.Entities.Notifications;

@Database(entities = Notifications.class ,version = 1, exportSchema = false)
public abstract class NotificationsDataBase extends RoomDatabase {

    private static NotificationsDataBase notificationsDataBase;

    public static synchronized NotificationsDataBase getDatabase(Context context) {
        if(notificationsDataBase == null) {
            notificationsDataBase = Room.databaseBuilder(
                    context,
                    NotificationsDataBase.class,
                    "notifications_db"
            ).build();
        }

        return notificationsDataBase;
    }

    public abstract NotificationsDao notificationsDao();

}
