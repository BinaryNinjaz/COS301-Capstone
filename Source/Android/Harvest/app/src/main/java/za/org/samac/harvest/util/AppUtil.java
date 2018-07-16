package za.org.samac.harvest.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Vector;

import za.org.samac.harvest.R;

public class AppUtil {
    //this file is used to keep the user logged in on opening app
    public static String SHARED_PREFERENCES_KEY_EMAIL = "userEmail";//key for my map (key must be unique)

    public static boolean isUserSignedIn(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_prefrences), Context.MODE_PRIVATE);
//        return sharedPreferences.contains(SHARED_PREFERENCES_KEY_EMAIL);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //User signed in
            return true;
        }
        else {
            //User not signed in
            return false;
        }
    }

    public static boolean isUserSignedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            //User signed in
            return true;
        }
        else {
            //User not signed in
            return false;
        }
    }

    public static void writeStringToSharedPrefs(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_prefrences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();//commit writes synchronously
    }

    public static String readStringFromSharedPrefs(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_shared_prefrences), Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, null);
    }

}
