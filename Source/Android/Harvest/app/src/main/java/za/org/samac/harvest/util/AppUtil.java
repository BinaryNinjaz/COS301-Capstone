package za.org.samac.harvest.util;

import android.content.Context;
import android.content.SharedPreferences;

import za.org.samac.harvest.R;

public class AppUtil {
    //this file is used to keep the user logged in on opening app
    public static String SHARED_PREFERENCES_KEY_EMAIL = "userEmail";//key for my map (key must be unique)

    public static boolean isUserSignedIn(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_prefrences), Context.MODE_PRIVATE);
        return sharedPreferences.contains(SHARED_PREFERENCES_KEY_EMAIL);
    }

    public static void writeStringToSharedPrefs(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_prefrences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();//commit writes synchronously
    }
}
