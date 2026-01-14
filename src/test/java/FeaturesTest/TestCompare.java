package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import UserFeatures.Compare;
import UserFeatures.Ministry;

public class TestCompare {

    @AfterEach
    void resetHook() {
        Compare.TEST_MINISTRY_PROVIDER = null;
    }

    // ================= toMapByName =================

    @Test
    void toMapByName_nullArray_returnsEmptyMap() throws Exception {
        Method m = Compare.class.getDeclaredMethod("toMapByName", Ministry[].class);
        m.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Ministry> result = (Map<String, Ministry>) m.invoke(null, (Object) null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toMapByName_ignoresNullElements() throws Exception {
        Method m = Compare.class.getDeclaredMethod("toMapByName", Ministry[].class);
        m.setAccessible(true);

        Ministry a = new Ministry("Health", 1000);
        Ministry b = new Ministry("Education", 2000);

        Ministry[] input = { a, null, b };

        @SuppressWarnings("unchecked")
        Map<String, Ministry> result = (Map<String, Ministry>) m.invoke(null, (Object) input);

        assertEquals(2, result.size());
        assertSame(a, result.get("Health"));
        assertSame(b, result.get("Education"));
    }

    // ================= CompareRow =================

    @Test
    void compareRow_getters_work() {
        Compare.CompareRow row = new Compare.CompareRow("Health", "1000", "2000");

        assertEquals("Health", row.getMinistry());
        assertEquals("1000", row.getFirstYearBudget());
        assertEquals("2000", row.getSecondYearBudget());
    }

    // ================= getComparisonRowsForGui =================

    @Test
    void guiRows_bothYearsHaveData() {
        Compare.TEST_MINISTRY_PROVIDER = year -> {
            if (year == 2020) {
                return new Ministry[] { new Ministry("Health", 1000), new Ministry("Education", 2000) };
            }
            if (year == 2021) {
                return new Ministry[] { new Ministry("Health", 1500) };
            }
            return null;
        };

        List<Compare.CompareRow> rows = Compare.getComparisonRowsForGui(2020, 2021);

        assertEquals(2, rows.size());

        Compare.CompareRow r1 = rows.get(0);
        assertEquals("Education", r1.getMinistry());
        assertEquals("2.000", r1.getFirstYearBudget());
        assertEquals("-", r1.getSecondYearBudget());

        Compare.CompareRow r2 = rows.get(1);
        assertEquals("Health", r2.getMinistry());
        assertEquals("1.000", r2.getFirstYearBudget());
        assertEquals("1.500", r2.getSecondYearBudget());
    }

    @Test
    void guiRows_missingYear_returnsEmpty() {
        Compare.TEST_MINISTRY_PROVIDER = year -> null;

        List<Compare.CompareRow> rows = Compare.getComparisonRowsForGui(2020, 2021);

        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
}
