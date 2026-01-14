package FeaturesTest;

import UserFeatures.CreatingMinistries;
import UserFeatures.Edit;
import UserFeatures.Ministry;
import UserFeatures.ViewGovernmentBudget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestViewGovernmentBudget {

    private ViewGovernmentBudget view;

    @BeforeEach
    void setup() {
        view = new ViewGovernmentBudget();

        // Setup ministries for 2026
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("Ministry of Health", 3000),
                new Ministry("Ministry of Education", 2000), new Ministry("Ministry of Finance", 5000) };

        // Other years empty but initialized
        CreatingMinistries.ministries2020 = new Ministry[0];
        CreatingMinistries.ministries2021 = new Ministry[0];
        CreatingMinistries.ministries2022 = new Ministry[0];
        CreatingMinistries.ministries2023 = new Ministry[0];
        CreatingMinistries.ministries2024 = new Ministry[0];
        CreatingMinistries.ministries2025 = new Ministry[0];

        Edit.balance = 1000;
    }

    /*
     * =============================== ministryYear ===============================
     */

    @Test
    void testMinistryYearValid() {
        Ministry[] result = ViewGovernmentBudget.ministryYear(2026);
        assertNotNull(result);
        assertEquals(3, result.length);
    }

    @Test
    void testMinistryYearInvalidReturnsNull() {
        assertNull(ViewGovernmentBudget.ministryYear(2018));
    }

    /*
     * =============================== sortingBudgets ===============================
     */

    @Test
    void testSortingBudgetsByDescendingBudget() {
        Ministry[] copy = CreatingMinistries.ministries2026.clone();

        view.sortingBudgets(copy);

        assertEquals("Ministry of Finance", copy[0].getMinistryName());
        assertEquals("Ministry of Health", copy[1].getMinistryName());
        assertEquals("Ministry of Education", copy[2].getMinistryName());
    }

    @Test
    void testSortingBudgetsTieBreakAlphabetically() {
        Ministry[] arr = new Ministry[] { new Ministry("B Ministry", 1000), new Ministry("A Ministry", 1000) };

        view.sortingBudgets(arr);

        assertEquals("A Ministry", arr[0].getMinistryName());
        assertEquals("B Ministry", arr[1].getMinistryName());
    }

    /*
     * =============================== viewGovBudget ===============================
     */

    @Test
    void testViewGovBudgetCreatesFileWithoutSort() {
        view.viewGovBudget(2026, false);

        Path file = Path.of("src/main/resources/NecessaryFilesAndData/view2026.txt");
        assertTrue(Files.exists(file));
    }

    @Test
    void testViewGovBudgetCreatesFileWithSort() {
        view.viewGovBudget(2026, true);

        Path file = Path.of("src/main/resources/NecessaryFilesAndData/view2026.txt");
        assertTrue(Files.exists(file));
    }

    @Test
    void testViewGovBudgetWithZeroTotalBudgetDoesNotThrow() {
        CreatingMinistries.ministries2026 = new Ministry[] { new Ministry("Empty Ministry", 0) };

        assertDoesNotThrow(() -> view.viewGovBudget(2026, false));
    }

    @Test
    void testViewGovBudgetWithInvalidYearDoesNotThrow() {
        assertDoesNotThrow(() -> view.viewGovBudget(2019, false));
    }
}
