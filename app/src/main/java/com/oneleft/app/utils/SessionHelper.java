package com.oneleft.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.oneleft.app.models.User;
import com.google.gson.Gson;

public class SessionHelper {


    public static boolean iseUserLoggedIn(Context context) {
        SharedPreferences prefs = getPrefs(context);
        String currentUser = prefs.getString("current_user", "");
        return currentUser != null && !currentUser.isEmpty();
    }

    public static void setCurrentUser(Context context, User user) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putString("current_user", new Gson().toJson(user)).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
