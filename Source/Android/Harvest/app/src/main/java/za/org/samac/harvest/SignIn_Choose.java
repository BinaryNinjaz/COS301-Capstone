package za.org.samac.harvest;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

import za.org.samac.harvest.util.Data;

public class SignIn_Choose extends AppCompatActivity {
    private ImageView farmerPic = null;
    private ImageView foremanPic =  null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin_choose);
        Data.newAccount();
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
