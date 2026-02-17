# Background Service Implementation

## Overview

This document describes the implementation of the persistent background service for Nomad Pulse, which ensures the app continues running in the background even after task kill.

## Architecture

### Components

1. **LocationForegroundService** (`android/app/src/main/java/xyz/enoki/nomadpulse/LocationForegroundService.java`)
   - Main foreground service that keeps the app running in the background
   - Displays a persistent notification to the user
   - Automatically restarts when killed by the system

2. **ServiceRestartReceiver** (`android/app/src/main/java/xyz/enoki/nomadpulse/ServiceRestartReceiver.java`)
   - Broadcast receiver that restarts the service on device boot
   - Handles service restart when the service is destroyed

3. **MainActivity** (`android/app/src/main/java/xyz/enoki/nomadpulse/MainActivity.java`)
   - Starts the foreground service when the app launches
   - Service continues running even when the activity is destroyed

## Key Features

### 1. Persistent Notification

The service displays a non-dismissible notification using:
- `setOngoing(true)`: Prevents user from swiping away the notification
- `IMPORTANCE_LOW`: Low priority to minimize user disruption
- Custom icon and text in Japanese

### 2. Service Restart Mechanisms

#### START_STICKY
```java
return START_STICKY;
```
- Service automatically restarts if killed by the system due to memory pressure
- System will recreate the service as soon as resources are available

#### onTaskRemoved
```java
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
            // Use setExactAndAllowWhileIdle for Android M+ for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    restartPendingIntent
                );
            } else {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    restartPendingIntent
                );
            }
        } catch (SecurityException e) {
            Log.w(TAG, "SecurityException scheduling alarm: " + e.getMessage());
        }
    }

    // Stop the service to ensure it's properly cleaned up before restart
    stopSelf();
}
```
- Restarts the service when the user removes the app from the recent apps list
- Uses `AlarmManager.setExactAndAllowWhileIdle()` for reliable restart on Android 6.0+ (API 23+)
- Calls `stopSelf()` to properly clean up the service before restart
- Uses secured `PendingIntent` with `FLAG_IMMUTABLE` for Android 12+ compatibility

#### onDestroy
```java
@Override
public void onDestroy() {
    super.onDestroy();
    Log.d(TAG, "Service destroyed, scheduling restart");

    // Schedule restart using AlarmManager for reliability
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    restartPendingIntent
                );
            } else {
                alarmManager.set(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + 1000,
                    restartPendingIntent
                );
            }
        } catch (SecurityException e) {
            // Fallback to broadcast if alarm scheduling fails
            sendBroadcast(restartServiceIntent);
        }
    } else {
        // Fallback to broadcast if AlarmManager is not available
        sendBroadcast(restartServiceIntent);
    }
}
```
- Schedules service restart when it's destroyed
- Uses `AlarmManager` instead of direct broadcast for more reliable restart
- Includes fallback to broadcast if `AlarmManager` is unavailable or throws `SecurityException`

#### BOOT_COMPLETED
```xml
<intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED" />
    <action android:name="xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE" />
</intent-filter>
```
- Starts the service automatically when the device boots up
- Handles custom restart action from the service

### 3. Foreground Service Configuration

```xml
<service
    android:name=".LocationForegroundService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="location"
    android:stopWithTask="false" />
```

- `android:stopWithTask="false"`: Service continues running when the task is removed
- `android:foregroundServiceType="location"`: Declares this as a location service
- `android:exported="false"`: Service is not accessible to other apps

## Permissions

The following permissions are required in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

**Note on Alarm Permissions:**
- `SCHEDULE_EXACT_ALARM`: Required for scheduling exact alarms on Android 12+ (API 31+). User can revoke this permission. Apps must check if this permission is granted at runtime using `AlarmManager.canScheduleExactAlarms()`.
- `USE_EXACT_ALARM`: Added for Android 14+ (API 34+). This permission is not revocable for apps that need exact alarms for core functionality (like background location tracking). No runtime check is needed as this permission cannot be revoked by the user.

## Limitations

### Android 13+ Task Manager

On Android 13 (API 33) and later, users can stop foreground services from the Task Manager:
- A "Stop" button appears in the notification drawer for apps running foreground services
- Pressing this button will:
  - Terminate the entire app process
  - Remove the service notification
  - Prevent the service from restarting automatically

This is an Android system behavior that cannot be bypassed for security and battery management reasons.

### Force Stop

If the user force-stops the app from Settings → Apps:
- All app processes are terminated
- The service will not restart until the user opens the app again
- This is expected Android behavior

## Testing

To verify the implementation works correctly:

1. **Build and install the app**
   ```bash
   cd android
   ./gradlew assembleDebug
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

2. **Test task removal**
   - Open the app
   - Open recent apps and swipe away the Nomad Pulse app
   - Check that the notification remains visible
   - Open the app again to verify it's still running

3. **Test device reboot**
   - Reboot the device
   - Check that the notification appears after boot
   - Verify the service is running

4. **Test system service kill**
   - Run the app for an extended period
   - Monitor that the service restarts if killed by the system

## Integration with Existing Code

The foreground service works alongside the existing background geolocation plugin:
- The existing `@capacitor-community/background-geolocation` plugin handles actual location tracking
- The `LocationForegroundService` ensures the process stays alive to keep location tracking active
- Both components work together to provide persistent background location tracking

## Future Improvements

Potential enhancements:
1. Make notification text configurable from the JavaScript/TypeScript layer
2. Add service status callbacks to notify the web layer when service state changes
3. Implement smart battery optimization handling
4. Add user-configurable service settings
