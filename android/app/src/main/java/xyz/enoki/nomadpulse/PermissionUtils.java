package xyz.enoki.nomadpulse;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

public final class PermissionUtils {
    // Private constructor to prevent instantiation
    private PermissionUtils() {
        throw new AssertionError("PermissionUtils is a utility class and should not be instantiated");
    }

    /**
     * Check if the app has location permissions (FINE or COARSE)
     * @param context The context to check permissions with
     * @return true if at least one of FINE or COARSE location permission is granted, false if context is null
     */
    public static boolean hasLocationPermissions(Context context) {
        // Validate context parameter
        if (context == null) {
            return false;
        }

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
