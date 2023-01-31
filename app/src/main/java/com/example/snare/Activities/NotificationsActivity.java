package com.example.snare.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;

import com.example.snare.Entities.Notifications;
import com.example.snare.R;
import com.example.snare.adapters.NotificationsAdapter;
import com.example.snare.dao.NotificationsDao;
import com.example.snare.dao.NotificationsDataBase;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotificationsAdapter adapter;
    private NotificationsDao notificationsDao;
    private List<Notifications> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        notificationsDao = NotificationsDataBase.getDatabase(getApplicationContext()).notificationsDao();
        list = notificationsDao.GetAllNotifications();

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationsAdapter(list);
        recyclerView.setAdapter(adapter);
    }

}
