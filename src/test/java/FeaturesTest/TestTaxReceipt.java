package FeaturesTest;

import UserFeatures.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.List;

public class TestTaxReceipt {

    @Before
    public void setup() {
        CreatingMinistries.ministries2026 = new Ministry[2];
        CreatingMinistries.ministries2026[0] = new Ministry("Health", 6000.0);
        CreatingMinistries.ministries2026[1] = new Ministry("Education", 4000.0);
    }

    @Test
    public void testTaxCalculationYoungLowIncome() {
        TaxReceipt.TaxResult result = TaxReceipt.generateForGui(9000, 0, 24);
        
        Assert.assertEquals("failure - tax should be 0 for young low income", (Double) 0.0, (Double) result.getTax());
        Assert.assertEquals("failure - income mismatch", (Double) 9000.0, (Double) result.getIncome());
    }

    @Test
    public void testTaxCalculationMiddleIncomeNoKids() {
        TaxReceipt.TaxResult result = TaxReceipt.generateForGui(25000, 0, 35);
        
        Assert.assertTrue("failure - tax should be positive", result.getTax() > 0);
        Assert.assertTrue("failure - tax should be less than income", result.getTax() < 25000);
    }

    @Test
    public void testTaxCalculationHighIncomeManyKids() {
        TaxReceipt.TaxResult result = TaxReceipt.generateForGui(60000, 4, 45);
        TaxReceipt.TaxResult resultNoKids = TaxReceipt.generateForGui(60000, 0, 45);

        Assert.assertTrue("failure - tax reduction for kids not applied", result.getTax() < resultNoKids.getTax());
    }

    @Test
    public void testDistributionRows() {
        TaxReceipt.TaxResult result = TaxReceipt.generateForGui(50000, 0, 40);
        List<TaxReceipt.TaxRow> rows = result.getRows();

        Assert.assertEquals("failure - wrong number of ministry rows", 2, rows.size());
        
        TaxReceipt.TaxRow row1 = rows.get(0);
        Assert.assertEquals("failure - wrong ministry name", "Health", row1.getMinistry());
    }
}