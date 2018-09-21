package za.org.samac.harvest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PolygonContainsPointTest {

    @Test
    public void polygonContainsPointValidator() {
        List<Double> x = new ArrayList<>();

        x.add(-25.72851215);
        x.add(-25.72851215);
        x.add(-25.74527022);
        x.add(-25.74623122);
        x.add(-25.74102861);
        x.add(-25.74102861);

        List<Double> y = new ArrayList<>();
        y.add(28.20080128);
        y.add(28.20080128);
        y.add(28.23202624);
        y.add(28.21209321);
        y.add(28.20344797);
        y.add(28.20344797);
        assertThat(polygonContainsPoint(0.0, 0.0, x, y), is(false));//outside
        assertThat(polygonContainsPoint(-25.74527022, 28.23202624, x, y), is(false));//border
        assertThat(polygonContainsPoint(-25.72851216, 28.20080129, x, y), is(true));//inside
    }

    private Boolean polygonContainsPoint(Double x, Double y, List<Double> px, List<Double> py) {
        Double pointx = x;
        Double pointy = y;

        int i = 0;
        int j = px.size() - 1;
        Boolean c = false;
        for (; i < px.size(); j = i++) {
            final Boolean yValid = (py.get(i) > pointy) != (py.get(j) > pointy);
            final Double xValidCond = (px.get(j) - px.get(i)) * (pointy - py.get(i)) / (py.get(j) - py.get(i)) + px.get(i);

            if (yValid && pointx < xValidCond) {
                c = !c;
            }
        }

        return c;
    }
}
