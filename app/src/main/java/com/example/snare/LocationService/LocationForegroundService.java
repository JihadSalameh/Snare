package com.example.snare.LocationService;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

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
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(3000);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getBaseContext());
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    String location = "Latitude : " + Objects.requireNonNull(locationResult.getLastLocation()).getLatitude() +
                            "\nLongitude : " + locationResult.getLastLocation().getLongitude();

                    Toast.makeText(LocationForegroundService.this, location, Toast.LENGTH_SHORT).show();
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
