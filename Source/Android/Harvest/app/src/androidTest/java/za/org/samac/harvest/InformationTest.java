package za.org.samac.harvest;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import za.org.samac.harvest.util.Orchard;
import za.org.samac.harvest.util.Worker;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class InformationTest {

    class Farm0{
        final String
                  name     = "FarmTestNameOne"
                , company  = "FarmTestCompanyOne"
                , email    = "FarmTestEmailOne"
                , phone    = "000 000 000 0000"
                , province = "FarmTestProvinceOne"
                , town     = "FarmTestTownOne"
                , further  = "FarmTestFurtherOne"
                ;
    }

    class Orchard0{
        final String
                  name  = "OrchardTestNameOne"
                , crop  = "OrchardTestCropOne"
                , mass  = "000"
                , irrig = "OrchardTestIrrigationOne"
                , cult0 = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla leo nisi, congue sit amet volutpat in, accumsan non tellus. Integer vestibulum tellus sit amet tempor pellentesque. Nam rutrum odio nibh, sit amet iaculis enim sollicitudin et. Aliquam non egestas urna. Cras ornare turpis ex, in tristique magna cursus non. Sed metus est, cursus id dignissim nec, porttitor id nibh. Praesent ullamcorper tellus euismod dui pellentesque ultricies."
                , cult1 = "Morbi ex est, ornare vitae lacus ut, convallis lobortis magna. Cras in congue sem. Praesent molestie mauris eget arcu venenatis, at facilisis velit lacinia. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Aenean ac risus tortor. Nam auctor enim a metus egestas malesuada. Vivamus imperdiet quam at odio congue, sed varius risus posuere. Aliquam erat volutpat. Sed egestas orci id nulla commodo, sit amet tempor nulla placerat. Maecenas eget dolor eget arcu euismod suscipit vitae in leo. Duis porttitor porta arcu, ac venenatis risus."
                , cult2 = "Donec dictum in diam ac pharetra. Sed porttitor magna ac mauris malesuada efficitur. Mauris vel felis ut mauris commodo pretium ultricies id nisi. Suspendisse potenti. Mauris laoreet dolor nec massa ultrices, non lacinia nunc feugiat. Sed ac purus convallis, commodo lacus quis, consectetur mauris. Aliquam scelerisque ut orci tempus congue. Fusce tincidunt nulla velit, ac gravida lectus ullamcorper vitae. Nulla id lorem sit amet elit gravida faucibus. Phasellus ante felis, varius id enim ut, interdum efficitur risus."
                , rowSp = "000"
                , treeS = "000"
                , furth = "Duis at risus in tortor vehicula tincidunt vitae et turpis. Nunc in tincidunt elit, quis consequat urna. Vestibulum ultrices varius purus, eu rutrum turpis malesuada vitae. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer quis lobortis est, eu efficitur diam. Aliquam sed tellus ac mauris euismod porttitor id sit amet erat. Etiam nisl diam, mollis at accumsan et, suscipit at odio. In feugiat nulla nunc, sed cursus nulla ullamcorper sed. Donec ac finibus dui, non sodales dui."
                ;
    }

    class Worker0{
        final String
                  fname   = "WorkerTestNameOne"
                , surname = "WorkerTestSurnameOne"
                , ID      = "WorkerTestIDOne"
                , phone   = "000 000 000 0000"
                , further = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nulla leo nisi, congue sit amet volutpat in, accumsan non tellus. Integer vestibulum tellus sit amet tempor pellentesque. Nam rutrum odio nibh, sit amet iaculis enim sollicitudin et. Aliquam non egestas urna. Cras ornare turpis ex, in tristique magna cursus non. Sed metus est, cursus id dignissim nec, porttitor id nibh. Praesent ullamcorper tellus euismod dui pellentesque ultricies"
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

        onView(withId(R.id.info_farm_butt_save)).perform(click());

        onView(withText(farm.name)).perform(click());

        onView(withId(R.id.info_farm_butt_del)).perform(click());
        onView(withText("Delete")).perform(click());
    }

    @Test
    public void orchards(){
        Orchard0 orchard = new Orchard0();

        onView(withText("Orchards")).perform(click());
        onView(withId(R.id.addSomething)).perform(click());
        onView(withText("Orchard Name:")).check(matches(isDisplayed()));
        onView(withId(R.id.info_orch_name_edit)).check(matches(isDisplayed()));

        onView(withId(R.id.info_orch_name_edit)).perform(typeText(orchard.name));

        onView(withId(R.id.info_orch_crop_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_crop_edit)).perform(typeText(orchard.crop));

        onView(withId(R.id.info_orch_mass_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_mass_edit)).perform(typeText(orchard.mass));

        onView(withId(R.id.info_orch_irig_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_irig_edit)).perform(typeText(orchard.irrig));

        onView(withId(R.id.info_orch_cultivars_add_text)).perform(scrollTo());
        onView(withId(R.id.info_orch_cultivars_add_text)).perform(typeText(orchard.cult0));
        onView(withId(R.id.info_orch_cultivars_add_butt)).perform(click());

        onView(withId(R.id.info_orch_cultivars_add_text)).perform(scrollTo());
        onView(withId(R.id.info_orch_cultivars_add_text)).perform(typeText(orchard.cult1));
        onView(withId(R.id.info_orch_cultivars_add_butt)).perform(click());
//
        onView(withId(R.id.info_orch_cultivars_add_text)).perform(scrollTo());
        onView(withId(R.id.info_orch_cultivars_add_text)).perform(typeText(orchard.cult2));
        onView(withId(R.id.info_orch_cultivars_add_butt)).perform(click());

        onView(withId(R.id.info_orch_row_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_row_edit)).perform(typeText(orchard.rowSp));

        onView(withId(R.id.info_orch_tree_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_tree_edit)).perform(typeText(orchard.treeS));

        onView(withId(R.id.info_orch_further_edit)).perform(scrollTo());
        onView(withId(R.id.info_orch_further_edit)).perform(typeText(orchard.furth));

        onView(withId(R.id.info_orch_butt_save)).perform(click());

        onView(withText(orchard.name)).perform(click());

        onView(withId(R.id.info_orch_butt_del)).perform(click());
        onView(withText("Delete")).perform(click());
    }

    @Test
    public void workers(){
        Worker0 worker = new Worker0();

        onView(withText("Workers")).perform(click());
        onView(withId(R.id.addSomething)).perform(click());
        onView(withText("Worker Name:")).check(matches(isDisplayed()));
        onView(withId(R.id.info_work_fName_edit)).check(matches(isDisplayed()));

        onView(withId(R.id.info_work_fName_edit)).perform(typeText(worker.fname));

        onView(withId(R.id.info_work_sName_edit)).perform(scrollTo());
        onView(withId(R.id.info_work_sName_edit)).perform(typeText(worker.surname));

        onView(withId(R.id.info_work_id_edit)).perform(scrollTo());
        onView(withId(R.id.info_work_id_edit)).perform(typeText(worker.ID));

        onView(withId(R.id.info_work_phone_edit)).perform(scrollTo());
        onView(withId(R.id.info_work_phone_edit)).perform(typeText(worker.phone));

        onView(withId(R.id.info_work_further_edit)).perform(scrollTo());
        onView(withId(R.id.info_work_further_edit)).perform(typeText(worker.further));

        onView(withId(R.id.info_work_butt_save)).perform(click());

        onView(withText(worker.surname + ", " + worker.fname)).perform(click());

        onView(withId(R.id.info_work_butt_del)).perform(click());
        onView(withText("Delete")).perform(click());
    }
}
