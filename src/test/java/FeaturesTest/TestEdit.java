package FeaturesTest;

import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.CreatingMinistries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestEdit {

    @BeforeEach
    void resetState() {
        // reset static state
        Edit.balance = 0;

       
        if (CreatingMinistries.ministries2026 == null ||
            CreatingMinistries.ministries2026.length == 0 ||
            CreatingMinistries.ministries2026[0] == null) {

            CreatingMinistries.ministries2026 = new Ministry[]{
                    new Ministry("Ministry of Health", 1000)
            };
        }
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

        assertEquals(oldBudget + 100, m.getBudget());
        assertEquals(-100, Edit.balance);
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

        assertEquals(oldBudget - 200, m.getBudget());
        assertEquals(200, Edit.balance);
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

        // apply
        Edit.applyEdit(edit, false, false);
        // undo
        Edit.applyEdit(edit, true, false);

        assertEquals(oldBudget, m.getBudget());
        assertEquals(0, Edit.balance);
    }

    @Test
    void testApplyEditProposalDoesNothing() {
        Ministry m = CreatingMinistries.ministries2026[0];
        double oldBudget = m.getBudget();

        Edit edit = new Edit(
                "Ministry of Health",
                "Increase",
                100,
                "fixed"
        );

        Edit.applyEdit(edit, false, true);

        assertEquals(oldBudget, m.getBudget());
        assertEquals(0, Edit.balance);
    }

    @Test
    void testApplyEditInvalidMinistryThrows() {
        Edit edit = new Edit(
                "Non Existing Ministry",
                "Increase",
                100,
                "fixed"
        );

        assertThrows(IllegalArgumentException.class,
                () -> Edit.applyEdit(edit, false, false));
    }
}
