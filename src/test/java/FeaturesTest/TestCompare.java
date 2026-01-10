package FeaturesTest;

import UserFeatures.Compare;
import UserFeatures.Compare.CompareRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
}
