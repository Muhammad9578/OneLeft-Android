package com.oneleft.app.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.oneleft.app.R;

import java.util.HashMap;

public class App extends Application {

    private static RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();

        requestQueue = Volley.newRequestQueue(this);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Game Notifications";
            String description = "Get notified about game status";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(getString(R.string.noti_channel_id), name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static void saveFCMToken(Context context, String token) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("FCM_TOKEN", token)
                .apply();
    }

    public static String getFCMToken(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("FCM_TOKEN", "");
    }

    public static void saveStripeKeys(Context context, String publishableKey, String secretKey) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("publishableKey", publishableKey)
                .putString("secretKey", secretKey)
                .apply();
    }

    public static HashMap<String, String> getStripeKeys(Context context){
        HashMap<String, String> keys = new HashMap<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        keys.put("publishableKey", sharedPreferences.getString("publishableKey", ""));
        keys.put("secretKey", sharedPreferences.getString("secretKey", ""));

        return keys;
    }
}