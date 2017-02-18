package com.example.android.sunshine.wear;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Boram on 2017-02-16.
 */

public class MyWearableListenerService extends WearableListenerService
        implements DataApi.DataListener {
    private static final String TAG = "MyWearableListenerService";
    private static final String REQ_WEATHER_PATH = "/sunshine-req";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d(TAG, "onMessageReceived : " + messageEvent);

        String path = messageEvent.getPath();
        Log.d(TAG, "message Path : " + path);

        if(path.equals(REQ_WEATHER_PATH)) {
            Context context = this.getApplicationContext();
            context.startService(new Intent(context, WearableIntentService.class));
        }
    }
}
