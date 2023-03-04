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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LocationForegroundService extends Service {

    private final IBinder iBinder = new MyBinder();
    private static final String CHANNEL_ID = "2";
    private DatabaseReference mDatabaseShouts;

    private List<PinnedLocations> list2 = new ArrayList<>();
    private List<LatLng> shoutList = new ArrayList<>();

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

        mDatabaseShouts = FirebaseDatabase.getInstance().getReference("Shouts");

        filter();
        getShouts();

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

    private void getShouts() {
        shoutList.clear();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            mDatabaseShouts.child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()).get().addOnSuccessListener(dataSnapshot -> {
                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    LatLng temp = null;
                    for(DataSnapshot snapshot1: snapshot.getChildren()) {
                        double x = Double.parseDouble(Objects.requireNonNull(snapshot1.child("latitude").getValue()).toString());
                        double y = Double.parseDouble(Objects.requireNonNull(snapshot1.child("longitude").getValue()).toString());
                        temp = new LatLng(x, y);
                    }
                    shoutList.add(temp);
                }
            });
        }
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000)
                .setMinUpdateIntervalMillis(30000)
                .setMaxUpdateDelayMillis(30000)
                .setWaitForAccurateLocation(true)
                .build();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getBaseContext());
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED && isNetworkAvailable(getApplicationContext())) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    //filtering list2
                    filter();

                    //if any of the pinned locations on the device is closer than 1Km it will send a notification
                    for(PinnedLocations pinnedLocations: list2) {
                        Location pinned = new Location("");
                        pinned.setLatitude(Double.parseDouble(pinnedLocations.getLat()));
                        pinned.setLongitude(Double.parseDouble(pinnedLocations.getLng()));
                        float v = Objects.requireNonNull(locationResult.getLastLocation()).distanceTo(pinned);
                        if(v < 1000) {
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

                    //if any of the shouts for the user is closer than 500m it will send a notification
                    for(LatLng latLng: shoutList) {
                        Location shout = new Location("");
                        shout.setLatitude(latLng.latitude);
                        shout.setLongitude(latLng.longitude);
                        float v1 = Objects.requireNonNull(locationResult.getLastLocation()).distanceTo(shout);
                        if(v1 < 500) {
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                    .setSmallIcon(R.drawable.ic_notifications)
                                    .setContentTitle("Shout")
                                    .setContentText("You're near a shout")
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true);

                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(1, builder.build());
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

        startForeground(2, builder.build());
    }

    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onDestroy() {
        list2.clear();
        shoutList.clear();
    }

    public class MyBinder extends Binder {
        public LocationForegroundService getService() {
            return LocationForegroundService.this;
        }
    }

}
