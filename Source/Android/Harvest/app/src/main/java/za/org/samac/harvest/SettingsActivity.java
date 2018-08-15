package za.org.samac.harvest;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Method;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by group, with group headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener(){
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            /*if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
*/
            /*} else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }*/
//            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
//            }
            return true;
        }
    };

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }


    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.

            //Disabled until fix
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AccountPreferenceFragment.class.getName().equals(fragmentName)
                || HelpPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows account preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class AccountPreferenceFragment extends PreferenceFragment {

        private FirebaseDatabase database;
        private DatabaseReference userAdmin;

        private FirebaseUser user;

        private boolean isFarmer;

        enum AccountAction{
            EMAIL,
            PASSWORD,
            DELETE
        }
        AccountAction accountAction;

        private PreferenceScreen preferenceScreen;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_account);

            setHasOptionsMenu(true);

            preferenceScreen = (PreferenceScreen) findPreference(getString(R.string.pref_account_key));

            //Firebase setup
            database = FirebaseDatabase.getInstance();
            user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                userAdmin = database.getReference("/" + user.getUid() + "/admin/");

                //determine if farmer
                isFarmer = true;
                for(UserInfo profile : user.getProviderData()){
                    if (profile.getProviderId().equals(PhoneAuthProvider.PROVIDER_ID)){
                        isFarmer = false;
                        break;
                    }
                }
            }

            //Hide farmer stuff
            if (!isFarmer){
                preferenceScreen.removePreference(findPreference(getString(R.string.pref_account_category_extra_key)));
                preferenceScreen.removePreference(findPreference(getString(R.string.pref_account_category_login_key)));
                preferenceScreen.removePreference(findPreference(getString(R.string.pref_account_category_management_key)));

                getActivity().setTitle(user.getPhoneNumber());
            }
            else {
                getActivity().setTitle(user.getEmail());

                //Set summaries
                userAdmin.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String fname = dataSnapshot.child("firstname").getValue(String.class);
                        String sname = dataSnapshot.child("lastname").getValue(String.class);
                        String org = dataSnapshot.child("organization").getValue(String.class);

                        findPreference(getString(R.string.pref_account_fname_key)).setSummary(fname);
                        findPreference(getString(R.string.pref_account_sname_key)).setSummary(sname);
                        findPreference(getString(R.string.pref_account_organization_key)).setSummary(org);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                /*
                If user need to reauth, then if(reAuth()){repeat action}
                TODO: Perhaps give user 3? chances, and if they are used up, then sign the user out.
                 */

                //Update Email
                //TODO: Verification email
                findPreference(getString(R.string.pref_account_email_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, final Object newValue) {
                        Email(newValue.toString());
                        return false;
                    }
                });

                //Update Password
                findPreference(getString(R.string.pref_account_password_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Password(newValue.toString());
                        return false;
                    }
                });

                //Organization Name
                findPreference(getString(R.string.pref_account_organization_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        userAdmin.child("organization").setValue(newValue.toString());
                        preference.setSummary(newValue.toString());

                        return false;
                    }
                });

                //First Name
                findPreference(getString(R.string.pref_account_fname_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        userAdmin.child("firstname").setValue(newValue.toString());
                        preference.setSummary(newValue.toString());

                        return false;
                    }
                });

                //Surname
                findPreference(getString(R.string.pref_account_sname_key)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        userAdmin.child("lastname").setValue(newValue.toString());
                        preference.setSummary(newValue.toString());

                        return false;
                    }
                });

                //Delete Account
                findPreference(getString(R.string.pref_account_delete_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                            builder.setMessage(R.string.pref_account_delete_alert_message)
                                    .setTitle(R.string.pref_account_delete_alert_title)
                                    .setPositiveButton(R.string.pref_account_delete_alert_yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Another confirmation dialog
                                            AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());

                                            builder1.setTitle(R.string.pref_account_delete_alert_sure_title)
                                                    .setMessage(R.string.pref_account_delete_alert_sure_message)
                                                    .setPositiveButton(R.string.pref_account_delete_alert_sure_yes, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            Delete();
                                                        }
                                                    })
                                                    .setNegativeButton(R.string.pref_account_delete_alert_sure_no, new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.cancel();
                                                        }
                                                    })
                                            ;

                                            builder1.show();
                                        }
                                    })
                                    .setNegativeButton(R.string.pref_account_delete_alert_no, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    });

                            builder.show();

                        return true;
                    }
                });
            }

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("example_text"));
//            bindPreferenceSummaryToValue(findPreference("example_list"));
        }

        private void Delete(){
            FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        database.getReference("/" + user.getUid()).setValue(null);
                        Log.d(TAG, "User account deleted");
                        startActivity(new Intent(getActivity(), SignIn_Choose.class));
                        getActivity().finish();
                    } else {
                        Log.e(TAG, "User account deletion failed " + task.getException().toString());
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthRecentLoginRequiredException e) {
                            accountAction = AccountAction.DELETE;
                            reAuth(false, null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public void Email(final String email){
            user.updateEmail(email.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        getActivity().setTitle(email.toString());
                        userAdmin.child("email").setValue(email.toString());
                        Log.d(TAG, "User email updated.");
                    }
                    else {
                        try {
                            throw task.getException();
                        }
                        catch (FirebaseAuthRecentLoginRequiredException e){
                            accountAction = AccountAction.EMAIL;
                            reAuth(false, email);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        public void Password(final String password){
            user.updatePassword(password.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User password updated.");
                    }
                    else {
                        try{
                            throw task.getException();
                        }
                        catch (FirebaseAuthRecentLoginRequiredException e){
                            accountAction = AccountAction.PASSWORD;
                            reAuth(false, password);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        /**
         * Reauthenticate the user.
         * @param again true if the user is trying again.
         */
        private void reAuth(boolean again, final String passAlong){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();

            builder.setView(inflater.inflate(R.layout.dialog_reauth, null))
                    .setPositiveButton(R.string.pref_account_reAuthConfirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            EditText email = dialog.findViewById(R.id.pref_account_reAuth_email);
//                            EditText pass  = getActivity().findViewById(R.id.pref_account_reAuth_password);
                            EditText email = ((AlertDialog) dialog).findViewById(R.id.pref_account_reAuth_email);
                            EditText pass = ((AlertDialog) dialog).findViewById(R.id.pref_account_reAuth_password);
//                            result = new String[]{email.getText().toString(), pass.getText().toString()};
                            AuthCredential credential = EmailAuthProvider.getCredential(email.getText().toString(), pass.getText().toString());
                            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.d(TAG, "User re-authenticated.");
                                        switch (accountAction){
                                            case DELETE:
                                                Delete();
                                                break;
                                            case EMAIL:
                                                Email(passAlong);
                                                break;
                                            case PASSWORD:
                                                Password(passAlong);
                                                break;
                                        }
                                    }
                                    else {
                                        reAuth(true, null);
                                    }
                                }
                            });
                        }
                    })
                    .setNegativeButton(R.string.pref_account_reAuthCancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
            ;
            if (again){
                builder.setTitle(R.string.pref_account_reAuthAgain);
            }
            else {
                builder.setTitle(R.string.pref_account_reAuth);
            }
            builder.show();
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class HelpPreferenceFragment extends PreferenceFragment{

        public final static String KEY_NONEWACTIVITY = "KEY_NONEWACTIVITY";

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_help);

            setHasOptionsMenu(true);

            findPreference(getString(R.string.pref_help_intro_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Bundle extras = new Bundle();
                    extras.putBoolean(KEY_NONEWACTIVITY, true);
                    Intent intent = new Intent(getActivity(), IntroViewFlipper.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                    return true;
                }
            });

            findPreference(getString(R.string.pref_help_tutorial_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Bundle extras = new Bundle();
                    extras.putBoolean(KEY_NONEWACTIVITY, true);
                    Intent intent = new Intent(getActivity(), ViewFlipperActivity.class);
                    intent.putExtras(extras);
                    startActivity(intent);
                    return true;
                }
            });

            findPreference(getString(R.string.pref_help_man_key)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Log.i(TAG, "Zhu Li, Do the Thing!");
                    return true;
                }
            });
        }

    }
}
