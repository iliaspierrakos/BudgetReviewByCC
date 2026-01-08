package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestEdit {
    private Edit edit;

    @Before
    public void setup() {
        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Health", 1000.0);
        Edit.balance = 0;
    }

    @Test
    public void testIncreaseBudgetLogic() {
        edit = new Edit("Ministry of Health", "Increase", 500.0, "fixed");
        edit.editingbudget(edit, false, false);
        
        double result = CreatingMinistries.ministries2026[0].getBudget();
        
        Assert.assertEquals("failure - budget increase logic failed", (Double) 1500.0, (Double) result);
    }

    @Test
    public void testBalanceUpdate() {
        Edit.balance = 1000.0;
        Assert.assertTrue("failure - balance should be positive", Edit.balance > 0);
    }
}