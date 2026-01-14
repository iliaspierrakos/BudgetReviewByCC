package FeaturesTest;

import UserFeatures.Ministry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestMinistry {

    @Test
    void testConstructorAndGetters() {
        Ministry m = new Ministry("Health", 1000.50);

        assertEquals("Health", m.getMinistryName());
        assertEquals(1000.50, m.getBudget());
    }

    @Test
    void testConstructorWithInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> new Ministry("", 1000));

        assertThrows(IllegalArgumentException.class, () -> new Ministry(null, 1000));
    }

    @Test
    void testSetMinistryNameValid() {
        Ministry m = new Ministry("Education", 500);
        m.setMinistryName("Defense");

        assertEquals("Defense", m.getMinistryName());
    }

    @Test
    void testSetMinistryNameInvalid() {
        Ministry m = new Ministry("Education", 500);

        assertThrows(IllegalArgumentException.class, () -> m.setMinistryName(""));

        assertThrows(IllegalArgumentException.class, () -> m.setMinistryName(null));
    }

    @Test
    void testSetBudgetValid() {
        Ministry m = new Ministry("Finance", 1000);
        m.setBudget(2000);

        assertEquals(2000, m.getBudget());
    }

    @Test
    void testSetBudgetNegative() {
        Ministry m = new Ministry("Finance", 1000);

        assertThrows(IllegalArgumentException.class, () -> m.setBudget(-1));
    }

    @Test
    void testFindByNameFound() {
        Ministry m1 = new Ministry("Health", 1000);
        Ministry m2 = new Ministry("Education", 2000);

        Ministry[] ministries = { m1, m2 };

        Ministry result = Ministry.findByName("education", ministries);

        assertNotNull(result);
        assertEquals("Education", result.getMinistryName());
    }

    @Test
    void testFindByNameNotFound() {
        Ministry m1 = new Ministry("Health", 1000);
        Ministry[] ministries = { m1 };

        Ministry result = Ministry.findByName("Defense", ministries);

        assertNull(result);
    }

    @Test
    void testBudgetSearchByNameFound() {
        Ministry m1 = new Ministry("Health", 1000);
        Ministry m2 = new Ministry("Education", 2000);

        Ministry[] ministries = { m1, m2 };

        double budget = Ministry.budgetSearchByName("Health", ministries);

        assertEquals(1000, budget);
    }

    @Test
    void testBudgetSearchByNameNotFound() {
        Ministry m1 = new Ministry("Health", 1000);
        Ministry[] ministries = { m1 };

        assertThrows(IllegalArgumentException.class, () -> Ministry.budgetSearchByName("Defense", ministries));
    }

    @Test
    void testFormattedBudget() {
        String formatted = Ministry.getFormattedBudget(1234567.89);

        assertEquals("1.234.567,89", formatted);
    }

    @Test
    void testYesOrNo() {
        assertEquals("yes", Ministry.yesOrNo("yes"));
        assertEquals("no", Ministry.yesOrNo("no"));
        assertEquals("no", Ministry.yesOrNo("maybe"));
        assertEquals("no", Ministry.yesOrNo(null));
    }

    @Test
    void testToStringContainsNameAndBudget() {
        Ministry m = new Ministry("Health", 1000);

        String result = m.toString();

        assertTrue(result.contains("Health"));
        assertTrue(result.contains("1.000"));
    }
}
