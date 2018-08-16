package za.org.samac.harvest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class ViewFlipperActivity extends Activity {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private ViewFlipper mViewFlipper;
    private Context mContext;
    private final GestureDetector detector = new GestureDetector(new SwipeGestureDetector());
    private TextView skipOne;
    private TextView skipTwo;
    private TextView skipThree;
    private TextView skipFour;
    private TextView done;
    private Integer slideCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_flipper);
        mContext = this;
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.view_flipper);

        //If this is called as a redo, then we don't want to go back to the clicker, so the HelpPreferenceFragment will signal with this.
        Bundle extras = getIntent().getExtras();
        final boolean noNewActivity;
        noNewActivity = extras != null && extras.getBoolean(SettingsActivity.HelpPreferenceFragment.KEY_NONEWACTIVITY, false);


        skipOne = findViewById(R.id.skipOne);
        skipOne.setMovementMethod(LinkMovementMethod.getInstance());
        skipOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endThis(!noNewActivity);
            }
        });

        skipTwo = findViewById(R.id.skipTwo);
        skipTwo.setMovementMethod(LinkMovementMethod.getInstance());
        skipTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endThis(!noNewActivity);
            }
        });

        skipThree = findViewById(R.id.skipThree);
        skipThree.setMovementMethod(LinkMovementMethod.getInstance());
        skipThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endThis(!noNewActivity);
            }
        });

        skipFour = findViewById(R.id.skipFour);
        skipFour.setMovementMethod(LinkMovementMethod.getInstance());
        skipFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endThis(!noNewActivity);
            }
        });

        done = findViewById(R.id.done);
        done.setMovementMethod(LinkMovementMethod.getInstance());
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endThis(!noNewActivity);
            }
        });

        mViewFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(Intent.ACTION_MAIN);
        setIntent.addCategory(Intent.CATEGORY_HOME);
        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(setIntent);
    }

    private void endThis(boolean goToStart){
        if (goToStart) {
            Intent intent = new Intent(ViewFlipperActivity.this, InformationActivity.class);
            startActivity(intent);
        }
        finish();
    }

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (slideCount == 4) {
                        slideCount = 0;
                        Intent intent = new Intent(ViewFlipperActivity.this, InformationActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_in));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_out));
                        mViewFlipper.showNext();
                        slideCount++;
                    }

                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (slideCount == 0) {

                    } else {
                        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_in));
                        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.right_out));
                        mViewFlipper.showPrevious();
                        slideCount--;
                    }

                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
