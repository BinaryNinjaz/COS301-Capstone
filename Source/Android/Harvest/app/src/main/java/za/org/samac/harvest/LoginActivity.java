package za.org.samac.harvest;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.text.Html;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import za.org.samac.harvest.util.AppUtil;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

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
    private EditText edtEmail;
    private EditText edtPassword;
    private View login_progress;
    private View login_form;
    private Button btnSignup;
    private Button btnLogin;
    private Button btnLoginWithGoogle;
    private TextView linkForgotAccountDetails;

    private FirebaseAuth mAuth;//declared an instance of FirebaseAuth
    private static final String TAG = "EmailPassword";
    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    private static int RC_SIGN_IN = 100;
    private static GoogleSignInAccount account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        edtEmail = findViewById(R.id.edtEmail);

        edtPassword = findViewById(R.id.edtPassword);
        edtPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    signInToAccount(edtEmail.getText().toString(), edtPassword.getText().toString());//attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //Button mEmailSignInButton = (Button) findViewById(R.id.btnLogin);
        /*mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });*/

        login_form = findViewById(R.id.login_form);
        login_progress = findViewById(R.id.login_progress);

        //user presses Log in button, validates and if all is well goes to actual app screen
        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signInToAccount(edtEmail.getText().toString(), edtPassword.getText().toString());
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();

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

        //user presses Sign up button (goes to sign up screen)
        btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
                finish();//kill current Activity
            }
        });

        //user presses Forgot account details link
        linkForgotAccountDetails = findViewById(R.id.linkForgotAccountDetails);
        linkForgotAccountDetails.setMovementMethod(LinkMovementMethod.getInstance());
        linkForgotAccountDetails.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);

                //TODO: center "Reset Password"
                alert.setMessage(Html.fromHtml("<b>"+"Reset Password"+"</b>"+"<br>"+"Please enter your email, you will receive an email to recover your password."));

                final EditText email = new EditText(LoginActivity.this);
                email.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                email.setHint("Email");

                //TODO: adjust padding of text field

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

                /*Intent intent = new Intent(LoginActivity.this, LoginActivity.class);//refresh log in screen
                startActivity(intent);
                finish();//kill current Activity*/
            }
        });

        mAuth = FirebaseAuth.getInstance();//initialisation the FirebaseAuth instance
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);//had to create method for this

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        /*GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account == null) {
            Intent intent = new Intent(LoginActivity.this, LoginActivity.class);//go to log in screen
            startActivity(intent);
            finish();//kill current Activity
        } else {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();//kill current Activity
        }*/
        //updateUI(account);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            account.getId();

            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);//go to actual app
            startActivity(intent);
            finish();//kill current Activity
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);

            // If sign in fails, display a message to the user.

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
    }

    private void updateUI(FirebaseUser currentUser) {

    }

    private void signInToAccount(String email, String password) {
        Log.d(TAG, "signInToAccount:" + email);
        if (!validateForm()) {
            return;
        }

        login_form.setVisibility(View.INVISIBLE);
        login_progress.setVisibility(View.VISIBLE);
        if (password.length() < 6) {
            //int paddingCount = 6 - password.length();

            /*for(int i = 0; i < paddingCount; i++) {
                password += '#';
            }*/

            password += "s3cr3ts4uc3";//Letanyans idea for padding
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);

                            AppUtil.writeStringToSharedPrefs(getApplicationContext(), AppUtil.SHARED_PREFERENCES_KEY_EMAIL, user.getEmail());

                            Snackbar.make(login_form, "Log In Successful", Snackbar.LENGTH_LONG).show();
                            login_progress.setVisibility(View.GONE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);//go to actual app
                                    startActivity(intent);
                                    finish();//kill current Activity
                                }
                            }, 1500);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            /*Toast.makeText(LoginActivity.this, "Authentication failed.",
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

    private boolean validateForm() {
        boolean valid = true;

        String email = edtEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
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
            edtPassword.setError(null);
        }

        return valid;
    }

    /*private boolean mayRequestContacts() {
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
    }*/

    /**
     * Callback received when a permissions request has been completed.
     */
    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }*/

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: we are still debating on what value to use
        return password.length() > 0;
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

