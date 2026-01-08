package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Test;

public class TestMinistriesBudgets {

    @Test
    public void testYearExtraction() {
        String fileName = "Budget2026Data.csv";
        String year = fileName.replaceAll("\\D+", "");
        
        Assert.assertEquals("failure - year extraction mismatch", "2026", year);
    }

    @Test
    public void testObjectIsReady() {
        MinistriesBudgets mb = new MinistriesBudgets();
        Assert.assertTrue("failure - mb instance is null", mb != null);
    }
}