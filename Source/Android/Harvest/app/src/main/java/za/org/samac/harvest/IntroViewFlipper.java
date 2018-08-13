package za.org.samac.harvest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class IntroViewFlipper extends Activity {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_view_flipper);
        mContext = this;
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.view_flipper);


        skipOne = findViewById(R.id.skipOne);
        skipOne.setMovementMethod(LinkMovementMethod.getInstance());
        skipOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroViewFlipper.this, SignIn_Choose.class);
                startActivity(intent);
                finish();//kill current Activity
            }
        });

        skipTwo = findViewById(R.id.skipTwo);
        skipTwo.setMovementMethod(LinkMovementMethod.getInstance());
        skipTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroViewFlipper.this, SignIn_Choose.class);
                startActivity(intent);
                finish();//kill current Activity
            }
        });

        skipThree = findViewById(R.id.skipThree);
        skipThree.setMovementMethod(LinkMovementMethod.getInstance());
        skipThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroViewFlipper.this, SignIn_Choose.class);
                startActivity(intent);
                finish();//kill current Activity
            }
        });

        done = findViewById(R.id.done);
        done.setMovementMethod(LinkMovementMethod.getInstance());
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(IntroViewFlipper.this, SignIn_Choose.class);
                startActivity(intent);
                finish();//kill current Activity
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

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_in));
                    //mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.left_out));
                    mViewFlipper.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    //mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.right_in));
                    //mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext,R.anim.right_out));
                    mViewFlipper.showPrevious();
                    return true;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return false;
        }
    }
}
