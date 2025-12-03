package UserFeatures;

/**
 * Utility class for creating formatted table outputs.
 * Provides reusable methods for table formatting used across View, Compare, and BulkEdit.
 */
public class TableUtils {
    
    /**
     * Appends a table row with aligned columns to the StringBuilder.
     * First column is left-aligned (60 chars), remaining columns are right-aligned (20 chars each).
     * 
     * @param sb The StringBuilder to append to
     * @param columns Variable number of column values (first column left-aligned, rest right-aligned)
     */
    public static void appendTableRow(StringBuilder sb, String... columns) {
        for (int i = 0; i < columns.length; i++) {
            if (i == 0) {
                // First column: left-aligned, 60 characters
                sb.append(String.format("%-60s", columns[i]));
            } else {
                // Remaining columns: right-aligned, 20 characters with leading space
                sb.append(String.format(" %20s", columns[i]));
            }
        }
        sb.append("\n");
    }
    
    /**
     * Appends a separator line (decorative line of repeated characters).
     * Used for creating visual separation between sections of a table.
     * @param sb The StringBuilder to append to
     * @param width Total width of the separator line
     * @param character The character to repeat (e.g., '=' for header/footer, '-' for section divider)
     */
    public static void appendSeparator(StringBuilder sb, int width, char character) {
        sb.append(String.valueOf(character).repeat(width)).append("\n");
    }
    
    /**
     * Appends a centered title.
     * Uses right-alignment with calculated padding to achieve centering effect.
     * @param sb The StringBuilder to append to
     * @param title The title text to center
     * @param width Total width for centering
     */
    public static void appendTitle(StringBuilder sb, String title, int width) {
        int padding = (width + title.length()) / 2;
        sb.append(String.format("%" + padding + "s", title)).append("\n");
    }
    
    /**
     * Appends a table row with custom column widths.
     * Used primarily in BulkEdit preview tables which need wider columns.
     * @param sb The StringBuilder to append to
     * @param firstColumnWidth Width of the first column
     * @param otherColumnWidth Width of all other columns
     * @param columns Variable number of column values
     */
    public static void appendTableRowCustom(StringBuilder sb, int firstColumnWidth, 
                                           int otherColumnWidth, String... columns) {
        for (int i = 0; i < columns.length; i++) {
            if (i == 0) {
                // First column: left-aligned, custom width
                sb.append(String.format("%-" + firstColumnWidth + "s", columns[i]));
            } else {
                // Remaining columns: right-aligned, custom width with leading space
                sb.append(String.format(" %" + otherColumnWidth + "s", columns[i]));
            }
        }
        sb.append("\n");
    }
}