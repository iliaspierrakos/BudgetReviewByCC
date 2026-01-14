package FeaturesTest;

import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.Ministry;
import UserFeatures.CreatingMinistries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestEditHistoryList {

    private EditHistoryList history;
    private Ministry ministry;

    @BeforeEach
    void setup() {
        // reset static state
        Edit.balance = 0;
        Edit.history = new EditHistoryList();

        // ensure ministry exists
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("Ministry of Health", 1000) };

        ministry = CreatingMinistries.ministries2026[0];
        history = new EditHistoryList();
    }

    @Test
    void testAddEditAndGetEditList() {
        Edit e = new Edit("Ministry of Health", "Increase", 100);

        history.addEdit(e);

        List<Edit> list = history.getEditList();
        assertEquals(1, list.size());
        assertEquals(0, history.getIndex());
        assertEquals(e, list.get(0));
    }

    @Test
    void testClear() {
        history.addEdit(new Edit("Ministry of Health", "Increase", 100));
        history.clear();

        assertTrue(history.getEditList().isEmpty());
        assertEquals(-1, history.getIndex());
    }

    @Test
    void testUndoRemovesLastEditAndRevertsBudget() {
        double originalBudget = ministry.getBudget();

        Edit e = new Edit("Ministry of Health", "Increase", 200);
        history.addEdit(e);

        // apply edit
        Edit.applyEdit(e, false, false);

        assertEquals(originalBudget + 200, ministry.getBudget());

        history.undo();

        assertEquals(originalBudget, ministry.getBudget());
        assertEquals(0, Edit.balance);
        assertEquals(-1, history.getIndex());
    }

    @Test
    void testReverseEditRevertsBudgetButKeepsHistory() {
        double originalBudget = ministry.getBudget();

        Edit e = new Edit("Ministry of Health", "Increase", 150);
        history.addEdit(e);

        Edit.applyEdit(e, false, false);
        history.reverseEdit(e);

        assertEquals(originalBudget, ministry.getBudget());
    }

    @Test
    void testApplyingEditsAppliesAllAndClearsHistory() {
        double originalBudget = ministry.getBudget();

        Edit e1 = new Edit("Ministry of Health", "Increase", 100);
        Edit e2 = new Edit("Ministry of Health", "Decrease", 50);

        history.addEdit(e1);
        history.addEdit(e2);

        history.applyingEdits();

        assertEquals(originalBudget + 50, ministry.getBudget());
        assertTrue(history.getEditList().isEmpty());
        assertEquals(-1, history.getIndex());
    }
}
