package za.org.samac.harvest.util;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import za.org.samac.harvest.R;

import me.relex.circleindicator.CircleIndicator;
import za.org.samac.harvest.SignIn_Choose;
import za.org.samac.harvest.SplashScreenActivity;
import za.org.samac.harvest.adapter.IntroPagerAdapter;

public class IntroActivity extends AppCompatActivity {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] XMEN= {R.drawable.harvestintro,R.drawable.harvestmanage,R.drawable.harvesttrack,R.drawable.harvestanalyse};
    private ArrayList<Integer> XMENArray = new ArrayList<Integer>();
    Handler handler = new Handler();
    int delay = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        init();
    }
    private void init() {
        for(int i=0;i<XMEN.length;i++)
            XMENArray.add(XMEN[i]);

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(new IntroPagerAdapter(IntroActivity.this,XMENArray));
        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                if (currentPage == XMEN.length) {
                    //currentPage = 0;
                    Intent intent = new Intent(IntroActivity.this, SignIn_Choose.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                mPager.setCurrentItem(currentPage++, true);

                handler.postDelayed(this, delay);
            }
        }, delay);

        /*final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == XMEN.length) {
                    //currentPage = 0;
                    Intent intent = new Intent(IntroActivity.this, SignIn_Choose.class);
                    startActivity(intent);
                    finish();
                    return;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
                if (currentPage == XMEN.length) {
                    return;
                }
            }
        }, 2500, 2500);*/
    }
}