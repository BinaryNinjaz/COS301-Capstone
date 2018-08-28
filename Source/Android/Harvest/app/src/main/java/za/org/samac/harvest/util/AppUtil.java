package za.org.samac.harvest.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.widget.Adapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
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

    public static String normalisePhoneNumber(String number, Context context){
        number = number.replaceAll("-", "");
        number = number.replaceAll(" ", "");

        //Thank you Wais
        //https://stackoverflow.com/questions/5402253/getting-telephone-country-code-with-android

        if (!number.startsWith("+") || !number.startsWith("00")) {
            String CountryZipCode = "";
            String CountryID = "";

            TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (manager != null) {
                CountryID = manager.getSimCountryIso().toUpperCase();
            }
            String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
            for (String aRl : rl) {
                String[] g = aRl.split(",");
                if (g[1].trim().equals(CountryID.trim())) {
                    CountryZipCode = g[0];
                    break;
                }
            }
            number = number.replaceFirst("0", "+" + CountryZipCode);
        }

        return number;
    }

    public static String convertDate(double milliseconds){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy HH:mm ZZ");
        return format.format(milliseconds);
    }

    public static double convertDate(String fbString){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy HH:mm ZZ");
        Date date = format.parse(fbString, new ParsePosition(0));
        return date.getTime();
    }
}
