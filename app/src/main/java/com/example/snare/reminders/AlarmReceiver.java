package com.example.snare.reminders;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.example.snare.Activities.ReminderActivity;
import com.example.snare.R;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract the reminder data from the intent's extras
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");

        Intent openIntent = new Intent(context, ReminderActivity.class);
       // openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, openIntent, PendingIntent.FLAG_IMMUTABLE);
        // Create a notification using the NotificationCompat class
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "channel_id")
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        // Show the notification using the NotificationManager class
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(3, builder.build());
    }

}