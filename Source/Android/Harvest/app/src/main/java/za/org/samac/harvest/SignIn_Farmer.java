package za.org.samac.harvest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import za.org.samac.harvest.util.AppUtil;
import za.org.samac.harvest.util.Data;

/**
 * A login screen that offers login via email/password.
 */
public class SignIn_Farmer extends AppCompatActivity implements  GoogleApiClient.OnConnectionFailedListener {

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

    //used same names as IDs in xml
    private static EditText edtEmail;
    private static EditText edtPassword;
    private View login_progress;
    private View login_form;
    private Button btnSignup;
    private Button btnLogin;
    private Button btnLoginWithGoogle;
    private TextView linkForgotAccountDetails;
    private LinearLayout linearLayout;
    private ImageView imageView;

    private FirebaseAuth mAuth;//declared an instance of FirebaseAuth
    private static final String TAG = "EmailPassword";
    public static GoogleSignInClient  mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;
    private static GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_farmer);

        // Set up the login form.
        edtEmail = findViewById(R.id.edtEmail);

        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    if(validateForm()) {
                        hideSoftKeyboard();
                        signInToAccount(edtEmail.getText().toString(), edtPassword.getText().toString());//attemptLogin();
                    }
                    return true;
                }
                return false;
            }
        });

        login_form = findViewById(R.id.login_form);
        login_progress = findViewById(R.id.login_progress);

        //user presses Log in button, validates and if all is well goes to actual app screen
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(validateForm()) {
                    hideSoftKeyboard();
                    signInToAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
                }
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //user presses Log in with Google button, validates and if all is well goes to actual app screen
        btnLoginWithGoogle = findViewById(R.id.btnLoginWithGoogle);
        btnLoginWithGoogle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnLoginWithGoogle:
                        signInWithGoogle();
                        break;
                }
            }
        });

        //user presses Forgot account details link
        linkForgotAccountDetails = findViewById(R.id.linkForgotAccountDetails);
        linkForgotAccountDetails.setMovementMethod(LinkMovementMethod.getInstance());
        linkForgotAccountDetails.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(SignIn_Farmer.this);

                alert.setMessage(Html.fromHtml("<b>"+"Reset Password"+"</b>"+"<br>"+"Please enter your email, you will receive an email to reset your password."));

                final EditText email = new EditText(SignIn_Farmer.this);
                email.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                email.setHint("Email");

                alert.setView(email);

                alert.setNegativeButton("Cancel", null);
                alert.setPositiveButton("Request Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        login_form.setVisibility(View.INVISIBLE);
                        login_progress.setVisibility(View.VISIBLE);

                        mAuth.sendPasswordResetEmail(email.getText().toString());//send reset password email

                        login_form.setVisibility(View.VISIBLE);
                        login_progress.setVisibility(View.GONE);
                        Snackbar.make(login_form, "Please check your email.", Snackbar.LENGTH_LONG)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                })
                                .setActionTextColor(getResources().getColor(android.R.color.holo_green_light ))
                                .show();
                    }
                });

                alert.show();
            }
        });

        mAuth = FirebaseAuth.getInstance();//initialisation the FirebaseAuth instance
    }

    public static void setEdtEmail(EditText e) {
        edtEmail = e;
    }

    public static void setEdtPassword(EditText p) {
        edtPassword = p;
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);//had to create method for this
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        login_form.setVisibility(View.INVISIBLE);
        login_progress.setVisibility(View.VISIBLE);

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            AppUtil.writeStringToSharedPrefs(getApplicationContext(), AppUtil.SHARED_PREFERENCES_KEY_EMAIL, user.getEmail());

                            Snackbar.make(login_form, "Log In Successful", Snackbar.LENGTH_LONG).show();
                            login_progress.setVisibility(View.GONE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    startMain();
                                }
                            }, 1500);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            login_form.setVisibility(View.VISIBLE);
                            login_progress.setVisibility(View.GONE);
                            Snackbar.make(login_form, "Authentication Failed.", Snackbar.LENGTH_LONG)
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

    private void updateUI(FirebaseUser currentUser) {

    }

    private void startMain(){
        Data.setNeedsPull(false);
        Data data = new Data();
        data.notifyMe(this);
        data.pull();
    }

    public void pullDone(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignIn_Farmer.this);
        if(!prefs.getBoolean("firstTimeInMain", false)) {
            // run your one time code
            Intent intent = new Intent(SignIn_Farmer.this, ViewFlipperActivity.class);
            startActivity(intent);
            finish();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTimeInMain", true);
            editor.apply();

            Stats.GraphDB.createDefaults(this);
        } else {
            if (Data.hasFarm() && Data.hasOrchard() && Data.hasWorker()) {
                Intent intent = new Intent(SignIn_Farmer.this, MainActivity.class);//go to actual app
                startActivity(intent);
            }
            else {
                startActivity(new Intent(SignIn_Farmer.this, InformationActivity.class));//They're missing something.
            }
            finish();//kill current Activity
        }
    }

    private void signInToAccount(String email, String password) {
        Log.d(TAG, "signInToAccount:" + email);
        login_form.setVisibility(View.INVISIBLE);
        login_progress.setVisibility(View.VISIBLE);
        if (!validateForm()) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            mAuth.getCurrentUser().reload();
                            user = mAuth.getCurrentUser();
                            updateUI(user);

                            AppUtil.writeStringToSharedPrefs(getApplicationContext(), AppUtil.SHARED_PREFERENCES_KEY_EMAIL, user.getEmail());

                            if(user.isEmailVerified() == false) {
                                System.out.println("AppUtil.isUserSignedIn() ******** "+AppUtil.isUserSignedIn(getApplicationContext()));
                                FirebaseAuth.getInstance().signOut();
                                System.out.println("AppUtil.isUserSignedIn() ******** "+AppUtil.isUserSignedIn(getApplicationContext()));

                                login_form.setVisibility(View.VISIBLE);
                                login_progress.setVisibility(View.GONE);
                                Snackbar.make(login_form, "Login unsuccessful. Have you confirmed your email?", Snackbar.LENGTH_LONG)
                                        .setAction("OK", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        })
                                        .setActionTextColor(getResources().getColor(android.R.color.holo_red_light ))
                                        .show();
                            } else {
                                Snackbar.make(login_form, "Log In Successful", Snackbar.LENGTH_LONG).show();
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SignIn_Farmer.this);
                                if (prefs.contains("firstname")) {
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference userAdmin = database.getReference(user.getUid() + "/admin/");
                                    userAdmin.child("firstname").setValue(prefs.getString("firstname", ""));
                                    userAdmin.child("lastname").setValue(prefs.getString("lastname", ""));
                                    userAdmin.child("organization").setValue(prefs.getString("organization", ""));
                                    userAdmin.child("email").setValue(prefs.getString("email", ""));
                                    SharedPreferences.Editor editor = prefs.edit();
                                    editor.remove("firstname");
                                    editor.remove("lastname");
                                    editor.remove("organization");
                                    editor.remove("email");
                                    editor.commit();
                                }

                                login_progress.setVisibility(View.GONE);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        startMain();
                                    }
                                }, 1500);
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            /*Toast.makeText(SignIn_Farmer.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);*/

                            login_form.setVisibility(View.VISIBLE);
                            login_progress.setVisibility(View.GONE);
                            Snackbar.make(login_form, "Email or password incorrect. Please try again.", Snackbar.LENGTH_LONG)
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

    //validate edit fields
    public boolean validateForm() {
        boolean valid = true;
        View focusView = null;

        String email = edtEmail.getText().toString();

        if (isEmailValid(email) == false) {
            edtEmail.setError(getString(R.string.error_invalid_email));
            focusView = edtEmail;
            focusView.requestFocus();
            valid = false;
        } else if (TextUtils.isEmpty(email)) {
            edtEmail.setError("Required.");
            valid = false;
        } else {
            edtEmail.setError(null);
        }

        String password = edtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            edtPassword.setError("Required.");
            valid = false;
        } else {
            if (edtPassword != null) {
                edtPassword.setError(null);
            }
        }

        return valid;
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
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

            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
            login_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            login_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            login_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            login_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

