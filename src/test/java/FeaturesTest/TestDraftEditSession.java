package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.CreatingMinistries;
import UserFeatures.DraftEditSession;
import UserFeatures.Ministry;

/**
 * Unit tests for {@link DraftEditSession}.
 *
 * <p>
 * These tests verify the correct behavior of the draft budget editing session,
 * including initialization, fixed and percentage edits, undo functionality,
 * draft balance handling, and history tracking.
 * </p>
 *
 * <p>
 * Each test starts from a clean static state to ensure isolation and
 * reproducibility.
 * </p>
 */
public class TestDraftEditSession {

    /**
     * Resets all static state and initializes a fresh draft session
     * from the current official ministries before each test.
     */
    @BeforeEach
    void resetState() {
        forceResetDraftSession();

        CreatingMinistries.ministries2026 = new Ministry[] {
                new Ministry("Ministry of Health", 1000),
                new Ministry("Ministry of Education", 500)
        };

        DraftEditSession.resetFromCurrentBudgets();
    }

    /**
     * Verifies that resetting from current budgets creates
     * a deep copy of ministries rather than reusing references.
     */
    @Test
    void testResetFromCurrentCreatesSandboxCopyNotSameObjects() {
        Ministry official = CreatingMinistries.ministries2026[0];
        Ministry sandbox = DraftEditSession.getSandbox()[0];

        assertEquals(official.getMinistryName(), sandbox.getMinistryName());
        assertEquals(official.getBudget(), sandbox.getBudget());

        sandbox.setBudget(777);
        assertEquals(1000, official.getBudget(),
                "Official ministry budget must not be affected by sandbox changes");
    }

    /**
     * Ensures ministry lookup by name is case-insensitive.
     */
    @Test
    void testFindIndexByNameIsCaseInsensitive() {
        int idx = DraftEditSession.findIndexByName("ministry of health");
        assertEquals(0, idx);
    }

    /**
     * Confirms that the draft history list cannot be modified externally.
     */
    @Test
    void testGetHistoryIsUnmodifiable() {
        List<DraftEditSession.DraftEdit> history = DraftEditSession.getHistory();

        assertThrows(UnsupportedOperationException.class,
                () -> history.add(new DraftEditSession.DraftEdit("X", "Increase", 1, "fixed")));
    }

    /**
     * Tests that a fixed increase is rejected when draft balance is insufficient.
     */
    @Test
    void testApplyFixedIncreaseFailsWhenInsufficientBalance() {
        Ministry m = DraftEditSession.getSandbox()[0];
        double oldBudget = m.getBudget();

        String err = DraftEditSession.applyFixed("Ministry of Health", true, 100);

        assertEquals("Insufficient draft balance. Decrease another ministry first.", err);
        assertEquals(oldBudget, m.getBudget());
        assertEquals(0, DraftEditSession.getDraftBalance());
        assertTrue(DraftEditSession.getHistory().isEmpty());
    }

    /**
     * Tests that a fixed decrease updates the budget,
     * increases draft balance, and records history.
     */
    @Test
    void testApplyFixedDecreaseIncreasesBalanceAndAddsHistory() {
        Ministry m = DraftEditSession.getSandbox()[0];

        String err = DraftEditSession.applyFixed("Ministry of Health", false, 200);

        assertNull(err);
        assertEquals(800, m.getBudget());
        assertEquals(200, DraftEditSession.getDraftBalance());
        assertEquals(1, DraftEditSession.getHistory().size());
    }

    /**
     * Verifies that a fixed increase succeeds after a prior decrease
     * has created sufficient draft balance.
     */
    @Test
    void testApplyFixedIncreaseAfterDecreaseSucceeds() {
        Ministry m = DraftEditSession.getSandbox()[0];

        DraftEditSession.applyFixed("Ministry of Health", false, 300);
        DraftEditSession.applyFixed("Ministry of Health", true, 100);

        assertEquals(800, m.getBudget());
        assertEquals(200, DraftEditSession.getDraftBalance());
        assertEquals(2, DraftEditSession.getHistory().size());
    }

    /**
     * Ensures invalid input values are rejected with appropriate messages.
     */
    @Test
    void testApplyFixedRejectsInvalidInput() {
        assertEquals("Please select a ministry.",
                DraftEditSession.applyFixed(" ", true, 100));

        assertEquals("Amount must be positive.",
                DraftEditSession.applyFixed("Ministry of Health", true, 0));
    }

    /**
     * Tests percentage-based decrease and corresponding balance update.
     */
    @Test
    void testApplyPercentDecreaseWorks() {
        Ministry m = DraftEditSession.getSandbox()[0];

        String err = DraftEditSession.applyPercent("Ministry of Health", false, 10);

        assertNull(err);
        assertEquals(900, m.getBudget(), 1e-9);
        assertEquals(100, DraftEditSession.getDraftBalance(), 1e-9);
    }

    /**
     * Ensures percentage increase is rejected when balance is insufficient.
     */
    @Test
    void testApplyPercentIncreaseFailsWithoutBalance() {
        Ministry m = DraftEditSession.getSandbox()[0];

        String err = DraftEditSession.applyPercent("Ministry of Health", true, 10);

        assertEquals(
                "Insufficient draft balance for this increase. Decrease another ministry first.",
                err
        );
        assertEquals(1000, m.getBudget());
    }

    /**
     * Tests undo functionality restores both budget and draft balance.
     */
    @Test
    void testUndoLastRestoresState() {
        Ministry m = DraftEditSession.getSandbox()[0];

        DraftEditSession.applyFixed("Ministry of Health", false, 200);
        String err = DraftEditSession.undoLast();

        assertNull(err);
        assertEquals(1000, m.getBudget());
        assertEquals(0, DraftEditSession.getDraftBalance());
        assertTrue(DraftEditSession.getHistory().isEmpty());
    }

    /**
     * Verifies undo fails gracefully when no edits exist.
     */
    @Test
    void testUndoLastWithEmptyHistory() {
        String err = DraftEditSession.undoLast();
        assertEquals("No draft edits to undo.", err);
    }

    /**
     * Ensures the compatibility overload of undo behaves identically.
     */
    @Test
    void testUndoCompatibilityOverload() {
        DraftEditSession.applyFixed("Ministry of Health", false, 50);
        assertNull(DraftEditSession.undoLast(0));
    }

    /**
     * Forces a full reset of private static state using reflection.
     * Used to guarantee test isolation.
     */
    private static void forceResetDraftSession() {
        try {
            setStaticField("initialized", false);
            setStaticField("sandbox", null);
            setStaticField("draftBalance", 0.0);

            Field historyField = DraftEditSession.class.getDeclaredField("history");
            historyField.setAccessible(true);
            ((List<?>) historyField.get(null)).clear();
        } catch (Exception e) {
            fail("Failed to reset DraftEditSession state");
        }
    }

    private static void setStaticField(String name, Object value) throws Exception {
        Field f = DraftEditSession.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }
}
