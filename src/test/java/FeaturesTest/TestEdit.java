package FeaturesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;

public class TestEdit {

    @BeforeEach
    void resetState() {
        // reset static state
        Edit.balance = 0;

        // initialize ministries for tests
        CreatingMinistries.ministries2026 = new Ministry[] {
                new Ministry("Ministry of Health", 1000)
        };
    }

    @Test
    void testApplyEditIncreaseNotProposal() {
        Ministry m = CreatingMinistries.ministries2026[0];
        double oldBudget = m.getBudget();

        Edit edit = new Edit(
                "Ministry of Health",
                "Increase",
                100,
                "fixed"
        );

        Edit.applyEdit(edit, false, false);

        assertEquals(oldBudget + 100, m.getBudget(),
                "Increase should raise ministry budget");
        assertEquals(-100, Edit.balance,
                "Increase should reduce available balance");
    }

    @Test
    void testApplyEditDecreaseNotProposal() {
        Ministry m = CreatingMinistries.ministries2026[0];
        double oldBudget = m.getBudget();

        Edit edit = new Edit(
                "Ministry of Health",
                "Decrease",
                200,
                "fixed"
        );

        Edit.applyEdit(edit, false, false);

        assertEquals(oldBudget - 200, m.getBudget(),
                "Decrease should lower ministry budget");
        assertEquals(200, Edit.balance,
                "Decrease should increase available balance");
    }

    @Test
    void testApplyEditUndo() {
        Ministry m = CreatingMinistries.ministries2026[0];
        double oldBudget = m.getBudget();

        Edit edit = new Edit(
                "Ministry of Health",
                "Increase",
                150,
                "fixed"
        );

        // apply edit
        Edit.applyEdit(edit, false, false);
        // undo edit
        Edit.applyEdit(edit, true, false);

        assertEquals(oldBudget, m.getBudget(),
                "Undo should restore original budget");
        assertEquals(0, Edit.balance,
                "Undo should restore balance to zero");
    }

    @Test
    void testApplyEditProposalDoesNotChangeBudget() {
        Ministry m = CreatingMinistries.ministries2026[0];
        double oldBudget = m.getBudget();

        Edit edit = new Edit(
                "Ministry of Health",
                "Increase",
                100,
                "fixed"
        );

        Edit.applyEdit(edit, false, true);

        assertEquals(oldBudget, m.getBudget(),
                "Proposal must not change real budget");
        assertEquals(-100, Edit.balance,
                "Proposal still affects temporary balance");
    }

    @Test
    void testApplyEditInvalidMinistryThrowsException() {
        Edit edit = new Edit(
                "Non Existing Ministry",
                "Increase",
                100,
                "fixed"
        );

        assertThrows(IllegalArgumentException.class,
                () -> Edit.applyEdit(edit, false, false),
                "Editing unknown ministry should throw exception");
    }
}
