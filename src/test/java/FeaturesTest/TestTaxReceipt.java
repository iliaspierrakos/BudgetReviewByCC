package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserFeatures.TaxReceipt;
import UserFeatures.TaxReceipt.TaxResult;
import UserFeatures.TaxReceipt.TaxRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestTaxReceipt {

    @BeforeEach
    void setup() {
        // Setup σταθερό κυβερνητικό budget
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("Ministry of Health", 2000),
                new Ministry("Ministry of Education", 3000), new Ministry("Ministry of Finance", 5000) };
    }

    @Test
    void testGenerateForGuiBasicScenario() {
        TaxResult result = TaxReceipt.generateForGui(30000, // income
                1, // kids
                40 // age
        );

        assertNotNull(result);
        assertEquals(30000, result.getIncome());
        assertEquals(1, result.getKids());
        assertEquals(40, result.getAge());

        // Ο φόρος πρέπει να είναι > 0
        assertTrue(result.getTax() > 0);

        // Πρέπει να υπάρχουν rows
        List<TaxRow> rows = result.getRows();
        assertFalse(rows.isEmpty());

        // Τα ministries πρέπει να υπάρχουν
        assertEquals("Ministry of Finance", rows.get(0).getMinistry());

        // Sorting: μεγαλύτερο budget -> μεγαλύτερο share
        double first = rows.get(0).getShareValue();
        double second = rows.get(1).getShareValue();
        assertTrue(first >= second);
    }

    @Test
    void testDistributionSumsApproximatelyToTax() {
        TaxResult result = TaxReceipt.generateForGui(40000, 0, 45);

        double sum = result.getRows().stream().mapToDouble(TaxRow::getShareValue).sum();

        // floating point ανοχή
        assertEquals(result.getTax(), sum, 0.01);
    }

    @Test
    void testFormattingTexts() {
        TaxResult result = TaxReceipt.generateForGui(25000, 2, 35);

        assertNotNull(result.getIncomeText());
        assertNotNull(result.getTaxText());

        TaxRow row = result.getRows().get(0);
        assertNotNull(row.getShareText());
    }

    @Test
    void testYoungPersonTaxExemption() {
        TaxResult result = TaxReceipt.generateForGui(15000, 0, 23 // age <= 25 => μειωμένος/μηδενικός φόρος
        );

        assertEquals(0, result.getTax(), 0.01);
        assertTrue(result.getRows().isEmpty());
    }

    @Test
    void testInvalidIncomeThrows() {
        assertThrows(IllegalArgumentException.class, () -> TaxReceipt.generateForGui(-1, 0, 30));
    }

    @Test
    void testInvalidKidsThrows() {
        assertThrows(IllegalArgumentException.class, () -> TaxReceipt.generateForGui(20000, -2, 30));
    }

    @Test
    void testInvalidAgeThrows() {
        assertThrows(IllegalArgumentException.class, () -> TaxReceipt.generateForGui(20000, 1, 17));
    }

    @Test
    void testZeroGovernmentBudgetThrows() {
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("Ministry of Empty", 0) };

        assertThrows(IllegalStateException.class, () -> TaxReceipt.generateForGui(20000, 0, 40));
    }
}
