package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Test;
import java.util.List;

public class TestCompare {
    @Test
    public void testGuiRowsLogic() {
        Ministry[] year1 = new Ministry[2];
        year1[0] = new Ministry("MinA", 100.0);
        year1[1] = new Ministry("MinB", 200.0);
        
        Ministry[] year2 = new Ministry[1];
        year2[0] = new Ministry("MinA", 150.0);

        Compare.CompareRow row = new Compare.CompareRow(year1[0].getMinistryName(), "100", "150");
        
        Assert.assertEquals("failure - ministry mismatch", "MinA", row.getMinistry());
        Assert.assertEquals("failure - first budget mismatch", "100", row.getFirstYearBudget());
        Assert.assertEquals("failure - second budget mismatch", "150", row.getSecondYearBudget());
    }
}