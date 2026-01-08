package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestViewGovernmentBudget {
    private ViewGovernmentBudget viewGov;

    @Before
    public void setup() {
        viewGov = new ViewGovernmentBudget();
    }

    @Test
    public void testSortingLogic() {
        Ministry m1 = new Ministry("Alpha", 100.0);
        Ministry m2 = new Ministry("Beta", 200.0);
        Ministry m3 = new Ministry("Gamma", 100.0);

        Ministry[] list = {m1, m2, m3};

        viewGov.sortingBudgets(list);

        Assert.assertEquals("failure - first element should be Beta (max budget)", "Beta", list[0].getMinistryName());
        Assert.assertEquals("failure - second element should be Alpha (alphabetical)", "Alpha", list[1].getMinistryName());
        Assert.assertEquals("failure - third element should be Gamma", "Gamma", list[2].getMinistryName());
    }

    @Test
    public void testMinistryYearSelection() {
        CreatingMinistries.ministries2020 = new Ministry[]{ new Ministry("M2020", 500.0) };
        CreatingMinistries.ministries2026 = new Ministry[]{ new Ministry("M2026", 900.0) };

        Ministry[] result2020 = ViewGovernmentBudget.ministryYear(2020);
        Assert.assertTrue("failure - 2020 data not found", result2020 != null);
        Assert.assertEquals("failure - wrong year data returned", "M2020", result2020[0].getMinistryName());

        Ministry[] result2026 = ViewGovernmentBudget.ministryYear(2026);
        Assert.assertEquals("failure - wrong year data returned", "M2026", result2026[0].getMinistryName());
        
        Ministry[] resultInvalid = ViewGovernmentBudget.ministryYear(1900);
        Assert.assertTrue("failure - invalid year should return null", resultInvalid == null);
    }
}