package za.org.samac.harvest;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignIn_Foreman extends AppCompatActivity {

    private  static  final  String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";

    private TextView invalid;
    private TextView failure;
    private EditText phoneNumberField;

    private String phone;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneCallback;
    private boolean verificationInProgress = false;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_foreman);

        //Restore Instance State
        if (savedInstanceState != null){
            onRestoreInstanceState(savedInstanceState);
        }

        mAuth = FirebaseAuth.getInstance();

        failure = findViewById(R.id.signIn_foreman_failure);
        phoneNumberField = findViewById(R.id.signIn_foreman_phone_edit);

        phoneCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                //Already verified, or device says so.
                verificationInProgress = false;
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                verificationInProgress = false;
                if (e instanceof FirebaseAuthInvalidCredentialsException){
                    failure.setVisibility(View.VISIBLE);
                    failure.setText(R.string.signIn_foreman_invalfail);
                }
                else if (e instanceof FirebaseTooManyRequestsException){
                    failure.setVisibility(View.VISIBLE);
                    failure.setText(R.string.signIn_foreman_reqfail);
                }
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
//                super.onCodeSent(s, forceResendingToken);
                mVerificationId = s;
                mResendToken = forceResendingToken;
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(verificationInProgress){
            startPhoneNumberVerification(phoneNumberField.getText().toString());
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    private  void startPhoneNumberVerification(String phoneNumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                100,
                TimeUnit.SECONDS,
                this,
                phoneCallback
        );

        verificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void resendVerificationCode(String phoneNumber, PhoneAuthProvider.ForceResendingToken token){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                this,
                phoneCallback,
                token
        );
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential){
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    findFarm();
                }
                else {
                    findViewById(R.id.signIn_foreman_failure).setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void findFarm(){

    }
}
