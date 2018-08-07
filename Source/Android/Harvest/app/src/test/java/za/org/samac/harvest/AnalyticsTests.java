package za.org.samac.harvest;

import org.junit.Test;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import za.org.samac.harvest.util.Category;
import za.org.samac.harvest.util.Data;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import static za.org.samac.harvest.Analytics.THOUSAND;

public class AnalyticsTests {

    private final String intervals[] = {
            Analytics.HOURLY,
            Analytics.DAILY,
            Analytics.WEEKLY,
            Analytics.MONTHLY,
            Analytics.YEARLY
    };

    @SuppressWarnings("ConstantConditions")
    @Test
    public void determineDatesTests(){
        System.out.println("Since the only way to programmatically determine the result is correct is with the same algorithm, it is up to the pathetic human to verify the results, with the use of some logic thingy...");

        System.out.println("\n" + Analytics.TODAY);
        printDateBundle(Analytics.determineDates(Analytics.TODAY));
        System.out.println("\n" + Analytics.YESTERDAY);
        printDateBundle(Analytics.determineDates(Analytics.YESTERDAY));

        System.out.println("\n" + Analytics.THIS_WEEK);
        printDateBundle(Analytics.determineDates(Analytics.THIS_WEEK));
        System.out.println("\n" + Analytics.LAST_WEEK);
        printDateBundle(Analytics.determineDates(Analytics.LAST_WEEK));

        System.out.println("\n" + Analytics.THIS_MONTH);
        printDateBundle(Analytics.determineDates(Analytics.THIS_MONTH));
        System.out.println("\n" + Analytics.LAST_MONTH);
        printDateBundle(Analytics.determineDates(Analytics.LAST_MONTH));

        System.out.println("\n" + Analytics.THIS_YEAR);
        printDateBundle(Analytics.determineDates(Analytics.THIS_YEAR));
        System.out.println("\n" + Analytics.LAST_YEAR);
        printDateBundle(Analytics.determineDates(Analytics.LAST_YEAR));

        assert true;
    }
    
    private void printDateBundle(Analytics.DateBundle dateBundle){
        Date date = new Date((long) (dateBundle.startDate * THOUSAND));
        System.out.println("Start: " + date.toString());
        
        Date start = new Date((long) (dateBundle.endDate * THOUSAND));
        System.out.println("End: " + start.toString());
    }

    @Test
    public void dateFormatterTests(){
        /*
        The rules are:
         a base format(fmt) of YYYY MM DD, truncated from left to right where a period matches, when they do not match, truncation stops.
         depending on the intervals:
          hourly
           fmt + ' HH:mm'
          daily
           fmt == '' ? 'ddd' : fmt
          weekly
           fmt == '' ? 'ddd' : fmt
          monthly
           fmtYear + 'MMM'
            where fmtYear is either '' or YYYY(if differs)
          yearly
           YYYY
         */

        Analytics_Graph analytics_graph = new Analytics_Graph();

        for (int i = 0; i < intervals.length; i++){
            System.out.print(intervals[i]);

            //Same Year
            System.out.print(", Same All");

            SimpleDateFormat simpleDateFormat;
            Calendar startCal = Calendar.getInstance();
            startCal.set(2000, 2, 2);
            Calendar endCal = Calendar.getInstance();
            endCal.set(2000, 2, 2);

            SimpleDateFormat fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("ddd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("ddd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day
            System.out.print(", Day");
            endCal.roll(Calendar.DAY_OF_MONTH, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("DD HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day & Month
            System.out.print(", Month");
            endCal.roll(Calendar.MONTH, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("MMM DD HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("MMM DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("MMM DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day, Month, and Year
            System.out.print(", Year");
            endCal.roll(Calendar.YEAR, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("YYYY MMM DD HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("YYYY MMM DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("YYYY MMM DD");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("YYYY MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("YYYY");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }
        }
    }

    @Test
    public void labelTests(){
        /*
        So,

        We need to test that all of the labels are coming back correctly, these means testing the following functions:
         populateLabels(), and getDoubleFromKey(key, subMin).
        The first thing that needs to be stated is that is that determineDates is correct (see above).
        Date formatting also works correctly (see above).
        It also needs to be assumed that FireBase returns correctly there, and since FireBase returns dates, fixed dates will be used in the mocking.
        We'll also assume that different groups all perform the same, so only mock a single group.
        Also, the dates given are insignificant, since that's all performed before, and by, the FireBase function.
         */

        /*
        The very first thing we need to do, is, obviously, create the object, then we'll need to setup the Data class for mocking.
         */

        final Category category = Category.WORKER;

        Analytics_Graph analytics_graph = new Analytics_Graph();

        Data data = new Data(true); // Tell it not to do any FireBase stuff.
        List<String[]> mocks = new ArrayList<>();
        mocks.add(new String[]{"0", "fName0", "sName0"});
        mocks.add(new String[]{"1", "fName1", "sName1"});
        data.mockWith(mocks, Category.WORKER);
        //Data has two workers now, with ids 0 and 1.

        /*
        Now, constructing a response will be tough, since it needs to be different for each of the 15 situations.
        We'll build it on the fly.
         */

        //All have an average, and it

        /*
        Now, both populateLabels() and getDoubleFromKey(key, subMin) need the mode and the intervals to be set, so lets do that.
         */

        final String modes[] = {
                Analytics.ACCUMULATION_ENTITY,
                Analytics.ACCUMULATION_NONE,
                Analytics.ACCUMULATION_TIME
        };

        for (int mi = 0; mi < modes.length; mi++){
            for (int ii = 0; ii < intervals.length; ii++){
                analytics_graph.configureForLabelTesting(modes[mi], intervals[ii], category);



                /*
                So now, populateLabels() needs all of the getDoubleFromKey(key, subMin) be run, so lets do that first.
                 */


            }
        }
    }
}
