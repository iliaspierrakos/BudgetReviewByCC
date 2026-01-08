package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Test;

public class TestCreatingMinistries {
    @Test
    public void testYearAssignmentLogic() {
        Assert.assertEquals("failure - 2020 array size", 20, CreatingMinistries.ministries2020.length);
        Assert.assertEquals("failure - 2026 array size", 20, CreatingMinistries.ministries2026.length);
        
        CreatingMinistries.ministries2026[0] = new Ministry("Test Ministry", 5000.0);
        Assert.assertTrue("failure - ministry not assigned to 2026", CreatingMinistries.ministries2026[0] != null);
        Assert.assertEquals("failure - budget mismatch", (Double) 5000.0, (Double) CreatingMinistries.ministries2026[0].getBudget());
    }
}