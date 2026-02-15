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
    // Recreate the service when task is removed
    Intent restartServiceIntent = new Intent(getApplicationContext(), LocationForegroundService.class);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startForegroundService(restartServiceIntent);
    } else {
        startService(restartServiceIntent);
    }
    super.onTaskRemoved(rootIntent);
}
```
- Restarts the service when the user removes the app from the recent apps list

#### onDestroy
```java
@Override
public void onDestroy() {
    super.onDestroy();
    Intent restartServiceIntent = new Intent(getApplicationContext(), ServiceRestartReceiver.class);
    restartServiceIntent.setAction("RestartService");
    sendBroadcast(restartServiceIntent);
}
```
- Broadcasts an intent to restart the service when it's destroyed

#### BOOT_COMPLETED
```xml
<intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED" />
    <action android:name="RestartService" />
</intent-filter>
```
- Starts the service automatically when the device boots up

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
```

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
