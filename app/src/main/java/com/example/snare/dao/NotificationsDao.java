package com.example.snare.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.snare.Entities.Notifications;

import java.util.List;

@Dao
public interface NotificationsDao {

    @Insert
    void Insert(Notifications notifications);

    @Query("SELECT * FROM notifications")
    List<Notifications> GetAllNotifications();

    @Query("DELETE FROM notifications")
    void DeleteAllNotifications();

}
