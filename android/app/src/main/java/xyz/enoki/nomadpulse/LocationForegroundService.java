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
import android.os.SystemClock;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class LocationForegroundService extends Service {
    private static final String TAG = "LocationFgService";
    private static final String CHANNEL_ID = "LocationServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        createNotificationChannel();
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
}
