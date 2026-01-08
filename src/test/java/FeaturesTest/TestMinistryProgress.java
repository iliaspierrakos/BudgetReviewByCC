package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestMinistryProgress {

    @Before
    public void setup() {
        CreatingMinistries.ministries2020 = new Ministry[]{ new Ministry("Defense", 1000.0) };
        CreatingMinistries.ministries2021 = new Ministry[]{ new Ministry("Defense", 1100.0) };
        CreatingMinistries.ministries2022 = new Ministry[]{ new Ministry("Defense", 1200.0) };
    }

    @Test
    public void testProgressDataConsistency() {

        
        String targetName = "Defense";

        Ministry m2020 = Ministry.findByName(targetName, ViewEditBudget.viewBudget(2020));
        Assert.assertTrue("failure - 2020 data missing", m2020 != null);
        Assert.assertEquals("failure - 2020 budget wrong", (Double) 1000.0, (Double) m2020.getBudget());

        Ministry m2021 = Ministry.findByName(targetName, ViewEditBudget.viewBudget(2021));
        Assert.assertTrue("failure - 2021 data missing", m2021 != null);
        Assert.assertTrue("failure - budget did not increase", m2021.getBudget() > m2020.getBudget());
    }
}