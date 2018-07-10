package za.org.samac.harvest;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

    private static final int STATE_START = 0;
    private static final int STATE_CODE_SENT = 1;
    private static final int STATE_VERIFY_FAIL = 2;
    private static final int STATE_INVALID_NUMBER = 3;
    private static final int STATE_QUOTA_EXCEED = 4;
    private static final int STATE_FARM_NONE = 5;
    private static final int STATE_FARM_ONE = 6;
    private static final int STATE_FARM_MULTI = 7;
    private int state;

    private EditText phoneNumberField;
    private TextView SMSWarning;
    private Button logInButt;
    private TextView failure;
    private TextView verificationTip;
    private EditText verificationField;
    private LinearLayout verificationButts;
    private Button verificationOkay;
    private Button verificationResend;
    private TextView phoneConfTip;
    private TextView phoneConfLook;
    private TextView farmTip;
    private Spinner farmChoose;
    private TextView farmOneLook;
    private Button farmOkay;


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

        phoneNumberField = findViewById(R.id.signIn_foreman_phone_edit);
        SMSWarning = findViewById(R.id.signIn_foreman_warning);
        logInButt = findViewById(R.id.signIn_foreman_logIn_butt);
        failure = findViewById(R.id.signIn_foreman_failure);
        verificationTip = findViewById(R.id.signIn_foreman_verificationTip);
        verificationField = findViewById(R.id.signIn_foreman_verifyCode_edit);
        verificationButts = findViewById(R.id.signIn_foreman_verification_butts);
        verificationOkay = findViewById(R.id.signIn_foreman_verification_okayButt);
        verificationResend = findViewById(R.id.signIn_foreman_verification_resendButt);
        phoneConfTip = findViewById(R.id.signIn_foreman_phoneConf_tip);
        phoneConfLook = findViewById(R.id.signIn_foreman_phoneConf_Look);
        farmTip = findViewById(R.id.signIn_foreman_farm_tip);
        farmChoose = findViewById(R.id.signIn_foreman_farmChoose_Spinner);
        farmOneLook = findViewById(R.id.signIn_foreman_farmOne_look);
        farmOkay = findViewById(R.id.signIn_foreman_farm_okay);

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

    public void signInForemanButtClick(View v){

    }

    public void findFarm(){

    }

    private void updateUI(){
        switch (this.state){
            case STATE_START:
                phoneNumberField.setVisibility(View.VISIBLE);
                SMSWarning.setVisibility(View.VISIBLE);
                logInButt.setVisibility(View.VISIBLE);
                failure.setVisibility(View.GONE);
                verificationTip.setVisibility(View.GONE);
                verificationField.setVisibility(View.GONE);
                verificationButts.setVisibility(View.GONE);
                phoneConfTip.setVisibility(View.GONE);
                phoneConfLook.setVisibility(View.GONE);
                farmTip.setVisibility(View.GONE);
                farmChoose.setVisibility(View.GONE);
                farmOneLook.setVisibility(View.GONE);
                farmOkay.setVisibility(View.GONE);
                break;
            case STATE_CODE_SENT:
                failure.setVisibility(View.GONE);
                verificationTip.setVisibility(View.VISIBLE);
                verificationField.setVisibility(View.VISIBLE);
                verificationButts.setVisibility(View.VISIBLE);
                logInButt.setVisibility(View.GONE);
                SMSWarning.setVisibility(View.GONE);
                phoneNumberField.setFocusable(false);
                break;
            case STATE_VERIFY_FAIL:
                failure.setVisibility(View.VISIBLE);
                failure.setText(R.string.signIn_foreman_fail);
                break;
            case STATE_INVALID_NUMBER:
                failure.setVisibility(View.VISIBLE);
                failure.setText(R.string.signIn_foreman_invalfail);
                break;
            case STATE_QUOTA_EXCEED:
                failure.setVisibility(View.VISIBLE);
                failure.setText(R.string.signIn_foreman_reqfail);
            case STATE_FARM_NONE:
                showConfirmationBasics();
                farmTip.setText(R.string.signIn_foreman_farmNone);
                farmTip.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                farmOkay.setVisibility(View.GONE);
            case STATE_FARM_ONE:
                showConfirmationBasics();
                farmTip.setText(R.string.signIn_foreman_farmOne);
                farmOneLook.setVisibility(View.VISIBLE);
                farmOneLook.setText();

        }
    }

    private void showConfirmationBasics() {
        phoneNumberField.setVisibility(View.GONE);
        SMSWarning.setVisibility(View.GONE);
        logInButt.setVisibility(View.GONE);
        failure.setVisibility(View.GONE);
        verificationTip.setVisibility(View.GONE);
        verificationField.setVisibility(View.GONE);
        verificationButts.setVisibility(View.GONE);
        phoneConfTip.setVisibility(View.VISIBLE);
        phoneConfLook.setVisibility(View.VISIBLE);
        phoneConfLook.setText(mAuth.getCurrentUser().getPhoneNumber());
        farmTip.setVisibility(View.VISIBLE);
        farmOkay.setVisibility(View.VISIBLE);
    }
}
