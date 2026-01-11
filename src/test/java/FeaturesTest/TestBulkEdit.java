package FeaturesTest;

import UserFeatures.BulkEdit;
import UserFeatures.BulkEdit.ChangeMode;
import UserFeatures.BulkEdit.ChangeType;
import UserFeatures.BulkEdit.BulkEditResult;
import UserFeatures.BulkEdit.PreviewRow;
import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TestBulkEdit {

    private BulkEdit bulkEdit;

    @BeforeEach
    void setup() {
        // reset static state
        Edit.balance = 1000;
        Edit.history.clear();

        // setup ministries
        CreatingMinistries.ministries2026 = new Ministry[]{
                new Ministry("Ministry of Health", 1000),
                new Ministry("Ministry of Education", 2000),
                new Ministry("Ministry of Finance", 3000)
        };

        bulkEdit = new BulkEdit();
    }

    @Test
    void testApplySelectedGuiPercentageIncreaseSuccess() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0); // Health
        indices.add(1); // Education

        BulkEditResult result = bulkEdit.applySelectedGui(
                indices,
                ChangeMode.PERCENTAGE,
                10,
                ChangeType.INCREASE
        );

        assertTrue(result.ok);
        assertEquals(300, result.totalChange); // 10% of 1000 + 2000
        assertEquals(700, Edit.balance);

        assertEquals(1100, CreatingMinistries.ministries2026[0].getBudget());
        assertEquals(2200, CreatingMinistries.ministries2026[1].getBudget());
    }

    @Test
    void testApplySelectedGuiPercentageDecrease() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);

        BulkEditResult result = bulkEdit.applySelectedGui(
                indices,
                ChangeMode.PERCENTAGE,
                20,
                ChangeType.DECREASE
        );

        assertTrue(result.ok);
        assertEquals(-200, result.totalChange);
        assertEquals(1200, Edit.balance);

        assertEquals(800, CreatingMinistries.ministries2026[0].getBudget());
    }

    @Test
    void testApplySelectedGuiFixedIncreaseInsufficientBalance() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);
        indices.add(1);

        BulkEditResult result = bulkEdit.applySelectedGui(
                indices,
                ChangeMode.FIXED,
                1000,
                ChangeType.INCREASE
        );

        assertFalse(result.ok);
        assertTrue(result.message.contains("Insufficient balance"));
    }

    @Test
    void testApplySelectedGuiFixedDecreaseNegativeBudgetBlocked() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);

        BulkEditResult result = bulkEdit.applySelectedGui(
                indices,
                ChangeMode.FIXED,
                2000,
                ChangeType.DECREASE
        );

        assertFalse(result.ok);
        assertTrue(result.message.contains("budget negative"));
    }

    @Test
    void testPreviewSelectedGuiPercentage() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(0);
        indices.add(2);

        ArrayList<PreviewRow> rows = bulkEdit.previewSelectedGui(
                indices,
                ChangeMode.PERCENTAGE,
                10,
                ChangeType.INCREASE
        );

        assertEquals(2, rows.size());

        PreviewRow row = rows.get(0);
        assertEquals("Ministry of Health", row.getMinistry());
        assertEquals(1000, row.getCurrentBudget());
        assertEquals(1100, row.getNewBudget());
        assertEquals(100, row.getChange());
    }

    @Test
    void testPreviewSelectedGuiFixedDecrease() {
        ArrayList<Integer> indices = new ArrayList<>();
        indices.add(1);

        ArrayList<PreviewRow> rows = bulkEdit.previewSelectedGui(
                indices,
                ChangeMode.FIXED,
                500,
                ChangeType.DECREASE
        );

        PreviewRow row = rows.get(0);

        assertEquals(2000, row.getCurrentBudget());
        assertEquals(1500, row.getNewBudget());
        assertEquals(-500, row.getChange());
    }
}
