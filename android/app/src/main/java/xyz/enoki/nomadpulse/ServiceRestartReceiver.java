package xyz.enoki.nomadpulse;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;

public class ServiceRestartReceiver extends BroadcastReceiver {
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

        // Verify that the intent is one we expect and from a trusted source
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // BOOT_COMPLETED is a system action, safe to handle
            startLocationServiceIfPermitted(context);
        } else if (ACTION_RESTART_SERVICE.equals(action)) {
            // Our custom action - verify it's from our own package
            if (context.getPackageName().equals(intent.getPackage()) || 
                intent.getComponent() != null && 
                context.getPackageName().equals(intent.getComponent().getPackageName())) {
                startLocationServiceIfPermitted(context);
            }
        }
    }

    private void startLocationServiceIfPermitted(Context context) {
        // Only start the service if we have location permissions
        if (!hasLocationPermissions(context)) {
            return;
        }

        Intent serviceIntent = new Intent(context, LocationForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private boolean hasLocationPermissions(Context context) {
        // Check for basic location permissions (FINE or COARSE)
        boolean hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        // At least one of FINE or COARSE location permission is required
        return hasFineLocation || hasCoarseLocation;
    }
}
