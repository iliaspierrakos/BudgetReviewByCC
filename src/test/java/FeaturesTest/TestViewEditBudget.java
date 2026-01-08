package FeaturesTest;

import UserFeatures.*;

import UserFeatures.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestViewEditBudget {

    @Before
    public void setup() {
        CreatingMinistries.ministries2020 = new Ministry[]{ new Ministry("M2020", 100.0) };
        CreatingMinistries.ministries2026 = new Ministry[]{ new Ministry("M2026", 600.0) };
    }

    @Test
    public void testViewBudgetYearSelection() {
        Ministry[] res2020 = ViewEditBudget.viewBudget(2020);
        Assert.assertEquals("failure - wrong array for 2020", "M2020", res2020[0].getMinistryName());

        Ministry[] res2026 = ViewEditBudget.viewBudget(2026);
        Assert.assertEquals("failure - wrong array for 2026", "M2026", res2026[0].getMinistryName());
    }

    @Test
    public void testInvalidYear() {
        try {
            ViewEditBudget.viewBudget(1999);
            Assert.fail("failure - invalid year should throw exception");
        } catch (IllegalArgumentException e) { }
    }
}