package za.org.samac.harvest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import za.org.samac.harvest.util.AppUtil;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class SignIn_SignUp extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references (used the ID names).
    private EditText edtFirstName;
    private EditText edtSurname;
    private EditText edtEmail;
    private EditText edtOrganization;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private View signUp_progress;
    private View signUp_form;
    private Button btnSignUp;
    private Button btnLogin;

    private String oldEmail;

    private FirebaseAuth mAuth;//declared an instance of FirebaseAuth
    private static final String TAG = "EmailPassword";//tag I used for log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_signup);
        // Set up the sign up form.
        edtFirstName = findViewById(R.id.edtFirstName);

        edtSurname = findViewById(R.id.edtSurname);

        edtEmail = findViewById(R.id.edtEmail);

        edtOrganization = findViewById(R.id.edtOrganization);

        edtPassword = findViewById(R.id.edtPassword);

        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        edtConfirmPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if(validateForm()) {
                        hideSoftKeyboard();
                        createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
                    }

                    return true;
                }
                return false;
            }
        });

        signUp_form = findViewById(R.id.signUp_form);
        signUp_progress = findViewById(R.id.signUp_progress);

        //user presses Sign up button
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()) {
                    hideSoftKeyboard();
                    createAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
                }
            }
        });

        //user presses Already have an account? button
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignIn_SignUp.this, SignIn_Farmer.class);
                startActivity(intent);
                finish();//kill current Activity
            }
        });

        mAuth = FirebaseAuth.getInstance();//initialisation the FirebaseAuth instance

        //Set organization to email
//        edtEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (!hasFocus){
//                    String email = edtEmail.getText().toString();
//                    String org = edtOrganization.getText().toString();
//                    if (org.equals("") || org.equals(oldEmail)){
//                        edtOrganization.setText(email);
//                    }
//                    oldEmail = email;
//                }
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);//had to create method for this
    }

    private void updateUI(FirebaseUser currentUser) {

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        signUp_form.setVisibility(View.INVISIBLE);
        signUp_progress.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            AppUtil.writeStringToSharedPrefs(getApplicationContext(), AppUtil.SHARED_PREFERENCES_KEY_EMAIL, user.getEmail());

                            Snackbar.make(signUp_form, "Registration Successful", Snackbar.LENGTH_LONG).show();
                            signUp_progress.setVisibility(View.GONE);

                            //Add the name, surname, and organization to Firebase
                            EditText fname, sname, org, email;
                            fname = findViewById(R.id.edtFirstName);
                            sname = findViewById(R.id.edtSurname);
                            org = findViewById(R.id.edtOrganization);
                            email = findViewById(R.id.edtEmail);

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference userAdmin = database.getReference(user.getUid() + "/admin/");
                            userAdmin.child("firstname").setValue(fname.getText().toString());
                            userAdmin.child("surname").setValue(sname.getText().toString());
                            userAdmin.child("organization").setValue(org.getText().toString());
                            userAdmin.child("email").setValue(email.getText().toString());

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(SignIn_SignUp.this, InformationActivity.class);
                                    startActivity(intent);
                                    finish();//kill current Activity
                                }
                            }, 1500);

                        } else {

                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            /*Toast.makeText(SignIn_SignUp.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);*/

                            signUp_form.setVisibility(View.VISIBLE);
                            signUp_progress.setVisibility(View.GONE);
                            Snackbar.make(signUp_form, "Registration Failed. Please try again.", Snackbar.LENGTH_LONG)
                                    .setAction("OK", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                        }
                                    })
                                    .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                    .show();
                        }

                        // ...
                    }
                });
    }

    //firebase suggested validate
    private boolean validateForm() {
        boolean valid = true;
        View focusView = null;

        String confirmPassword = edtConfirmPassword.getText().toString();
        if (TextUtils.isEmpty(confirmPassword)) {
            edtConfirmPassword.setError("Required.");
            focusView = edtConfirmPassword;
            valid = false;
        } else {
            edtConfirmPassword.setError(null);
        }

        String password = edtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Required.");
            focusView = edtPassword;
            valid = false;
        } else {
            edtPassword.setError(null);
        }

        if (isPasswordValid(password)) {
            edtPassword.setError("Passwords must be at least 6 characters.");
            focusView = edtPassword;
            valid = false;
        } else {
            edtPassword.setError(null);
        }



        //check if two passwords entered match
        if (!confirmPassword.equals(password)) {
            valid = false;
        }

        String email = edtEmail.getText().toString();
        if (isEmailValid(email) == false) {
            edtEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            focusView.requestFocus();
            valid = false;
        } else {
            edtEmail.setError(null);
        }

        //TODO: Should this really be necessary?
//        String surname = edtSurname.getText().toString();
//        if (TextUtils.isEmpty(surname)) {
//            edtSurname.setError("Required.");
//            focusView = edtSurname;
//            valid = false;
//        } else {
//            edtSurname.setError(null);
//        }
//
//        String firstName = edtFirstName.getText().toString();
//        if (TextUtils.isEmpty(firstName)) {
//            edtFirstName.setError("Required.");
//            focusView = edtFirstName;
//            valid = false;
//        } else {
//            edtFirstName.setError(null);
//        }

        if (valid == false) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }

        return valid;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(edtEmail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }*/


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    /*private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        edtEmail.setError(null);
        edtPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            edtPassword.setError(getString(R.string.error_invalid_password));
            focusView = edtPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            edtEmail.setError(getString(R.string.error_field_required));
            focusView = edtEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            edtEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }*/

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 5;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            signUp_form.setVisibility(show ? View.GONE : View.VISIBLE);
            signUp_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signUp_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            signUp_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            signUp_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    signUp_progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            signUp_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            signUp_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(SignIn_SignUp.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        //edtEmail.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                edtPassword.setError(getString(R.string.error_incorrect_password));
                edtPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

