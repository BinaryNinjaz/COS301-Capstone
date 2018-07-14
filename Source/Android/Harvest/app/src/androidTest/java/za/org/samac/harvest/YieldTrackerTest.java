package za.org.samac.harvest;

import android.os.SystemClock;
import android.support.test.espresso.DataInteraction;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class YieldTrackerTest {

    @Rule
    //@JvmField
    public ActivityTestRule<SplashScreenActivity> rule  = new ActivityTestRule<>(SplashScreenActivity.class);

    private String username_tobe_typed = "a@gmail.com";
    private String correct_password = "a";
    private String startButton = "Start";

    @Before
    public void  login_success() {
        Log.e("@Test", "Performing login success test");
        //splash screen ****************************************************************************
        Espresso.onView(withId(R.id.tvAppName))
                .check(matches(withText(R.string.app_name)));

        SystemClock.sleep(3000);//wait for splash

        //log in ****************************************************************************
        Espresso.onView((withId(R.id.edtEmail)))
                .perform(ViewActions.typeText(username_tobe_typed));

        Espresso.onView(withId(R.id.edtPassword))
                .perform(ViewActions.typeText(correct_password), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        SystemClock.sleep(5000);//wait for Firebase verification

        Espresso.onView(withId(R.id.button_start))
                .check(matches(withText(startButton)));
    }

    @Test
    public void yieldTracker() {
        Espresso.onView(withId(R.id.button_start))
                .check(matches(withText(startButton)));

        SystemClock.sleep(2000);//wait for GPS verification

        //0 Bags Collected Session *****************************************************************
        Espresso.onView(withId(R.id.button_start))
                .perform(ViewActions.click());//start session

        SystemClock.sleep(2000);//wait for GPS verification

        Espresso.onView(withId(R.id.button_start))
                .perform(ViewActions.click());//stop session

        SystemClock.sleep(2000);//wait like a human

        Espresso.onView(withText("OK"))
                .inRoot(isDialog()) // <---
                .check(matches(withText("OK")))
                .perform(ViewActions.click());

        SystemClock.sleep(2000);//wait like a human

        Espresso.onView(withText("No"))
                .inRoot(isDialog()) // <---
                .check(matches(withText("No")))
                .perform(ViewActions.click());

        /*SystemClock.sleep(2000);//wait like a human

        //Collect Bags Session *********************************************************************
        for (int i = 0; i<100; i++) {
            SystemClock.sleep(250);
            Espresso.onView(anyOf(first(withId(R.id.btnPlus))))
                    .perform(ViewActions.click());
        }

        Espresso.onView(withId(R.id.button_start))
                .perform(ViewActions.click());//stop session

        Espresso.onView(withText("Yes"))
                .inRoot(isDialog()) // <---
                .check(matches(withText("Yes")))
                .perform(ViewActions.click());*/
    }

    private <T> Matcher<T> first(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }

                return false;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("should return first matching item");
            }
        };
    }
}
