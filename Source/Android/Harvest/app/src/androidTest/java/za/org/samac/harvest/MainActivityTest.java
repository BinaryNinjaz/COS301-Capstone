package za.org.samac.harvest;


import android.os.SystemClock;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<SplashScreenActivity> mActivityTestRule = new ActivityTestRule<>(SplashScreenActivity.class);

    private String startButton = "Start";

    @Test
    public void mainActivityTest() {
        SystemClock.sleep(5000);

        Espresso.onView(withId(R.id.button_start))
                .check(matches(withText(R.string.start)));

        Espresso.onView(withId(R.id.actionYieldTracker))
                .check(matches(withText(R.string.bottomYieldTracker)));

        Espresso.onView(withId(R.id.actionInformation))
                .perform(ViewActions.click());

        Espresso.onView(withId(R.id.actionInformation))
                .check(matches(withText(R.string.titleInformation)));

        Espresso.onView(withId(R.id.actionSession))
                .perform(ViewActions.click());

        SystemClock.sleep(5000);

        Espresso.onView(withId(R.id.actionSession))
                .check(matches(withText(R.string.title_activity_sessions)));

        Espresso.onView(withId(R.id.actionStats))
                .perform(ViewActions.click());

        Espresso.onView(withId(R.id.actionStats))
                .check(matches(withText(R.string.title_stats)));
    }
}
