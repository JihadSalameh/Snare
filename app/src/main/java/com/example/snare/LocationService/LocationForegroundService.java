package com.example.snare.LocationService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.Reminder;
import com.example.snare.R;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.example.snare.dao.ReminderDataBase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationForegroundService extends Service {

    private final IBinder iBinder = new MyBinder();
    private static final String CHANNEL_ID = "2";

    private List<PinnedLocations> list2 = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        filter();

        buildNotification();

        requestLocationUpdates();
    }

    private void filter() {
        list2.clear();

        //experimentation
        List<PinnedLocations> list = PinnedLocationsDataBase.getDatabase(this).pinnedLocationsDao().GetAllPinnedLocations();
        System.out.println(list);

        List<Reminder> reminders = ReminderDataBase.getDatabase(this).reminderDao().getAllReminders();
        System.out.println(reminders);

        for (Reminder reminder : reminders) {
            for (PinnedLocations pinnedLocations : list) {
                if (!reminder.getLocation().equals("") && reminder.getLocation().equals(pinnedLocations.getName())) {
                    list2.add(pinnedLocations);
                }
            }
        }
        System.out.println(list2);

        list.clear();
        reminders.clear();
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(15000)
                .setMaxUpdateDelayMillis(15000)
                .setWaitForAccurateLocation(true)
                .build();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getBaseContext());
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    //filtering list2
                    filter();

                    //if any of the pinned locations on the device is closer than 1Km it will
                    //do what's inside the if statement
                    for(PinnedLocations pinnedLocations: list2) {
                        Location pinned = new Location("");
                        pinned.setLatitude(Double.parseDouble(pinnedLocations.getLat()));
                        pinned.setLongitude(Double.parseDouble(pinnedLocations.getLng()));
                        float v = Objects.requireNonNull(locationResult.getLastLocation()).distanceTo(pinned);
                        if(v < 2000) {
                            //Log.d(TAG, pinnedLocations.getName() + "******************" + locationResult.getLastLocation().distanceTo(pinned) + "********************");
                            //System.out.println(pinnedLocations.getName() + "******************" + locationResult.getLastLocation().distanceTo(pinned) + "********************");

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                    .setSmallIcon(R.drawable.ic_notifications)
                                    .setContentTitle(pinnedLocations.getName())
                                    .setContentText("You're near this location")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true);

                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(0, builder.build());
                        }
                    }
                }
            }, null);
        } else {
            stopSelf();
        }
    }

    private void buildNotification() {
        String stop = "stop";

        @SuppressLint("LaunchActivityFromNotification") PendingIntent broadcastIntent = PendingIntent.getBroadcast(getBaseContext(), 0, new Intent(stop), PendingIntent.FLAG_IMMUTABLE);

        //create persistent notification
        @SuppressLint("LaunchActivityFromNotification") NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Snare")
                .setContentText("Tracking Location for location-based reminders")
                .setOngoing(true)
                .setContentIntent(broadcastIntent);

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Snare", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setShowBadge(false);
        channel.setDescription("Location Tracking");
        channel.setSound(null, null);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(channel);

        startForeground(1, builder.build());
    }

    @Override
    public void onDestroy() {
        list2.clear();
    }

    public class MyBinder extends Binder {
        public LocationForegroundService getService() {
            return LocationForegroundService.this;
        }
    }

}
