package com.brauer.android.yofyz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.brauer.android.yofyz.options.Options;

public class ServiceRestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Options options = Options.fromDb(context);
        if (options.isServiceRunning()) {
            context.startForegroundService(new Intent(context, BackgroundService.class));
        }
    }
}
