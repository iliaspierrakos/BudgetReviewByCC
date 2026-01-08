package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestEditHistoryList {
    private EditHistoryList history;

    @Before
    public void setup() {
        history = new EditHistoryList();
        CreatingMinistries.ministries2026 = new Ministry[1];
        CreatingMinistries.ministries2026[0] = new Ministry("Ministry of Health", 1000.0);
        Edit.balance = 100.0;
    }

    @Test
    public void testUndoAndBalanceRecovery() {
        Edit e = new Edit("Ministry of Health", "Increase", 500.0, "Fixed");
        history.addEdit(e);
        
        Edit.balance = Edit.balance - 500.0; 
        
        history.undo();
        
        Assert.assertEquals("failure - index should be reset", -1, history.getIndex());
        Assert.assertEquals("failure - balance recovery failed", (Double) 100.0, (Double) Edit.balance);
    }
}