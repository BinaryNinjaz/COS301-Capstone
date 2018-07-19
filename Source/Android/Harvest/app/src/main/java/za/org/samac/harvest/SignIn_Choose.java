package za.org.samac.harvest;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class SignIn_Choose extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_choose);
    }

    public void choseFarmer(View v){
        Intent openFarmer = new Intent(SignIn_Choose.this, SignIn_Farmer.class);
        openFarmer.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openFarmer, 0);
    }

    public void choseForeman(View v){
        Intent openForeman = new Intent (SignIn_Choose.this, SignIn_Foreman.class);
        openForeman.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openForeman, 0);

    }

    public void choseNew(View v){
        Intent openRegistration = new Intent(SignIn_Choose.this, SignIn_SignUp.class);
        openRegistration.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(openRegistration, 0);
    }

    //Back acts as home
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }
}
