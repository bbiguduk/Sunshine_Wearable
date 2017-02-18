package com.example.android.sunshine.wear;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.data.WeatherContract;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by Boram on 2017-02-09.
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class WearableIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "WearableIntentService";

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };

    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;

    private GoogleApiClient mGoogleApiClient;

    private static final String PATH = "/sunshine";
    private static final String KEY_WEATHER_ID = "com.boram.key.weather_id";
    private static final String KEY_TEMP_MAX = "com.boram.key.max_temp";
    private static final String KEY_TEMP_MIN = "com.boram.key.min_temp";

    private int mWeatherId;
    private double mMaxTemp;
    private double mMinTemp;

    public WearableIntentService() {
        super("WearableIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");

        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(System.currentTimeMillis());
        Cursor cursor = getContentResolver().query(weatherUri, FORECAST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if(cursor == null) {
            return;
        }

        if(!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        mWeatherId = cursor.getInt(INDEX_WEATHER_ID);
        mMaxTemp = cursor.getDouble(INDEX_MAX_TEMP);
        mMinTemp = cursor.getDouble(INDEX_MIN_TEMP);
        cursor.close();

        Log.d(TAG, "mWeatherId : " + mWeatherId);
        Log.d(TAG, "mMaxTemp : " + mMaxTemp);
        Log.d(TAG, "mMinTemp : " + mMinTemp);

        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }

        if(!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "GoogleApiClient Connected");

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH);
        putDataMapRequest.setUrgent();

        putDataMapRequest.getDataMap().putInt(KEY_WEATHER_ID, mWeatherId);
        putDataMapRequest.getDataMap().putDouble(KEY_TEMP_MAX, mMaxTemp);
        putDataMapRequest.getDataMap().putDouble(KEY_TEMP_MIN, mMinTemp);

        Log.d(TAG, "mWeatherId : " + mWeatherId);
        Log.d(TAG, "mMaxTemp : " + mMaxTemp);
        Log.d(TAG, "mMinTemp : " + mMinTemp);

        putDataMapRequest.getDataMap().putLong("time", System.currentTimeMillis());

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {

                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if(dataItemResult.getStatus().isSuccess()) {
                            Log.d(TAG, "Success sent for weather data");
                        } else {
                            Log.d(TAG, "Fail sent for weather data");
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }
}
