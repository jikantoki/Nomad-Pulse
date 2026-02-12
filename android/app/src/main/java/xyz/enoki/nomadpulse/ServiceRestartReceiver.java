package xyz.enoki.nomadpulse;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        
        // Verify that the intent is one we expect
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "xyz.enoki.nomadpulse.ACTION_RESTART_SERVICE".equals(action)) {
            
            // Restart the service when device boots or service is killed
            Intent serviceIntent = new Intent(context, LocationForegroundService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
