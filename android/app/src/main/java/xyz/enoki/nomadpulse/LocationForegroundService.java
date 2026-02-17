package xyz.enoki.nomadpulse;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class LocationForegroundService extends Service {
    private static final String TAG = "LocationFgService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 15 * 60 * 1000; // 15分

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        createNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setupLocationUpdates();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        
        // Create notification
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NomadPulse")
            .setContentText("バックグラウンドで位置情報を取得しています")
            .setSmallIcon(R.drawable.ic_location_status)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Make notification persistent
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();

        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification);
        Log.d(TAG, "Started as foreground service");

        // Return START_STICKY to restart service if killed by system
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed, scheduling restart");

        // Remove location updates
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        // Schedule restart when service is destroyed using AlarmManager for reliability
        Intent restartServiceIntent = new Intent(getApplicationContext(), ServiceRestartReceiver.class);
        restartServiceIntent.setAction("xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE");
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartPendingIntent = PendingIntent.getBroadcast(
            getApplicationContext(),
            2,  // Different request code from onTaskRemoved
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Schedule restart in 1 second using setExactAndAllowWhileIdle
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 1000,
                        restartPendingIntent
                    );
                    Log.d(TAG, "Scheduled restart using setExactAndAllowWhileIdle");
                } else {
                    alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 1000,
                        restartPendingIntent
                    );
                    Log.d(TAG, "Scheduled restart using set");
                }
            } catch (SecurityException e) {
                // Fallback to broadcast if alarm scheduling fails
                Log.w(TAG, "SecurityException scheduling alarm, using broadcast: " + e.getMessage());
                sendBroadcast(restartServiceIntent);
            }
        } else {
            // Fallback to broadcast if AlarmManager is not available
            Log.w(TAG, "AlarmManager not available, using broadcast");
            sendBroadcast(restartServiceIntent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.d(TAG, "Task removed, scheduling restart");

        // Use AlarmManager for more reliable restart
        Intent restartServiceIntent = new Intent(getApplicationContext(), ServiceRestartReceiver.class);
        restartServiceIntent.setAction("xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE");
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartPendingIntent = PendingIntent.getBroadcast(
            getApplicationContext(),
            1,
            restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            try {
                // Schedule restart in 1 second using setExactAndAllowWhileIdle for better reliability
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 1000,
                        restartPendingIntent
                    );
                    Log.d(TAG, "Scheduled restart using setExactAndAllowWhileIdle");
                } else {
                    alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + 1000,
                        restartPendingIntent
                    );
                    Log.d(TAG, "Scheduled restart using set");
                }
            } catch (SecurityException e) {
                Log.w(TAG, "SecurityException scheduling alarm: " + e.getMessage());
            }
        }

        // Stop the service to ensure it's properly cleaned up before restart
        stopSelf();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("位置情報サービスの通知チャンネル");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private void setupLocationUpdates() {
        if (!PermissionUtils.hasLocationPermissions(this)) {
            Log.w(TAG, "Location permissions not granted, stopping service");
            stopSelf();
            return;
        }

        try {
            LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL
            )
            .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL)
            .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.w(TAG, "Received null location result");
                        return;
                    }
                    for (android.location.Location location : locationResult.getLocations()) {
                        Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                        sendLocationToServer(location.getLatitude(), location.getLongitude());
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            );
            Log.d(TAG, "Location updates requested");
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location updates: " + e.getMessage(), e);
            stopSelf();
        }
    }

    private void sendLocationToServer(double latitude, double longitude) {
        new Thread(() -> {
            try {
                // TODO: Replace with your actual server URL
                URL url = new URL("https://nomadpulse.enoki.xyz/php/update_location.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lat", latitude);
                jsonObject.put("lng", longitude);
                jsonObject.put("timestamp", System.currentTimeMillis());

                String jsonInputString = jsonObject.toString();
                Log.d(TAG, "Sending location to server: " + jsonInputString);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Server response code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Location sent successfully");
                } else {
                    Log.w(TAG, "Server returned non-OK response: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending location to server: " + e.getMessage(), e);
            }
        }).start();
    }
}
