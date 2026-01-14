package FeaturesTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import UserFeatures.BulkEdit;
import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.EditHistoryList;
import UserFeatures.Ministry;

/**
 * Comprehensive test class for {@link BulkEdit}.
 *
 * <p>
 * This test suite covers:
 * <ul>
 * <li>GUI API methods (applySelectedGui, previewSelectedGui)</li>
 * <li>Helper methods (fillingListWithIndex, validateForDecrease, smallerNegative)</li>
 * <li>Balance management and validation</li>
 * <li>Edge cases and error conditions</li>
 * </ul>
 *
 * <p>
 * <strong>Note:</strong> Console-based interactive methods are not tested as they require Scanner input simulation.
 * Focus is on testable logic methods.
 * </p>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestBulkEdit {

    private BulkEdit bulkEdit;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private PrintStream originalErr;

    // Test ministries
    private static final String DEFENSE = "Ministry of Defense";
    private static final String EDUCATION = "Ministry of Education";
    private static final String HEALTHCARE = "Ministry of Healthcare";
    private static final String TRANSPORT = "Ministry of Transport";
    private static final String AGRICULTURE = "Ministry of Agriculture";

    @BeforeAll
    static void setUpClass() {
        // Initialize ministries array with test data
        CreatingMinistries.ministries2026 = new Ministry[20];
        CreatingMinistries.ministries2026[0] = new Ministry(DEFENSE, 1000000.0);
        CreatingMinistries.ministries2026[1] = new Ministry(EDUCATION, 800000.0);
        CreatingMinistries.ministries2026[2] = new Ministry(HEALTHCARE, 600000.0);
        CreatingMinistries.ministries2026[3] = new Ministry(TRANSPORT, 400000.0);
        CreatingMinistries.ministries2026[4] = new Ministry(AGRICULTURE, 300000.0);
    }

    @BeforeEach
    void setUp() {
        bulkEdit = new BulkEdit();

        // Reset Edit.balance and history before each test
        Edit.balance = 500000.0;
        Edit.history = new EditHistoryList();

        // Reset ministry budgets to original values
        CreatingMinistries.ministries2026[0].setBudget(1000000.0);
        CreatingMinistries.ministries2026[1].setBudget(800000.0);
        CreatingMinistries.ministries2026[2].setBudget(600000.0);
        CreatingMinistries.ministries2026[3].setBudget(400000.0);
        CreatingMinistries.ministries2026[4].setBudget(300000.0);

        // Capture console output
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(new ByteArrayOutputStream())); // Suppress error output
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // ==================== Helper Method ====================

    private ArrayList<Integer> createSelectedIndices(int... indices) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int idx : indices) {
            list.add(idx);
        }
        return list;
    }

    // ==================== GUI API - Percentage Tests ====================

    @Test
    @Order(1)
    @DisplayName("GUI: Apply 10% increase to selected ministries - success")
    void testApplySelectedGui_PercentageIncrease_Success() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1); // Defense (1M), Education (800K)
        double percentage = 10.0;
        double initialBalance = Edit.balance;
        double expectedChange = (1000000.0 * 0.10) + (800000.0 * 0.10); // 100K + 80K = 180K

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Operation should succeed");
        assertEquals(180000.0, result.totalChange, 0.01, "Total change should be 180,000");
        assertEquals(initialBalance - 180000.0, Edit.balance, 0.01, "Balance should decrease by total change");
        assertEquals(1100000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Defense budget should increase by 10%");
        assertEquals(880000.0, CreatingMinistries.ministries2026[1].getBudget(), 0.01,
                "Education budget should increase by 10%");
    }

    @Test
    @Order(2)
    @DisplayName("GUI: Apply 15% decrease to selected ministries - success")
    void testApplySelectedGui_PercentageDecrease_Success() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(2, 3); // Healthcare (600K), Transport (400K)
        double percentage = 15.0;
        double initialBalance = Edit.balance;
        double expectedChange = -((600000.0 * 0.15) + (400000.0 * 0.15)); // -(90K + 60K) = -150K

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Operation should succeed");
        assertTrue(result.totalChange < 0, "Total change should be negative for decrease");
        assertEquals(initialBalance + 150000.0, Edit.balance, 0.01, "Balance should increase by returned funds");
        assertEquals(510000.0, CreatingMinistries.ministries2026[2].getBudget(), 0.01,
                "Healthcare budget should decrease by 15%");
    }

    @Test
    @Order(3)
    @DisplayName("GUI: Apply percentage increase with insufficient balance - failure")
    void testApplySelectedGui_PercentageIncrease_InsufficientBalance() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1, 2); // Total would need 240K
        double percentage = 10.0;
        Edit.balance = 50000.0; // Not enough
        double originalBalance = Edit.balance;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail due to insufficient balance");
        assertTrue(result.message.toLowerCase().contains("insufficient"),
                "Message should mention insufficient balance");
        assertEquals(originalBalance, Edit.balance, 0.01, "Balance should not change when operation fails");
        assertEquals(1000000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Ministry budgets should not change when operation fails");
    }

    @Test
    @Order(4)
    @DisplayName("GUI: Apply 100% decrease - failure")
    void testApplySelectedGui_Percentage100Decrease_Failure() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 100.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail for 100% decrease");
        assertTrue(result.message.contains("100%"), "Message should mention 100%");
    }

    @Test
    @Order(5)
    @DisplayName("GUI: Apply more than 100% decrease - failure")
    void testApplySelectedGui_PercentageOver100Decrease_Failure() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 150.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail for >100% decrease");
    }

    // ==================== GUI API - Fixed Amount Tests ====================

    @Test
    @Order(6)
    @DisplayName("GUI: Apply fixed amount increase to selected ministries - success")
    void testApplySelectedGui_FixedIncrease_Success() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1); // 2 ministries
        double amount = 50000.0;
        double initialBalance = Edit.balance;
        double expectedTotalChange = 50000.0 * 2; // 100K total

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Operation should succeed");
        assertEquals(expectedTotalChange, result.totalChange, 0.01,
                "Total change should be amount × number of ministries");
        assertEquals(initialBalance - expectedTotalChange, Edit.balance, 0.01,
                "Balance should decrease by total change");
        assertEquals(1050000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Defense budget should increase by fixed amount");
        assertEquals(850000.0, CreatingMinistries.ministries2026[1].getBudget(), 0.01,
                "Education budget should increase by fixed amount");
    }

    @Test
    @Order(7)
    @DisplayName("GUI: Apply fixed amount decrease to selected ministries - success")
    void testApplySelectedGui_FixedDecrease_Success() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1);
        double amount = 50000.0;
        double initialBalance = Edit.balance;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Operation should succeed");
        assertTrue(result.totalChange < 0, "Total change should be negative");
        assertEquals(initialBalance + 100000.0, Edit.balance, 0.01, "Balance should increase by returned funds");
        assertEquals(950000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Defense budget should decrease by fixed amount");
    }

    @Test
    @Order(8)
    @DisplayName("GUI: Apply fixed decrease causing negative budget - failure")
    void testApplySelectedGui_FixedCausingNegativeBudget_Failure() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture has 300K
        double amount = 400000.0; // More than current budget

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail when budget would become negative");
        assertTrue(result.message.toLowerCase().contains("negative"), "Message should mention negative budget");
        assertEquals(300000.0, CreatingMinistries.ministries2026[4].getBudget(), 0.01,
                "Budget should not change when operation fails");
    }

    @Test
    @Order(9)
    @DisplayName("GUI: Apply fixed increase with insufficient balance - failure")
    void testApplySelectedGui_FixedIncrease_InsufficientBalance() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1, 2); // 3 ministries
        double amount = 100000.0; // Total: 300K
        Edit.balance = 200000.0; // Not enough

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail due to insufficient balance");
        assertEquals(200000.0, Edit.balance, 0.01, "Balance should not change");
    }

    // ==================== GUI API - Selection Validation Tests ====================

    @Test
    @Order(10)
    @DisplayName("GUI: Apply with empty selection - failure")
    void testApplySelectedGui_EmptySelection_Failure() {
        // Arrange
        ArrayList<Integer> selected = new ArrayList<>();

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail with empty selection");
        assertTrue(result.message.toLowerCase().contains("no ministries"), "Message should indicate no selection");
        assertEquals(0.0, result.totalChange, 0.01, "Total change should be zero");
    }

    @Test
    @Order(11)
    @DisplayName("GUI: Apply with null selection - failure")
    void testApplySelectedGui_NullSelection_Failure() {
        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(null, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail with null selection");
        assertEquals(0.0, result.totalChange, 0.01, "Total change should be zero");
    }

    @Test
    @Order(12)
    @DisplayName("GUI: Apply to single ministry")
    void testApplySelectedGui_SingleMinistry() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 5.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Operation should succeed for single ministry");
        assertEquals(50000.0, result.totalChange, 0.01, "Should increase by 5% of 1M");
    }

    // ==================== Preview Tests ====================

    @Test
    @Order(13)
    @DisplayName("GUI: Preview percentage change for selected ministries")
    void testPreviewSelectedGui_PercentageChange() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1);
        double percentage = 10.0;

        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE,
                percentage, BulkEdit.ChangeType.INCREASE);

        // Assert
        assertEquals(2, preview.size(), "Should have 2 preview rows");

        BulkEdit.PreviewRow row1 = preview.get(0);
        assertEquals(DEFENSE, row1.getMinistry());
        assertEquals(1000000.0, row1.getCurrentBudget(), 0.01);
        assertEquals(1100000.0, row1.getNewBudget(), 0.01);
        assertEquals(100000.0, row1.getChange(), 0.01);

        BulkEdit.PreviewRow row2 = preview.get(1);
        assertEquals(EDUCATION, row2.getMinistry());
        assertEquals(800000.0, row2.getCurrentBudget(), 0.01);
        assertEquals(880000.0, row2.getNewBudget(), 0.01);
        assertEquals(80000.0, row2.getChange(), 0.01);
    }

    @Test
    @Order(14)
    @DisplayName("GUI: Preview percentage decrease")
    void testPreviewSelectedGui_PercentageDecrease() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(2);
        double percentage = 20.0;

        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE,
                percentage, BulkEdit.ChangeType.DECREASE);

        // Assert
        assertEquals(1, preview.size(), "Should have 1 preview row");
        BulkEdit.PreviewRow row = preview.get(0);
        assertEquals(HEALTHCARE, row.getMinistry());
        assertEquals(600000.0, row.getCurrentBudget(), 0.01);
        assertEquals(480000.0, row.getNewBudget(), 0.01);
        assertEquals(-120000.0, row.getChange(), 0.01);
    }

    @Test
    @Order(15)
    @DisplayName("GUI: Preview fixed amount change")
    void testPreviewSelectedGui_FixedChange() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(2, 3);
        double amount = 50000.0;

        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.FIXED,
                amount, BulkEdit.ChangeType.INCREASE);

        // Assert
        assertEquals(2, preview.size(), "Should have 2 preview rows");

        BulkEdit.PreviewRow row1 = preview.get(0);
        assertEquals(HEALTHCARE, row1.getMinistry());
        assertEquals(650000.0, row1.getNewBudget(), 0.01);
        assertEquals(50000.0, row1.getChange(), 0.01);

        BulkEdit.PreviewRow row2 = preview.get(1);
        assertEquals(TRANSPORT, row2.getMinistry());
        assertEquals(450000.0, row2.getNewBudget(), 0.01);
    }

    @Test
    @Order(16)
    @DisplayName("GUI: Preview fixed decrease")
    void testPreviewSelectedGui_FixedDecrease() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double amount = 100000.0;

        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.FIXED,
                amount, BulkEdit.ChangeType.DECREASE);

        // Assert
        assertEquals(1, preview.size(), "Should have 1 preview row");
        BulkEdit.PreviewRow row = preview.get(0);
        assertEquals(900000.0, row.getNewBudget(), 0.01);
        assertEquals(-100000.0, row.getChange(), 0.01);
    }

    @Test
    @Order(17)
    @DisplayName("GUI: Preview with empty selection returns empty list")
    void testPreviewSelectedGui_EmptySelection() {
        // Arrange
        ArrayList<Integer> selected = new ArrayList<>();

        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE,
                10.0, BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(preview.isEmpty(), "Preview should be empty for empty selection");
    }

    @Test
    @Order(18)
    @DisplayName("GUI: Preview with null selection returns empty list")
    void testPreviewSelectedGui_NullSelection() {
        // Act
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(null, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(preview.isEmpty(), "Preview should be empty for null selection");
    }

    @Test
    @Order(19)
    @DisplayName("GUI: Preview does not modify actual budgets")
    void testPreviewSelectedGui_DoesNotModifyBudgets() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1);
        double originalDefenseBudget = CreatingMinistries.ministries2026[0].getBudget();
        double originalEducationBudget = CreatingMinistries.ministries2026[1].getBudget();

        // Act
        bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 50.0, // Large change
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertEquals(originalDefenseBudget, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Preview should not modify actual budgets");
        assertEquals(originalEducationBudget, CreatingMinistries.ministries2026[1].getBudget(), 0.01,
                "Preview should not modify actual budgets");
    }

    // ==================== PreviewRow Tests ====================

    @Test
    @Order(20)
    @DisplayName("Test PreviewRow getters return correct values")
    void testPreviewRow_Getters() {
        // Arrange & Act
        BulkEdit.PreviewRow row = new BulkEdit.PreviewRow(DEFENSE, 1000000.0, 1100000.0, 100000.0);

        // Assert
        assertEquals(DEFENSE, row.getMinistry());
        assertEquals(1000000.0, row.getCurrentBudget(), 0.01);
        assertEquals(1100000.0, row.getNewBudget(), 0.01);
        assertEquals(100000.0, row.getChange(), 0.01);
    }

    @Test
    @Order(21)
    @DisplayName("Test PreviewRow formatted text methods return non-null")
    void testPreviewRow_FormattedText() {
        // Arrange
        BulkEdit.PreviewRow row = new BulkEdit.PreviewRow(EDUCATION, 800000.0, 900000.0, 100000.0);

        // Act
        String currentText = row.getCurrentBudgetText();
        String newText = row.getNewBudgetText();
        String changeText = row.getChangeText();

        // Assert
        assertNotNull(currentText, "Current budget text should not be null");
        assertNotNull(newText, "New budget text should not be null");
        assertNotNull(changeText, "Change text should not be null");

        // Verify formatting (German style: 800.000,00)
        assertTrue(currentText.contains("800"), "Should contain budget value");
        assertTrue(newText.contains("900"), "Should contain new budget value");
    }

    @Test
    @Order(22)
    @DisplayName("Test PreviewRow with negative change")
    void testPreviewRow_NegativeChange() {
        // Arrange & Act
        BulkEdit.PreviewRow row = new BulkEdit.PreviewRow(HEALTHCARE, 600000.0, 500000.0, -100000.0);

        // Assert
        assertEquals(-100000.0, row.getChange(), 0.01);
        assertNotNull(row.getChangeText(), "Should format negative values");
    }

    // ==================== Helper Method Tests ====================

    @Test
    @Order(23)
    @DisplayName("Test fillingListWithIndex with valid comma-separated input")
    void testFillingListWithIndex_ValidInput() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,2,3");

        // Assert
        assertEquals(3, result.size(), "Should parse 3 valid indices");
        assertTrue(result.contains(0), "Should contain index 0 (ministry 1)");
        assertTrue(result.contains(1), "Should contain index 1 (ministry 2)");
        assertTrue(result.contains(2), "Should contain index 2 (ministry 3)");
    }

    @Test
    @Order(24)
    @DisplayName("Test fillingListWithIndex with spaces around commas")
    void testFillingListWithIndex_WithSpaces() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1, 2, 3, 4");

        // Assert
        assertEquals(4, result.size(), "Should handle spaces correctly");
        assertEquals(0, result.get(0));
        assertEquals(1, result.get(1));
        assertEquals(2, result.get(2));
        assertEquals(3, result.get(3));
    }

    @Test
    @Order(25)
    @DisplayName("Test fillingListWithIndex with invalid text")
    void testFillingListWithIndex_WithInvalidText() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,abc,3,xyz");

        // Assert
        assertEquals(2, result.size(), "Should skip invalid input");
        assertTrue(result.contains(0) && result.contains(2), "Should only contain valid numeric indices");
    }

    @Test
    @Order(26)
    @DisplayName("Test fillingListWithIndex with out of range numbers")
    void testFillingListWithIndex_OutOfRange() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,25,3,100");

        // Assert
        assertEquals(2, result.size(), "Should ignore out of range indices");
        assertTrue(result.contains(0), "Should contain valid index 0");
        assertTrue(result.contains(2), "Should contain valid index 2");
        assertFalse(result.contains(24), "Should not include out of range index 24");
        assertFalse(result.contains(99), "Should not include out of range index 99");
    }

    @Test
    @Order(27)
    @DisplayName("Test fillingListWithIndex with negative numbers")
    void testFillingListWithIndex_NegativeNumbers() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,-5,3");

        // Assert - negative numbers convert to invalid indices
        assertTrue(result.size() <= 2, "Should handle negative numbers appropriately");
    }

    @Test
    @Order(28)
    @DisplayName("Test fillingListWithIndex with duplicate numbers")
    void testFillingListWithIndex_Duplicates() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,2,1,3,2");

        // Assert - ArrayList allows duplicates
        assertEquals(5, result.size(), "Should parse all numbers including duplicates");
    }

    @Test
    @Order(29)
    @DisplayName("Test fillingListWithIndex with empty string")
    void testFillingListWithIndex_EmptyString() {
        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("");

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list for empty string");
    }

    @Test
    @Order(30)
    @DisplayName("Test fillingListWithIndex with null ministries in array")
    void testFillingListWithIndex_NullMinistriesIgnored() {
        // Arrange - temporarily set some ministries to null
        Ministry savedMinistry = CreatingMinistries.ministries2026[5];
        CreatingMinistries.ministries2026[5] = null;

        // Act
        ArrayList<Integer> result = bulkEdit.fillingListWithIndex("1,6"); // 6 maps to index 5

        // Assert
        assertFalse(result.contains(5), "Should not include null ministry index");

        // Restore
        CreatingMinistries.ministries2026[5] = savedMinistry;
    }

    // ==================== Edge Case Tests ====================

    @Test
    @Order(31)
    @DisplayName("Edge case: Multiple operations on same ministries")
    void testMultipleOperations_SameMinistries() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        Edit.balance = 1000000.0;
        double originalBudget = CreatingMinistries.ministries2026[0].getBudget();

        // Act - First increase
        BulkEdit.BulkEditResult result1 = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Act - Second increase
        BulkEdit.BulkEditResult result2 = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result1.ok && result2.ok, "Both operations should succeed");
        assertTrue(Edit.balance < 1000000.0, "Balance should decrease after increases");

        // 10% of 1M = 100K, then 10% of 1.1M = 110K
        // Total increase should be 210K
        assertTrue(CreatingMinistries.ministries2026[0].getBudget() > originalBudget,
                "Budget should be higher after two increases");
    }

    @Test
    @Order(32)
    @DisplayName("Edge case: Very small percentage change")
    void testVerySmallPercentageChange() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 0.01; // 0.01% of 1M = 100

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Small percentage change should succeed");
        assertTrue(result.totalChange > 0 && result.totalChange < 1000, "Change should be very small but positive");
        assertEquals(100.0, result.totalChange, 0.01, "Should be 0.01% of 1M");
    }

    @Test
    @Order(33)
    @DisplayName("Edge case: Decrease with zero balance")
    void testDecreaseWithZeroBalance() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        Edit.balance = 0.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Decrease should work even with zero balance");
        assertEquals(100000.0, Edit.balance, 0.01, "Should return 10% of 1M");
    }

    @Test
    @Order(34)
    @DisplayName("Edge case: Large percentage increase")
    void testLargePercentageIncrease() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture 300K
        double percentage = 200.0; // 200% increase
        Edit.balance = 1000000.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Large percentage increase should succeed if balance allows");
        assertEquals(600000.0, result.totalChange, 0.01, "Should be 200% of 300K");
        assertEquals(900000.0, CreatingMinistries.ministries2026[4].getBudget(), 0.01,
                "Budget should triple (300K + 600K)");
    }

    @Test
    @Order(35)
    @DisplayName("Edge case: All ministries selected for percentage change")
    void testAllMinistriesSelected_Percentage() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1, 2, 3, 4);
        double percentage = 5.0;
        Edit.balance = 500000.0; // Just enough for 5% increase

        double totalBudget = 1000000.0 + 800000.0 + 600000.0 + 400000.0 + 300000.0; // 3.1M
        double expectedChange = totalBudget * 0.05; // 155K

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle all ministries");
        assertEquals(expectedChange, result.totalChange, 100.0, "Should calculate total change");
    }

    @Test
    @Order(36)
    @DisplayName("Edge case: Very large fixed amount")
    void testVeryLargeFixedAmount() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double amount = 5000000.0;
        Edit.balance = 10000000.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle very large amounts");
        assertEquals(6000000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Budget should increase by large amount");
    }

    @Test
    @Order(37)
    @DisplayName("Edge case: Exact balance match for increase")
    void testExactBalanceMatch() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double amount = 500000.0;
        Edit.balance = 500000.0; // Exactly matches the increase

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should succeed when balance exactly matches");
        assertEquals(0.0, Edit.balance, 0.01, "Balance should be exactly zero");
    }

    @Test
    @Order(38)
    @DisplayName("Edge case: Decrease almost entire budget")
    void testDecreaseToZero() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture 300K
        double percentage = 99.99; // Just under 100%

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Should succeed for decrease less than 100%");
        assertTrue(CreatingMinistries.ministries2026[4].getBudget() > 0, "Budget should be very small but positive");
        assertTrue(CreatingMinistries.ministries2026[4].getBudget() < 100, "Budget should be near zero");
    }

    @Test
    @Order(39)
    @DisplayName("Edge case: Fixed amount exactly equals current budget")
    void testFixedAmountEqualsCurrentBudget() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture 300K
        double amount = 300000.0;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Should succeed when decreasing by exact budget amount");
        assertEquals(0.0, CreatingMinistries.ministries2026[4].getBudget(), 0.01, "Budget should be exactly zero");
    }

    @Test
    @Order(40)
    @DisplayName("Edge case: Alternating increase and decrease operations")
    void testAlternatingOperations() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        Edit.balance = 500000.0;
        double originalBudget = CreatingMinistries.ministries2026[0].getBudget();

        // Act - Increase
        bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, 100000.0, BulkEdit.ChangeType.INCREASE);

        // Act - Decrease
        bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, 50000.0, BulkEdit.ChangeType.DECREASE);

        // Assert
        assertEquals(originalBudget + 50000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Net change should be +50K");
    }

    // ==================== Validation Helper Tests ====================

    @Test
    @Order(41)
    @DisplayName("Test validateForDecrease with positive number")
    void testValidateForDecrease_PositiveNumber() {
        // Arrange
        String input = "5\n-10\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        BulkEdit testBulkEdit = new BulkEdit();

        // Act
        double result = testBulkEdit.validateForDecrease(5.0);

        // Assert
        assertTrue(result < 0, "Should return negative number");
        assertEquals(-10.0, result, 0.01);
    }

    @Test
    @Order(42)
    @DisplayName("Test validateForDecrease with already negative number")
    void testValidateForDecrease_AlreadyNegative() {
        // Act
        double result = bulkEdit.validateForDecrease(-15.0);

        // Assert
        assertEquals(-15.0, result, 0.01, "Should return the same negative number");
    }

    @Test
    @Order(43)
    @DisplayName("Test smallerNegative with amount that would cause negative budget")
    void testSmallerNegative_WouldCauseNegative() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture 300K
        String input = "-400000\n-250000\n"; // First too large, then acceptable
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        BulkEdit testBulkEdit = new BulkEdit();

        // Act
        double result = testBulkEdit.smallerNegative(-400000.0, selected);

        // Assert
        assertTrue(result < 0 && result >= -300000.0,
                "Should return negative amount that doesn't cause negative budget");
    }

    @Test
    @Order(44)
    @DisplayName("Test smallerNegative with acceptable negative amount")
    void testSmallerNegative_AcceptableAmount() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Agriculture 300K
        String input = "-100000\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        BulkEdit testBulkEdit = new BulkEdit();

        // Act
        double result = testBulkEdit.smallerNegative(-100000.0, selected);

        // Assert
        assertEquals(-100000.0, result, 0.01, "Should accept valid negative amount");
    }

    // ==================== Integration Tests ====================

    @Test
    @Order(45)
    @DisplayName("Integration: Complex workflow with multiple operations")
    void testComplexWorkflow() {
        // Arrange
        Edit.balance = 1000000.0;

        // Act - Sequence of operations
        // 1. Increase Defense by 10%
        bulkEdit.applySelectedGui(createSelectedIndices(0), BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // 2. Decrease Education by 50K
        bulkEdit.applySelectedGui(createSelectedIndices(1), BulkEdit.ChangeMode.FIXED, 50000.0,
                BulkEdit.ChangeType.DECREASE);

        // 3. Increase Healthcare by 5%
        bulkEdit.applySelectedGui(createSelectedIndices(2), BulkEdit.ChangeMode.PERCENTAGE, 5.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertEquals(1100000.0, CreatingMinistries.ministries2026[0].getBudget(), 0.01);
        assertEquals(750000.0, CreatingMinistries.ministries2026[1].getBudget(), 0.01);
        assertEquals(630000.0, CreatingMinistries.ministries2026[2].getBudget(), 0.01);

        // Balance: 1M - 100K (defense) + 50K (education) - 30K (healthcare) = 920K
        assertEquals(920000.0, Edit.balance, 0.01);
    }

    @Test
    @Order(46)
    @DisplayName("Integration: Preview then apply workflow")
    void testPreviewThenApplyWorkflow() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1);
        double percentage = 15.0;

        // Act - First preview
        ArrayList<BulkEdit.PreviewRow> preview = bulkEdit.previewSelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE,
                percentage, BulkEdit.ChangeType.INCREASE);

        // Verify preview doesn't change budgets
        double defenseBeforeApply = CreatingMinistries.ministries2026[0].getBudget();

        // Act - Then apply
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertEquals(2, preview.size(), "Preview should show 2 rows");
        assertTrue(result.ok, "Apply should succeed");
        assertNotEquals(defenseBeforeApply, CreatingMinistries.ministries2026[0].getBudget(),
                "Budget should change after apply");
        assertEquals(preview.get(0).getNewBudget(), CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Applied budget should match preview");
    }

    @Test
    @Order(47)
    @DisplayName("Integration: Multiple ministries with mixed operations")
    void testMixedOperations() {
        // Arrange
        Edit.balance = 2000000.0;

        // Act - Increase some, decrease others
        bulkEdit.applySelectedGui(createSelectedIndices(0, 1), BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        bulkEdit.applySelectedGui(createSelectedIndices(2, 3), BulkEdit.ChangeMode.FIXED, 50000.0,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(CreatingMinistries.ministries2026[0].getBudget() > 1000000.0, "Defense should increase");
        assertTrue(CreatingMinistries.ministries2026[1].getBudget() > 800000.0, "Education should increase");
        assertTrue(CreatingMinistries.ministries2026[2].getBudget() < 600000.0, "Healthcare should decrease");
        assertTrue(CreatingMinistries.ministries2026[3].getBudget() < 400000.0, "Transport should decrease");
    }

    // ==================== Boundary Tests ====================

    @Test
    @Order(48)
    @DisplayName("Boundary: Ministry at index 0")
    void testMinistryAtIndexZero() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, 10000.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle first ministry in array");
    }

    @Test
    @Order(49)
    @DisplayName("Boundary: Ministry at last valid index")
    void testMinistryAtLastIndex() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(4); // Last initialized ministry

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, 10000.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle last ministry in array");
    }

    @Test
    @Order(50)
    @DisplayName("Boundary: Percentage at 0.001% (very small)")
    void testVerySmallPercentage() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 0.001;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle very small percentages");
        assertTrue(result.totalChange > 0, "Should still produce some change");
    }

    @Test
    @Order(51)
    @DisplayName("Boundary: Percentage at 99.999% decrease")
    void testAlmostCompleteDecrease() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double percentage = 99.999;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, percentage,
                BulkEdit.ChangeType.DECREASE);

        // Assert
        assertTrue(result.ok, "Should allow decrease just under 100%");
        assertTrue(CreatingMinistries.ministries2026[0].getBudget() > 0, "Budget should be positive but very small");
    }

    @Test
    @Order(52)
    @DisplayName("Boundary: Fixed amount of 0.01 (minimal)")
    void testMinimalFixedAmount() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0);
        double amount = 0.01;

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.FIXED, amount,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        assertTrue(result.ok, "Should handle minimal amounts");
        assertEquals(0.01, result.totalChange, 0.001);
    }

    // ==================== Error Handling Tests ====================

    @Test
    @Order(53)
    @DisplayName("Error: Result object contains correct error messages")
    void testResultErrorMessages() {
        // Test empty selection
        BulkEdit.BulkEditResult result1 = bulkEdit.applySelectedGui(new ArrayList<>(), BulkEdit.ChangeMode.PERCENTAGE,
                10.0, BulkEdit.ChangeType.INCREASE);
        assertFalse(result1.ok);
        assertNotNull(result1.message);
        assertTrue(result1.message.length() > 0);

        // Test insufficient balance
        Edit.balance = 10.0;
        BulkEdit.BulkEditResult result2 = bulkEdit.applySelectedGui(createSelectedIndices(0), BulkEdit.ChangeMode.FIXED,
                1000000.0, BulkEdit.ChangeType.INCREASE);
        assertFalse(result2.ok);
        assertTrue(result2.message.toLowerCase().contains("insufficient"));
    }

    @Test
    @Order(54)
    @DisplayName("Error: Operations maintain data integrity on failure")
    void testDataIntegrityOnFailure() {
        // Arrange
        double initialDefenseBudget = CreatingMinistries.ministries2026[0].getBudget();
        Edit.balance = 100.0; // Not enough for the operation
        double initialBalance = Edit.balance; // Capture the insufficient balance

        // Act - Try to apply change that will fail
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(createSelectedIndices(0), BulkEdit.ChangeMode.FIXED,
                1000000.0, BulkEdit.ChangeType.INCREASE);

        // Assert
        assertFalse(result.ok, "Operation should fail");
        assertEquals(initialDefenseBudget, CreatingMinistries.ministries2026[0].getBudget(), 0.01,
                "Budget should not change on failure");
        assertEquals(initialBalance, Edit.balance, 0.01,
                "Balance should not change on failure (should still be 100.0)");
    }

    @Test
    @Order(55)
    @DisplayName("Consistency: Total change calculation matches actual budget changes")
    void testTotalChangeConsistency() {
        // Arrange
        ArrayList<Integer> selected = createSelectedIndices(0, 1, 2);
        double originalTotal = CreatingMinistries.ministries2026[0].getBudget()
                + CreatingMinistries.ministries2026[1].getBudget() + CreatingMinistries.ministries2026[2].getBudget();

        // Act
        BulkEdit.BulkEditResult result = bulkEdit.applySelectedGui(selected, BulkEdit.ChangeMode.PERCENTAGE, 10.0,
                BulkEdit.ChangeType.INCREASE);

        // Assert
        double newTotal = CreatingMinistries.ministries2026[0].getBudget()
                + CreatingMinistries.ministries2026[1].getBudget() + CreatingMinistries.ministries2026[2].getBudget();

        assertEquals(result.totalChange, newTotal - originalTotal, 0.01,
                "Reported total change should match actual budget changes");
    }
}
