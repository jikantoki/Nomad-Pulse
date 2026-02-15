package xyz.enoki.nomadpulse;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start the foreground service only if it's not already running AND we have location permissions
        if (!isServiceRunning(LocationForegroundService.class) && hasLocationPermissions()) {
            Intent serviceIntent = new Intent(this, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't stop the service when activity is destroyed
        // The service will continue running in the background
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasLocationPermissions() {
        // Check for basic location permissions (FINE or COARSE)
        boolean hasFineLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        boolean hasCoarseLocation = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        // At least one of FINE or COARSE location permission is required
        return hasFineLocation || hasCoarseLocation;
    }
}
