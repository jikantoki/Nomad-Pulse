package xyz.enoki.nomadpulse;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public class PermissionUtils {
    /**
     * Check if the app has location permissions (FINE or COARSE)
     * @param context The context to check permissions with
     * @return true if at least one of FINE or COARSE location permission is granted
     */
    public static boolean hasLocationPermissions(Context context) {
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
