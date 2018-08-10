package za.org.samac.harvest;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.Executor;
import java.util.regex.Pattern;

import za.org.samac.harvest.util.AppUtil;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class InputValidationTest {

    @Mock
    private FirebaseAuth mAuth;

    @Mock
    private FirebaseUser currentUser;

    @Mock
    private Context context;

    String incorrectEmail = "ramsey@gmail.com";
    String incorrectPassword = "ramsey";
    String correctFirstName = "Aaron";
    String correctSurname = "Ramsey";

    @Test
    public void loginValidator_CorrectFields_ReturnsTrue() {
        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();


        mAuth.signInWithEmailAndPassword(incorrectEmail, incorrectPassword)
                .addOnCompleteListener((Executor) this, new OnCompleteListener<AuthResult>() {
                    public static final String TAG = "Message";

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

//                            assertThat(SignIn_Farmer.validateForm("a@gmail.com", "a"), is(true));

                        } else {
                            Log.d(TAG, "signInWithEmail:fail");
//                            assertThat(SignIn_Farmer.validateForm("a@gmail.com", "a"), is(false));
                        }

                        // ...
                    }
                });
    }
}