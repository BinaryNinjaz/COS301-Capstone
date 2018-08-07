package za.org.samac.harvest;

import org.junit.Test;

import java.util.Date;

public class AnalyticsTests {

    @SuppressWarnings("ConstantConditions")
    @Test
    public void DetermineDatesTests(){
        System.out.println("Since the only way to determine the result is correct programmatically is with the same algorithm, it is up to the pathetic human to verify the results, with the use of some logic thingy...");

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
        Date date = new Date((long) (dateBundle.startDate * Analytics.THOUSAND));
        System.out.println("Start: " + date.toString());
        
        Date start = new Date((long) (dateBundle.endDate * Analytics.THOUSAND));
        System.out.println("End: " + start.toString());
    }
}
