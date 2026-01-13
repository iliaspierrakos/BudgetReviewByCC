package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import UserFeatures.ViewEditBudget;
import UserManagement.Citizen;
import UserManagement.MinistryMember;
import UserManagement.User;

/**
 * Unit tests for {@link ViewEditBudget}.
 *
 * <p>
 * This test suite focuses on deterministic behavior and exception-based control flow.
 * It intentionally avoids branches that trigger interactive console I/O or GUI-based flows
 * (e.g. {@code Edit.collectData()}, {@code BulkEdit.bulkEditMenu()},
 * {@code GovernorCheck.viewProposalsNames()}).
 * </p>
 *
 * <p>
 * The goal is to increase coverage safely by testing:
 * </p>
 * <ul>
 *   <li>{@link ViewEditBudget#viewBudget(int)} year routing and invalid year handling</li>
 *   <li>{@link ViewEditBudget#edit(User, int)} input validation and MinistryMember unsupported bulk edit</li>
 *   <li>{@link ViewEditBudget#recommendations(User)} unsupported role path</li>
 *   <li>{@link ViewEditBudget#taxReceipt(User)} role guarding and GUI-based unsupported operation</li>
 *   <li>{@link ViewEditBudget#resetAll()} only in a non-destructive environment</li>
 * </ul>
 */
public class TestViewEditBudget {

    /**
     * Resets the static ministry arrays used by {@link ViewEditBudget#viewBudget(int)}.
     * This makes the routing tests deterministic.
     */
    @BeforeEach
    void initMinistries() {
        CreatingMinistries.ministries2020 = new Ministry[] { new Ministry("M2020", 1) };
        CreatingMinistries.ministries2021 = new Ministry[] { new Ministry("M2021", 1) };
        CreatingMinistries.ministries2022 = new Ministry[] { new Ministry("M2022", 1) };
        CreatingMinistries.ministries2023 = new Ministry[] { new Ministry("M2023", 1) };
        CreatingMinistries.ministries2024 = new Ministry[] { new Ministry("M2024", 1) };
        CreatingMinistries.ministries2025 = new Ministry[] { new Ministry("M2025", 1) };
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("M2026", 1) };
    }

    /**
     * Verifies that {@link ViewEditBudget#viewBudget(int)} returns the correct ministry array
     * for a valid year.
     */
    @Test
    void testViewBudgetValidYearReturnsCorrectArray() {
        Ministry[] result = ViewEditBudget.viewBudget(2026);
        assertSame(CreatingMinistries.ministries2026, result,
                "viewBudget(2026) should return ministries2026 array");
    }

    /**
     * Verifies that {@link ViewEditBudget#viewBudget(int)} rejects invalid years with an exception.
     */
    @Test
    void testViewBudgetInvalidYearThrowsIllegalArgument() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ViewEditBudget.viewBudget(1999),
                "Invalid year should throw IllegalArgumentException"
        );

        assertTrue(ex.getMessage().contains("Invalid year"),
                "Exception message should mention invalid year");
    }

    /**
     * Verifies that {@link ViewEditBudget#edit(User, int)} rejects invalid edit types.
     */
    @Test
    void testEditInvalidEditTypeThrowsIllegalArgument() {
        User citizen = new Citizen("c1", "pw");

        assertThrows(
                IllegalArgumentException.class,
                () -> ViewEditBudget.edit(citizen, 0),
                "editType outside [1..3] should throw IllegalArgumentException"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> ViewEditBudget.edit(citizen, 4),
                "editType outside [1..3] should throw IllegalArgumentException"
        );
    }

    /**
     * Verifies that MinistryMember bulk edit is explicitly unsupported.
     *
     * <p>
     * This path is deterministic and does not trigger interactive menus.
     * </p>
     */
    @Test
    void testEditMinistryMemberBulkEditThrowsUnsupportedOperation() {
        User mm = new MinistryMember("mm1", "pw", "Ministry of Health");

        assertThrows(
                UnsupportedOperationException.class,
                () -> ViewEditBudget.edit(mm, 2),
                "Bulk edit (type 2) is not supported for MinistryMember"
        );
    }

    /**
     * Verifies that {@link ViewEditBudget#recommendations(User)} throws for MinistryMember,
     * since this flow is GUI-based.
     */
    @Test
    void testRecommendationsMinistryMemberThrowsUnsupportedOperation() {
        User mm = new MinistryMember("mm2", "pw", "Ministry of Finance");

        assertThrows(
                UnsupportedOperationException.class,
                () -> ViewEditBudget.recommendations(mm),
                "MinistryMember recommendations should throw UnsupportedOperationException"
        );
    }

    /**
     * Verifies that {@link ViewEditBudget#recommendations(User)} is a no-op for citizens
     * (no exception is thrown).
     */
    @Test
    void testRecommendationsCitizenDoesNotThrow() {
        User citizen = new Citizen("c2", "pw");
        assertDoesNotThrow(() -> ViewEditBudget.recommendations(citizen),
                "Citizen recommendations path should be a no-op and not throw");
    }

    /**
     * Verifies that {@link ViewEditBudget#taxReceipt(User)} rejects non-citizens.
     */
    @Test
    void testTaxReceiptNonCitizenThrowsIllegalState() {
        User mm = new MinistryMember("mm3", "pw", "Ministry of Education");

        assertThrows(
                IllegalStateException.class,
                () -> ViewEditBudget.taxReceipt(mm),
                "Only citizens can view tax receipts"
        );
    }

    /**
     * Verifies that {@link ViewEditBudget#taxReceipt(User)} throws UnsupportedOperationException
     * for citizens, since the feature is GUI-based.
     */
    @Test
    void testTaxReceiptCitizenThrowsUnsupportedOperation() {
        User citizen = new Citizen("c3", "pw");

        assertThrows(
                UnsupportedOperationException.class,
                () -> ViewEditBudget.taxReceipt(citizen),
                "Tax receipt is GUI-based and should throw UnsupportedOperationException"
        );
    }

    /**
     * Verifies that {@link ViewEditBudget#resetAll()} resets the internal initialization flag.
     *
     * <p>
     * WARNING: {@code resetAll()} deletes files under fixed project paths:
     * {@code src/main/java/NecessaryFilesAndData} and {@code Data}.
     * This test runs only when those folders do NOT exist, to avoid destructive behavior.
     * </p>
     */
    @Test
    void testResetAllResetsInitializedFlagNonDestructively() throws Exception {
        Path folder1 = Path.of("src", "main", "java", "NecessaryFilesAndData");
        Path folder2 = Path.of("Data");

        // Skip test if running in an environment where these directories exist,
        // to avoid deleting real project data.
        Assumptions.assumeFalse(Files.exists(folder1),
                "Skipping resetAll test because src/main/java/NecessaryFilesAndData exists");
        Assumptions.assumeFalse(Files.exists(folder2),
                "Skipping resetAll test because Data exists");

        // Set initialized=true via reflection, then resetAll should set it to false
        Field initField = ViewEditBudget.class.getDeclaredField("initialized");
        initField.setAccessible(true);
        initField.setBoolean(null, true);

        ViewEditBudget.resetAll();

        assertFalse(initField.getBoolean(null),
        "resetAll() should reset initialized flag to false");
    }
}
