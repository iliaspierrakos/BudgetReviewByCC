package guiFolder;

import UserFeatures.CreatingMinistries;
import UserFeatures.Ministry;
import org.junit.jupiter.api.*;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ViewBudgetScreenTest {

    @BeforeEach
    void setUp() {
        try {
            CreatingMinistries.loadGovernorDraft(2026);
        } catch (Exception ignored) {
            try {
                Path csv = Path.of("src/main/resources/NecessaryFilesAndData/MinistriesBudgets2026.csv");
                if (Files.exists(csv)) {
                    CreatingMinistries.loadOfficialBudgets(2026);
                }
            } catch (Exception ignored2) {}
        }
    }

    @Test
    void sortingBudgets_sortsByBudgetDesc_andThenByName() throws Exception {
        ViewBudgetScreen screen = new ViewBudgetScreen(null, null);

        Ministry[] arr = new Ministry[6];
        arr[0] = new Ministry("B Ministry", 100);
        arr[1] = new Ministry("A Ministry", 100);
        arr[2] = new Ministry("Big", 500);
        arr[3] = null;
        arr[4] = new Ministry("Small", 10);
        arr[5] = null;

        call_sortingBudgets(screen, arr);

        assertEquals("Big", arr[0].getMinistryName());

        assertEquals("A Ministry", arr[1].getMinistryName());
        assertEquals("B Ministry", arr[2].getMinistryName());

        assertEquals("Small", arr[3].getMinistryName());
        assertNull(arr[4]);
        assertNull(arr[5]);
    }

    @Test
    void getGovBudgetRows_returnsRowsAndAddsTotalRow() throws Exception {
        ViewBudgetScreen screen = new ViewBudgetScreen(null, null);

        List<ViewBudgetScreen.GovBudgetRow> rows = call_getGovBudgetRows(screen, 2026, false);

        assertNotNull(rows);
        assertTrue(rows.size() >= 2);

        ViewBudgetScreen.GovBudgetRow last = rows.get(rows.size() - 1);
        assertEquals("TOTAL", last.getMinistry());
        assertEquals("100,00%", last.getPercentText());

        for (ViewBudgetScreen.GovBudgetRow r : rows) {
            assertNotNull(r.getMinistry());
            assertNotNull(r.getBudgetText());
            assertNotNull(r.getPercentText());
            assertTrue(r.getPercentText().endsWith("%") || r.getMinistry().equals("TOTAL"));
        }
    }

    @Test
    void getGovBudgetRows_whenSortIsTrue_firstBudgetsAreDescending_exceptTotal() throws Exception {
        ViewBudgetScreen screen = new ViewBudgetScreen(null, null);

        List<ViewBudgetScreen.GovBudgetRow> rows = call_getGovBudgetRows(screen, 2026, true);

        assertEquals("TOTAL", rows.get(rows.size() - 1).getMinistry());

        int checks = Math.min(6, end - 1);

        for (int i = 0; i < checks; i++) {
            double b1 = parseBudget(rows.get(i).getBudgetText());
            double b2 = parseBudget(rows.get(i + 1).getBudgetText());
            assertTrue(b1 >= b2, "Not sorted desc at index " + i);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<ViewBudgetScreen.GovBudgetRow> call_getGovBudgetRows(ViewBudgetScreen screen, int year, boolean sort) throws Exception {
        Method m = ViewBudgetScreen.class.getDeclaredMethod("getGovBudgetRows", int.class, boolean.class);
        m.setAccessible(true);
        return (List<ViewBudgetScreen.GovBudgetRow>) m.invoke(screen, year, sort);
    }

    private static void call_sortingBudgets(ViewBudgetScreen screen, Ministry[] ministries) throws Exception {
        Method m = ViewBudgetScreen.class.getDeclaredMethod("sortingBudgets", Ministry[].class);
        m.setAccessible(true);
        m.invoke(screen, (Object) ministries);
    }


    private static double parseBudget(String s) {
        if (s == null) return 0.0;

        String cleaned = s.replaceAll("[^0-9,\\.]", "");

        cleaned = cleaned.replace(".", "");
        cleaned = cleaned.replace(",", ".");

        if (cleaned.isBlank()) return 0.0;
        return Double.parseDouble(cleaned);
    }
}
