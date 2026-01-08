package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMinistry {
    private Ministry ministry;

    @Before
    public void setup() {
        ministry = new Ministry("Test Ministry", 1000.0);
    }

    @Test
    public void testConstructorAndGetters() {
        Assert.assertEquals("failure - name mismatch", "Test Ministry", ministry.getMinistryName());
        Assert.assertEquals("failure - budget mismatch", (Double) 1000.0, (Double) ministry.getBudget());
    }

    @Test
    public void testSetBudgetValidation() {

        ministry.setBudget(500.0);
        Assert.assertEquals("failure - valid budget update failed", (Double) 500.0, (Double) ministry.getBudget());

        try {
            ministry.setBudget(-100.0);
            Assert.fail("failure - negative budget should throw exception");
        } catch (IllegalArgumentException e) {
            // Επιτυχία, πιάσαμε το exception
            Assert.assertTrue("failure - exception message mismatch", e.getMessage().contains("negative"));
        }
    }

    @Test
    public void testFindByName() {
        Ministry[] list = { ministry, new Ministry("Other", 200.0) };
        
        Ministry found = Ministry.findByName("test ministry", list);
        Assert.assertTrue("failure - ministry not found", found != null);
        Assert.assertEquals("failure - found wrong ministry", "Test Ministry", found.getMinistryName());

        Ministry notFound = Ministry.findByName("Ghost Ministry", list);
        Assert.assertTrue("failure - should return null", notFound == null);
    }
}