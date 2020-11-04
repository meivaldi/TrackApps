package com.meivaldi.trackerapps.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.meivaldi.trackerapps.service.LocationService;

public class RestartBackgroundService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, LocationService.class));
        } else {
            context.startService(new Intent(context, LocationService.class));
        }
    }
}
