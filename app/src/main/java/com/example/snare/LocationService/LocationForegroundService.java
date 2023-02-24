package com.example.snare.LocationService;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.snare.Entities.PinnedLocations;
import com.example.snare.Entities.User;
import com.example.snare.NotificationsPkg.FCMSend;
import com.example.snare.dao.PinnedLocationsDataBase;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LocationForegroundService extends Service {

    private final IBinder iBinder = new MyBinder();
    private static final String CHANNEL_ID = "2";

    //experimentation
    private List<PinnedLocations> list;

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

        list = PinnedLocationsDataBase.getDatabase(this).pinnedLocationsDao().GetAllPinnedLocations();

        buildNotification();

        requestLocationUpdates();
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
                    int delay = 1000; // delay for 1 sec.
                    int period = 15000; // repeat every 15 sec.
                    final int[] count = {0};
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask()
                    {
                        public void run()
                        {
                            // Your code to execute when having the location data

                            //if any of the pinned locations on the device is closer than 1Km it will
                            //do what's inside the if statement
                            for(PinnedLocations pinnedLocations: list) {
                                Location pinned = new Location("");
                                pinned.setLatitude(Double.parseDouble(pinnedLocations.getLat()));
                                pinned.setLongitude(Double.parseDouble(pinnedLocations.getLng()));
                                float v = Objects.requireNonNull(locationResult.getLastLocation()).distanceTo(pinned);
                                if(v < 1000) {
                                    Log.d(TAG, pinnedLocations.getName() + "******************" + locationResult.getLastLocation().distanceTo(pinned) + "********************");
                                    System.out.println(pinnedLocations.getName() + "******************" + locationResult.getLastLocation().distanceTo(pinned) + "********************");

                                    //check if it's working
                                    /*NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                            .setSmallIcon(R.drawable.ic_notifications)
                                            .setContentTitle(pinnedLocations.getName())
                                            .setContentText("You're near this location")
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            .setAutoCancel(true);

                                    NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.notify(0, builder.build());*/
                                }
                            }

                            count[0]++;
                        }
                    }, delay, period);
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

    public class MyBinder extends Binder {
        public LocationForegroundService getService() {
            return LocationForegroundService.this;
        }
    }

}
