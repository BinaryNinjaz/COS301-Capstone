package za.org.samac.harvest;

import com.github.mikephil.charting.data.LineData;

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
import static za.org.samac.harvest.Analytics.WEEKLY;

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
            //Same Year

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
                    simpleDateFormat = new SimpleDateFormat("EEE");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("EEE");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day
            endCal.roll(Calendar.DAY_OF_MONTH, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("dd HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day & Month
            endCal.roll(Calendar.MONTH, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("MMM dd HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("MMM dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("MMM dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;

            }

            //Differing Day, Month, and Year
            endCal.roll(Calendar.YEAR, 1);

            fromAnalytics = analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM");
                    assertTrue(fromAnalytics.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
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
        The first thing that needs to be stated is that is that determineDates is correct (see above), and so FireBase is working with fixed dates.
        Date formatting also works correctly (see above).
        It also needs to be assumed that FireBase returns correctly there, and since FireBase returns dates, fixed dates will be used in the mocking.
        We'll also assume that different groups all perform the same, so only mock a single group.
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
        We'll _try_ and build it dynamically now.
        To keep it simple, it's two workers, each with four collections, each collection in a different period.
         */

        //Applicable only to accumTime
        //[interval][periods(key)]
        String accumTime[][] = {
          //hourly
                {"12:00", "13:00", "14:00", "15:00"}, //not exact, ':00' added to match fancy display formatting
          //daily
                {"Sunday", "Monday", "Tuesday", "Wednesday"},
          //weekly
                {"32", "33", "34", "35"}, //Number of weeks 06 August through 27 August
          //monthly
                {"January", "February", "March", "April"},
          //yearly
                {"2000", "2001", "2002", "2003"}
        };

        //Applicable not to accumTime
        //[interval][periods(key)]
        String standardTime[][] = {
          //hourly
                {"12:00", "13:00", "14:00", "15:00"},
          //daily
                {"01", "02", "03", "04"},
          //weekly
                {"05", "12", "19", "26"}, //Date of the 'first' sundays of each week in august 2018.
          //monthly
                {"Jan", "Feb", "Mar", "Apr"},
          //yearly
                {"2000", "2001", "2002", "2003"}
        };
        //A reminder that date formatting is correct (see above), so it's irrelevant how simple or complex these are.
        //Perhaps switch things up a bit at a later point.

        /*
        Now, both populateLabels() and getDoubleFromKey(key, subMin) need the mode and the intervals to be set, so lets do that.
         */

        final String modes[] = {
                Analytics.ACCUMULATION_ENTITY,
                Analytics.ACCUMULATION_NONE,
                Analytics.ACCUMULATION_TIME
        };

        //Iterate through modes
        for (String mode : modes) {
            //Iterate through intervals
            for (int ii = 0; ii < intervals.length; ii++) {
                System.out.println("Accum: " + mode + ", Interval: " + intervals[ii] + ".");
                analytics_graph.configureForLabelTesting(mode, intervals[ii], category); //Configure the object
                //Use the correct periods.
                String time[][];
                if (mode.equals(Analytics.ACCUMULATION_TIME)) {
                    time = accumTime;
                } else {
                    time = standardTime;
                }

                /*
                So now, populateLabels() needs all of the getDoubleFromKey(key, subMin = false) be run, so lets do that first.
                */

                //Set the date format first.
                //These Calendars HAVE to match the period arrays.
                Calendar startCal = Calendar.getInstance(), endCal = Calendar.getInstance();
                startCal.set(2018, 7, 7, startCal.getActualMinimum(Calendar.HOUR_OF_DAY), 0, 0);
                startCal.set(Calendar.MILLISECOND, startCal.getActualMinimum(Calendar.MILLISECOND));
                endCal.set(2018,7,7, endCal.getActualMaximum(Calendar.HOUR_OF_DAY), 59,59);
                endCal.set(Calendar.MILLISECOND, endCal.getActualMaximum(Calendar.MILLISECOND));
                switch (intervals[ii]){
                    case Analytics.HOURLY:
                        startCal.set(Calendar.HOUR_OF_DAY, 12);
                        endCal.set(Calendar.HOUR_OF_DAY, 15);
                        break;
                    case Analytics.DAILY:
                        if (mode.equals(Analytics.ACCUMULATION_TIME)) {
                            startCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                            endCal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                        }
                        else{
                            startCal.set(Calendar.DAY_OF_MONTH, 1);
                            endCal.set(Calendar.DAY_OF_MONTH, 4);
                        }
                        break;
                    case Analytics.WEEKLY:
//                        if(mode.equals(Analytics.ACCUMULATION_TIME)) {
                            startCal.set(Calendar.DATE, 5);
                            endCal.set(Calendar.DATE, 26);
//                        }
//                        else {
//                            startCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
//                            endCal.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
//                        }
                        break;
                    case Analytics.MONTHLY:
                        startCal.set(Calendar.MONTH, Calendar.JANUARY);
                        endCal.set(Calendar.MONTH,Calendar.APRIL);
                        break;
                    case Analytics.YEARLY:
                        startCal.set(Calendar.YEAR, 2000);
                        endCal.set(Calendar.YEAR, 2003);
                        break;
                }
                System.out.println(startCal.getTime().toString() + ", " + endCal.getTime().toString());
                analytics_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[ii]);
                System.out.print("Date Format Set. ");

                //Iterate through time periods
                for (int ti = 0; ti < 4; ti++) {
                    analytics_graph.getDoubleFromKey(time[ii][ti], false);
                }

                System.out.print("Min/Max Set (" + Analytics_Graph.minTime + " < " + Analytics_Graph.maxTime + "). ");

                /*
                Next up, let's check those labels.
                AccumTime is easy, it's 0, 1, 2, 3.
                StandardTime is more complicated, so for now, let's just check that 0 < 1 < 2 < 3
                 It will be easier to check this is correct when the labels come up correct.
                So, for both, check 0 < 1 < 2 < 3.
                 */

                double results[] = new double[4];
                for (int ti = 0; ti < 4; ti++) {
                    results[ti] = analytics_graph.getDoubleFromKey(time[ii][ti], true);
                }
                for (int i = 0; i < 3; i++) {
                    assertTrue(results[i] < results[i + 1]);
                }

                System.out.print("Keys To Doubles Successful. ");

                /*
                Now check the labels.
                 */
                analytics_graph.populateLabels();
                for (int ti = 0; ti < 4; ti++) {
                    if (mode.equals(Analytics.ACCUMULATION_TIME)) {
                        if (intervals[ii].equals(Analytics.WEEKLY)) {
                            //This setting here takes week numbers from FireBase, but returns the date of the 'first' sunday of the week, so make the conversion.
                            String[] tokens = analytics_graph.getLabel(ti, null).split("/");
                            Calendar cal = Calendar.getInstance();
                            cal.set(2018, Integer.parseInt(tokens[1]), Integer.parseInt(tokens[0]));
                            assertTrue(cal.get(Calendar.WEEK_OF_YEAR) + 1 == Integer.parseInt(time[ii][ti]));
                        } else {
                            assertTrue(analytics_graph.getLabel(ti, null).equals(time[ii][ti]));
                        }
                    }
                    else {
                        double send = ti * Math.floor((Analytics_Graph.maxTime - Analytics_Graph.minTime) / 4);
                        String expect = time[ii][ti];
                        String get = analytics_graph.getLabel((float) send, null);
                        assertTrue(get.equals(expect));
                    }
                }
                System.out.println("Labels Gotten Successfully.");
                System.out.println("Passed\n");
            }
        }
    }
}
