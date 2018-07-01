package za.org.samac.harvest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class InformationTest {

    class Farm0{
        protected final String
                  name     = "FarmTestNameOne"
                , company  = "FarmTestCompanyOne"
                , email    = "FarmTestEmailOne"
                , phone    = "000 000 000 0000"
                , province = "FarmTestProvinceOne"
                , town     = "FarmTestTownOne"
                , further  = "FarmTestFurtherOne"
                ;
    }

    @Rule
    public ActivityTestRule<InformationActivity> rule = new ActivityTestRule<>(InformationActivity.class);

    @Test
    public void information(){
        onView(withText("Farms")).check(matches(isDisplayed()));
        onView(withText("Workers")).check(matches(isDisplayed()));
        onView(withText("Orchards")).check(matches(isDisplayed()));
    }

    @Test
    public void farms(){
        Farm0 farm = new Farm0();

        onView(withText("Farms")).perform(click());
        onView(withId(R.id.addSomething)).perform(click());
        onView(withText("Farm Name:")).check(matches(isDisplayed()));
        onView(withId(R.id.info_farm_name_edit)).check(matches(isDisplayed()));

        onView(withId(R.id.info_farm_name_edit)).perform(typeText(farm.name));

        onView(withId(R.id.info_farm_company_desc)).perform(scrollTo());
        onView(withId(R.id.info_farm_company_edit)).perform(typeText(farm.company));

        onView(withId(R.id.info_farm_email_edit)).perform(scrollTo());
        onView(withId(R.id.info_farm_email_edit)).perform(typeText(farm.email));

        onView(withId(R.id.info_farm_phone_edit)).perform(scrollTo());
        onView(withId(R.id.info_farm_phone_edit)).perform(typeText(farm.phone));

        onView(withId(R.id.info_farm_province_edit)).perform(scrollTo());
        onView(withId(R.id.info_farm_province_edit)).perform(typeText(farm.province));

        onView(withId(R.id.info_farm_town_edit)).perform(scrollTo());
        onView(withId(R.id.info_farm_town_edit)).perform(typeText(farm.town));

        onView(withId(R.id.info_farm_further_edit)).perform(scrollTo());
        onView(withId(R.id.info_farm_further_edit)).perform(typeText(farm.further));


        onView(withText(farm.name)).perform(click());
        onView(withId(R.id.info_farm_further_edit)).perform(click());

        onView(withId(R.id.info_farm_name_edit)).perform(typeText(farm.name + " more stuff"));
        onView(withId(R.id.info_farm_butt_save)).perform(click());
    }

    @Test
    public void orchards(){

    }

    @Test
    public void workers(){

    }

    @Test
    public void togetherness(){

    }

    @Test
    public void cleanUp(){

    }
}
