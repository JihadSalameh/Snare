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
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.snare.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class LocationForegroundService extends Service {

    private final IBinder iBinder = new MyBinder();
    private static final String CHANNEL_ID = "2";

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

        buildNotification();

        requestLocationUpdates();
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(3000)
                .setMaxUpdateDelayMillis(1000)
                .setWaitForAccurateLocation(true)
                .build();
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getBaseContext());
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    String location = Objects.requireNonNull(locationResult.getLastLocation()).getLatitude() +
                            "\n" + locationResult.getLastLocation().getLongitude();

                    int delay = 5000; // delay for 5 sec.
                    int period = 1000; // repeat every sec.
                    final int[] count = {0};
                    Timer timer = new Timer();
                    timer.scheduleAtFixedRate(new TimerTask()
                    {
                        public void run()
                        {
                            // Your code to execute when having the location data

                            //just to make sure you have the data when app is in the background
                            //you should keep receiving notifications with the location
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "channel_id")
                                    .setSmallIcon(R.drawable.ic_notifications)
                                    .setContentTitle("example")
                                    .setContentText(location)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setAutoCancel(true);

                            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.notify(0, builder.build());

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
