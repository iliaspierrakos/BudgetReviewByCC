package guiFolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EditHistoryScreenTest {

    private Path createdHistoryFile;

    @Test
    void historyRow_gettersReturnConstructorValues() {
        EditHistoryScreen.HistoryRow row =
                new EditHistoryScreen.HistoryRow("Health", "1.000", "2.000", "+1.000");

        assertEquals("Health", row.getMinistry());
        assertEquals("1.000", row.getPrevious());
        assertEquals("2.000", row.getNow());
        assertEquals("+1.000", row.getDelta());
    }

    @Test
    void parseGreekNumber_removesDotsAndCommas() throws Exception {
        EditHistoryScreen screen = new EditHistoryScreen(null, null);

        long n1 = parseGreekNumber(screen, "1.234.567");
        long n2 = parseGreekNumber(screen, "1,234,567");
        long n3 = parseGreekNumber(screen, "  12.345  ");

        assertEquals(1_234_567L, n1);
        assertEquals(1_234_567L, n2);
        assertEquals(12_345L, n3);
    }

    @Test
    void formatGreekNumber_addsDotsEveryThreeDigits() throws Exception {
        EditHistoryScreen screen = new EditHistoryScreen(null, null);

        assertEquals("0", formatGreekNumber(screen, 0));
        assertEquals("12", formatGreekNumber(screen, 12));
        assertEquals("1.234", formatGreekNumber(screen, 1234));
        assertEquals("1.234.567", formatGreekNumber(screen, 1_234_567));
    }

    @Test
    void calcDelta_returnsPlusMinusOrZero() throws Exception {
        EditHistoryScreen screen = new EditHistoryScreen(null, null);

        assertEquals("+1.000", calcDelta(screen, "1.000", "2.000"));
        assertEquals("-500", calcDelta(screen, "2.000", "1.500"));
        assertEquals("0", calcDelta(screen, "1.000", "1.000"));
    }

    @Test
    void calcDelta_invalidInput_returnsEmptyString() throws Exception {
        EditHistoryScreen screen = new EditHistoryScreen(null, null);

        assertEquals("", calcDelta(screen, "abc", "1.000"));
        assertEquals("", calcDelta(screen, "1.000", "xyz"));
        assertEquals("", calcDelta(screen, "", ""));
    }

    @Test
    void parseHistoryFile_readsValidLines_andSkipsHeaders() throws Exception {
        EditHistoryScreen screen = new EditHistoryScreen(null, null);

        Path historyPath = get_HISTORY_PATH();
        Files.createDirectories(historyPath.getParent());r
        List<String> lines = List.of(
                "RECENT CHANGES",
                "====================",
                "MINISTRY  PREVIOUS  NEW",
                "--------------------",
                "Health  1.000  2.000",
                "Education    10.000    9.500",          
                "   ",                                  
                "Transport  5.000  5.000"               
        );

        Files.write(historyPath, lines);
        createdHistoryFile = historyPath;

        List<?> rows = parseHistoryFile(screen);
        assertEquals(3, rows.size());

        EditHistoryScreen.HistoryRow r0 = (EditHistoryScreen.HistoryRow) rows.get(0);
        assertEquals("Health", r0.getMinistry());
        assertEquals("1.000", r0.getPrevious());
        assertEquals("2.000", r0.getNow());
        assertEquals("+1.000", r0.getDelta());

        EditHistoryScreen.HistoryRow r1 = (EditHistoryScreen.HistoryRow) rows.get(1);
        assertEquals("Education", r1.getMinistry());
        assertEquals("10.000", r1.getPrevious());
        assertEquals("9.500", r1.getNow());
        assertEquals("-500", r1.getDelta());

        EditHistoryScreen.HistoryRow r2 = (EditHistoryScreen.HistoryRow) rows.get(2);
        assertEquals("Transport", r2.getMinistry());
        assertEquals("5.000", r2.getPrevious());
        assertEquals("5.000", r2.getNow());
        assertEquals("0", r2.getDelta());
    }

    @AfterEach
    void cleanup() throws Exception {
        if (createdHistoryFile != null) {
            Files.deleteIfExists(createdHistoryFile);
            createdHistoryFile = null;
        }
    }

    private Path get_HISTORY_PATH() throws Exception {
        Field f = EditHistoryScreen.class.getDeclaredField("HISTORY_PATH");
        f.setAccessible(true);
        return (Path) f.get(null);
    }

    @SuppressWarnings("unchecked")
    private List<EditHistoryScreen.HistoryRow> parseHistoryFile(EditHistoryScreen screen) throws Exception {
        Method m = EditHistoryScreen.class.getDeclaredMethod("parseHistoryFile");
        m.setAccessible(true);
        return (List<EditHistoryScreen.HistoryRow>) m.invoke(screen);
    }

    private String calcDelta(EditHistoryScreen screen, String prev, String now) throws Exception {
        Method m = EditHistoryScreen.class.getDeclaredMethod("calcDelta", String.class, String.class);
        m.setAccessible(true);
        return (String) m.invoke(screen, prev, now);
    }

    private long parseGreekNumber(EditHistoryScreen screen, String s) throws Exception {
        Method m = EditHistoryScreen.class.getDeclaredMethod("parseGreekNumber", String.class);
        m.setAccessible(true);
        return (long) m.invoke(screen, s);
    }

    private String formatGreekNumber(EditHistoryScreen screen, long n) throws Exception {
        Method m = EditHistoryScreen.class.getDeclaredMethod("formatGreekNumber", long.class);
        m.setAccessible(true);
        return (String) m.invoke(screen, n);
    }
}
