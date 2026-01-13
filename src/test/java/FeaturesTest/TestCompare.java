package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import UserFeatures.Compare;
import UserFeatures.Ministry;

/**
 * Unit tests for {@link Compare}.
 *
 * <p>
 * Note: {@code Compare} uses a private static final Scanner for console input.
 * Without modifying production code, we do not unit-test interactive methods
 * that depend on that scanner (e.g., validityYear and comparingMinistries).
 * </p>
 */
public class TestCompare {

    /**
     * Verifies toMapByName(...) returns an empty map for null input.
     */
    @Test
    void testToMapByNameNullArrayReturnsEmptyMap() throws Exception {
        Method toMapByName = Compare.class.getDeclaredMethod("toMapByName", Ministry[].class);
        toMapByName.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Ministry> map = (Map<String, Ministry>) toMapByName.invoke(null, (Object) null);

        assertNotNull(map, "Map should not be null");
        assertTrue(map.isEmpty(), "Map should be empty when input is null");
    }

    /**
     * Verifies toMapByName(...) ignores null array elements and maps by ministry name.
     */
    @Test
    void testToMapByNameIgnoresNullElements() throws Exception {
        Method toMapByName = Compare.class.getDeclaredMethod("toMapByName", Ministry[].class);
        toMapByName.setAccessible(true);

        Ministry a = new Ministry("Ministry of Health", 1000);
        Ministry b = new Ministry("Ministry of Education", 500);

        Ministry[] arr = new Ministry[] { a, null, b };

        @SuppressWarnings("unchecked")
        Map<String, Ministry> map = (Map<String, Ministry>) toMapByName.invoke(null, (Object) arr);

        assertEquals(2, map.size(), "Map should contain only the non-null ministries");
        assertSame(a, map.get("Ministry of Health"), "Map should index by ministry name");
        assertSame(b, map.get("Ministry of Education"), "Map should index by ministry name");
    }

    /**
     * Verifies getComparisonRowsForGui(...) never returns null and returns an empty list
     * when the underlying data source has no data for the given years.
     */
    @Test
    void testGetComparisonRowsForGuiMissingDataReturnsEmpty() {
        List<Compare.CompareRow> rows = Compare.getComparisonRowsForGui(1900, 1901);

        assertNotNull(rows, "Method should never return null");
        assertTrue(rows.isEmpty(), "Missing years should yield empty GUI rows");
    }
}
