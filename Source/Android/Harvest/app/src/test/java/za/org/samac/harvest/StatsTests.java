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

import static za.org.samac.harvest.Stats.THOUSAND;
import static za.org.samac.harvest.Stats.WEEKLY;

public class StatsTests {

    private final String intervals[] = {
            Stats.HOURLY,
            Stats.DAILY,
            Stats.WEEKLY,
            Stats.MONTHLY,
            Stats.YEARLY
    };

    @SuppressWarnings("ConstantConditions")
    @Test
    public void determineDatesTests(){
        System.out.println("Since the only way to programmatically determine the result is correct is with the same algorithm, it is up to the pathetic human to verify the results, with the use of some logic thingy...");

        Stats stats = new Stats();

        System.out.println("\n" + Stats.TODAY);
        printDateBundle(stats.determineDates(Stats.TODAY));
        System.out.println("\n" + Stats.YESTERDAY);
        printDateBundle(stats.determineDates(Stats.YESTERDAY));

        System.out.println("\n" + Stats.THIS_WEEK);
        printDateBundle(stats.determineDates(Stats.THIS_WEEK));
        System.out.println("\n" + Stats.LAST_WEEK);
        printDateBundle(stats.determineDates(Stats.LAST_WEEK));

        System.out.println("\n" + Stats.THIS_MONTH);
        printDateBundle(stats.determineDates(Stats.THIS_MONTH));
        System.out.println("\n" + Stats.LAST_MONTH);
        printDateBundle(stats.determineDates(Stats.LAST_MONTH));

        System.out.println("\n" + Stats.THIS_YEAR);
        printDateBundle(stats.determineDates(Stats.THIS_YEAR));
        System.out.println("\n" + Stats.LAST_YEAR);
        printDateBundle(stats.determineDates(Stats.LAST_YEAR));

        assert true;
    }

    private void printDateBundle(Stats.DateBundle dateBundle){
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

        Stats_Graph stats_graph = new Stats_Graph();

        for (int i = 0; i < intervals.length; i++){
            //Same Year

            SimpleDateFormat simpleDateFormat;
            Calendar startCal = Calendar.getInstance();
            startCal.set(2000, 2, 2);
            Calendar endCal = Calendar.getInstance();
            endCal.set(2000, 2, 2);

            SimpleDateFormat fromStats = stats_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("HH:mm");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("EEE");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("EEE");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;

            }

            //Differing Day
            endCal.roll(Calendar.DAY_OF_MONTH, 1);

            fromStats = stats_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("dd HH:mm");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;

            }

            //Differing Day & Month
            endCal.roll(Calendar.MONTH, 1);

            fromStats = stats_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("MMM dd HH:mm");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("MMM dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("MMM dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMM");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;

            }

            //Differing Day, Month, and Year
            endCal.roll(Calendar.YEAR, 1);

            fromStats = stats_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i]);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd HH:mm");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM dd");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("yyyy MMM");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;

            }

            //Accumulation Time
            fromStats = stats_graph.testDateFormatWith(startCal.getTimeInMillis() / THOUSAND, endCal.getTimeInMillis() / THOUSAND, intervals[i], Stats.ACCUMULATION_TIME);
            switch (i){
                case 0:
                    //hourly
                    simpleDateFormat = new SimpleDateFormat("HH");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 1:
                    //daily
                    simpleDateFormat = new SimpleDateFormat("EEEE");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 2:
                    //weekly
                    simpleDateFormat = new SimpleDateFormat("w");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 3:
                    //monthly
                    simpleDateFormat = new SimpleDateFormat("MMMM");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;
                case 4:
                    //yearly
                    simpleDateFormat = new SimpleDateFormat("yyyy");
                    assertTrue(fromStats.equals(simpleDateFormat));
                    break;

            }
        }
    }
}
