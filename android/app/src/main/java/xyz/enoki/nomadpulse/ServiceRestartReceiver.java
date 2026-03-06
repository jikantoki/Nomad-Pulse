package xyz.enoki.nomadpulse;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class ServiceRestartReceiver extends BroadcastReceiver {
    private static final String TAG = "ServiceRestartReceiver";
    private static final String ACTION_RESTART_SERVICE = "xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        Log.d(TAG, "Received intent with action: " + action);

        // Verify that the intent is one we expect and from a trusted source
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // BOOT_COMPLETED is a system action, safe to handle
            Log.d(TAG, "Device booted, starting location service");
            startLocationServiceIfPermitted(context);
        } else if (ACTION_RESTART_SERVICE.equals(action)) {
            // Our custom action - verify it's from our own package
            if (context.getPackageName().equals(intent.getPackage()) || 
                intent.getComponent() != null && 
                context.getPackageName().equals(intent.getComponent().getPackageName())) {
                Log.d(TAG, "Restarting location service after task kill");
                startLocationServiceIfPermitted(context);
            }
        }
    }

    private void startLocationServiceIfPermitted(Context context) {
        // Only start the service if we have location permissions
        if (!PermissionUtils.hasLocationPermissions(context)) {
            Log.w(TAG, "Cannot start service: Location permissions not granted");
            return;
        }

        // Check if service is already running to avoid duplicate starts
        if (isServiceRunning(context, LocationForegroundService.class)) {
            Log.d(TAG, "Service is already running, skipping start");
            return;
        }

        try {
            Intent serviceIntent = new Intent(context, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
                Log.d(TAG, "Started foreground service (API >= 26)");
            } else {
                context.startService(serviceIntent);
                Log.d(TAG, "Started service (API < 26)");
            }
        } catch (IllegalStateException e) {
            // This can happen if the app is in the background on Android 8+
            Log.e(TAG, "Failed to start service: " + e.getMessage(), e);
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when starting service: " + e.getMessage(), e);
        }
    }

    private boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            try {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (serviceClass.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking if service is running: " + e.getMessage(), e);
            }
        }
        return false;
    }
}
