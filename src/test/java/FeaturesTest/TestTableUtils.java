package FeaturesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import UserFeatures.TableUtils;

public class TestTableUtils {

    @Test
    void testAppendTableRowSingleColumn() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendTableRow(sb, "Ministry of Health");

        String expected = String.format("%-60s", "Ministry of Health") + "\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testAppendTableRowMultipleColumnsAlignmentAndWidths() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendTableRow(sb, "Ministry of Health", "1000", "2000");

        String expected = String.format("%-60s", "Ministry of Health") + String.format(" %20s", "1000")
                + String.format(" %20s", "2000") + "\n";

        assertEquals(expected, sb.toString());
    }

    @Test
    void testAppendSeparatorCreatesCorrectLine() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendSeparator(sb, 10, '=');

        assertEquals("==========\n", sb.toString());
    }

    @Test
    void testAppendSeparatorWithDifferentCharacter() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendSeparator(sb, 5, '-');

        assertEquals("-----\n", sb.toString());
    }

    @Test
    void testAppendTitleCentersUsingImplementedPaddingLogic() {
        StringBuilder sb = new StringBuilder();

        String title = "BUDGET 2026";
        int width = 40;

        // padding = (width + title.length()) / 2
        int padding = (width + title.length()) / 2;
        String expected = String.format("%" + padding + "s", title) + "\n";

        TableUtils.appendTitle(sb, title, width);

        assertEquals(expected, sb.toString());
    }

    @Test
    void testAppendTableRowCustomSingleColumn() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendTableRowCustom(sb, 10, 5, "ABC");

        String expected = String.format("%-10s", "ABC") + "\n";
        assertEquals(expected, sb.toString());
    }

    @Test
    void testAppendTableRowCustomMultipleColumnsAlignmentAndWidths() {
        StringBuilder sb = new StringBuilder();

        TableUtils.appendTableRowCustom(sb, 10, 5, "ABC", "1", "22");

        String expected = String.format("%-10s", "ABC") + String.format(" %5s", "1") + String.format(" %5s", "22")
                + "\n";

        assertEquals(expected, sb.toString());

        // Total length: 10 + (1+5)*2 + 1 newline = 23
        assertEquals(23, sb.length());
    }
}
