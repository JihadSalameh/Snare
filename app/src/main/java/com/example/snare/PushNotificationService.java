package com.example.snare;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import com.example.snare.Entities.Notifications;
import com.example.snare.dao.NotificationsDao;
import com.example.snare.dao.NotificationsDataBase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
public class PushNotificationService extends FirebaseMessagingService {

    private NotificationsDao notificationsDao;
    /////////////////
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference ref;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        String title = Objects.requireNonNull(message.getNotification()).getTitle();
        String text = message.getNotification().getBody();
        String CHANNEL_ID = "Notification";
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Notification",
                NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                        .setContentText(text)
                                .setSmallIcon(R.drawable.ic_baseline_people_24)
                                        .setAutoCancel(true);
        NotificationManagerCompat.from(this).notify(1, notification.build());

        ///////////////////
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference().child("Notifications").child(user.getUid());

        notificationsDao = NotificationsDataBase.getDatabase(this).notificationsDao();

        Notifications notifications = new Notifications();
        notifications.setTitle(title);
        notifications.setMessage(text);
        InsertNotification(notifications);

        super.onMessageReceived(message);
    }

    private void InsertNotification(Notifications notifications) {
        ////////////////////////////
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", notifications.getTitle());
        hashMap.put("message", notifications.getMessage());
        ref.child(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm:ss a",
                Locale.getDefault()).format(new Date())).updateChildren(hashMap);

        new InsertAsyncTask(notificationsDao).execute(notifications);
    }

    private static class InsertAsyncTask extends AsyncTask<Notifications, Void, Void> {

        private final NotificationsDao notificationsDao;

        public InsertAsyncTask(NotificationsDao notificationsDao1) {
            this.notificationsDao = notificationsDao1;
        }

        @Override
        protected Void doInBackground(Notifications... notifications) {
            notificationsDao.Insert(notifications[0]);
            return null;
        }
    }
}
