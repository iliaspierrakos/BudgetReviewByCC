package FeaturesTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import UserFeatures.Compare;
import UserFeatures.Compare.CompareRow;

public class TestCompare {

    @Test
    void testGetComparisonRowsForGuiReturnsRows() {
        // Act
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(2025, 2026);

        // Assert: δεν πρέπει να είναι null
        assertNotNull(rows);

        // Αν υπάρχουν δεδομένα, πρέπει να υπάρχουν γραμμές
        assertFalse(rows.isEmpty(), "Comparison rows should not be empty");

        // Έλεγχος δομής μιας γραμμής
        CompareRow row = rows.get(0);

        assertNotNull(row.getMinistry());
        assertNotNull(row.getFirstYearBudget());
        assertNotNull(row.getSecondYearBudget());
    }

    @Test
    void testGetComparisonRowsForGuiSameYearsHandled() {
        // Act
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(2026, 2026);

        // Assert: δεν σκάει
        assertNotNull(rows);
    }

    @Test
    void testGetComparisonRowsForGuiInvalidYearReturnsEmpty() {
        // Act
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(2019, 2030);

        // Assert
        assertNotNull(rows);
        assertTrue(rows.isEmpty(), "Invalid years should return empty list");
    }
    @Test
    void testNullMinistryArrayReturnsEmptyList() {
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(-1, -1);

        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }
    @Test
    void testComparisonRowsAreSortedByMinistryName() {
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(2025, 2026);

        assertTrue(rows.size() > 1);

        for (int i = 1; i < rows.size(); i++) {
            assertTrue(
                    rows.get(i - 1).getMinistry()
                            .compareTo(rows.get(i).getMinistry()) <= 0,
                    "Rows should be sorted alphabetically"
            );
        }
    }
    @Test
    void testComparisonHandlesMissingMinistryInOneYear() {
        List<CompareRow> rows =
                Compare.getComparisonRowsForGui(2020, 2026);

        assertNotNull(rows);
        assertFalse(rows.isEmpty());

        boolean hasDash =
                rows.stream().anyMatch(r ->
                        r.getFirstYearBudget().equals("-")
                        || r.getSecondYearBudget().equals("-")
                );

        assertTrue(hasDash, "Missing ministry should produce '-'");
    }
}
