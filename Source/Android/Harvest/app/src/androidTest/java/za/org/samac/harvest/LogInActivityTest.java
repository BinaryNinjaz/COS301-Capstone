package za.org.samac.harvest;


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
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
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.runner.RunWith;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
//import android.support.test.espresso.assertion.ViewAssertions.matches;
import android.support.test.espresso.matcher.ViewMatchers.*;

import java.util.concurrent.TimeUnit;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LogInActivityTest {

    @Rule
    //@JvmField
    public ActivityTestRule<SplashScreenActivity> rule  = new ActivityTestRule<>(SplashScreenActivity.class);

    private String registeredUnconfirmedEmail = "peter@parker.park";
    private String registeredUnconfirmedEmailPassword = "parker";

    private String fakeEmail = "williams@g.com";
    private String fakeEmailPassword = "000000";

    private String acceptableEmail = "u15096794@tuks.co.za";
    private String acceptableEmailPassword = "johnojo";

    private String startButton = "Start";

    private String name = "Test";
    private String surname = "Test";
    private String email = "test@gmail.com";
    private String password = "test";
    private String confirmPassword = "test";

    @Test
    public void  login_success(){
        Log.e("@Test","Performing login success test");
        //splash screen ****************************************************************************
        Espresso.onView(withId(R.id.tvAppName))
                .check(matches(withText(R.string.app_name)));

        SystemClock.sleep(3000);//wait for splash

        //choose sign in ****************************************************************************
        Espresso.onView((withId(R.id.farmerPic)))
                .perform(ViewActions.click());

        //log in with registeredUnconfirmedEmail ****************************************************************************
        Espresso.onView((withId(R.id.edtEmail)))
                .perform(ViewActions.typeText(registeredUnconfirmedEmail));

        Espresso.onView(withId(R.id.edtPassword))
                .perform(ViewActions.typeText(registeredUnconfirmedEmailPassword), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        //log in with fakeEmail ****************************************************************************
        Espresso.onView((withId(R.id.edtEmail)))
                .perform(clearText())
                .perform(ViewActions.typeText(fakeEmail));

        Espresso.onView(withId(R.id.edtPassword))
                .perform(clearText())
                .perform(ViewActions.typeText(fakeEmailPassword), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        //log in with acceptedEmail ****************************************************************************
        Espresso.onView((withId(R.id.edtEmail)))
                .perform(clearText())
                .perform(ViewActions.typeText(acceptableEmail));

        Espresso.onView(withId(R.id.edtPassword))
                .perform(clearText())
                .perform(ViewActions.typeText(acceptableEmailPassword), ViewActions.closeSoftKeyboard());

        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        SystemClock.sleep(5000);//wait for Firebase verification

        Espresso.onView(withId(R.id.button_start))
                .check(matches(withText(startButton)));

        SystemClock.sleep(3000);//wait for GPS and Orchards to be retrieved

        //logout ****************************************************************************
        /*Espresso.onView(withId(R.id.menu))
                .perform(ViewActions.click());//click on menu first

        Espresso.onView(withId(R.id.logout))
                .perform(ViewActions.click());

        ViewActions.closeSoftKeyboard();

        Espresso.onView(withId(R.id.btnLogin))
                .check(matches(withText(R.string.login)));

        //go to sign up ****************************************************************************
        Espresso.onView(withId(R.id.btnSignUp))
                .perform(ViewActions.click());

        ViewActions.closeSoftKeyboard();

        //go back to log in ****************************************************************************
        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        ViewActions.closeSoftKeyboard();

        //sign up ****************************************************************************
        Espresso.onView(withId(R.id.btnSignUp))
                .perform(ViewActions.click());

        ViewActions.closeSoftKeyboard();

        Espresso.onView((withId(R.id.edtFirstName)))
                .perform(ViewActions.typeText(name));

        Espresso.onView((withId(R.id.edtSurname)))
                .perform(ViewActions.typeText(surname));

        Espresso.onView((withId(R.id.edtEmail)))
                .perform(ViewActions.typeText(email));

        Espresso.onView((withId(R.id.edtPassword)))
                .perform(ViewActions.typeText(password));

        Espresso.onView((withId(R.id.edtConfirmPassword)))
                .perform(ViewActions.typeText(confirmPassword));

        Espresso.onView(withId(R.id.btnSignUp))
                .perform(ViewActions.click());

        SystemClock.sleep(5000);//wait for Firebase verification

        Espresso.onView(withId(R.id.button_start))
                .check(matches(withText(startButton)));*/
    }

    /*@Test
    public void  login_failure(){
        Log.e("@Test","Performing login failure test");
        Espresso.onView((withId(R.id.edtEmail)))
                .perform(ViewActions.typeText(username_tobe_typed));

        Espresso.onView(withId(R.id.edtPassword))
                .perform(ViewActions.typeText(wrong_password));

        Espresso.onView(withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(withId(R.id.btnLogin))
                .check(matches(withText(R.string.title_activity_login)));
    }*/

    /*@Rule
    public ActivityTestRule<SplashScreenActivity> mActivityTestRule = new ActivityTestRule<>(SplashScreenActivity.class);

    @Test
    public void logInActivityTest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.edtEmail),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("a@gmail.com"), closeSoftKeyboard());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(3591707);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.edtPassword),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.design.widget.TextInputLayout")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("a"), closeSoftKeyboard());

        pressBack();

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btnLogin), withText("Log In"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                2)));
        appCompatButton.perform(scrollTo(), click());

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }*/
}
