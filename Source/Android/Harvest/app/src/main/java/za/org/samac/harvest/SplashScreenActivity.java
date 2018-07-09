package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import za.org.samac.harvest.util.AppUtil;

public class SplashScreenActivity extends AppCompatActivity {

    private TextView tvAppName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        tvAppName = findViewById(R.id.tvAppName);
        tvAppName.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if(AppUtil.isUserSignedIn(getApplicationContext())) {
                    Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(SplashScreenActivity.this, SignIn_Farmer.class);
                    startActivity(intent);
                    finish();
                }

            }
        }, 2500);
    }

}
