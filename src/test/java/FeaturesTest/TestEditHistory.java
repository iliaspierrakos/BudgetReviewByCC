package FeaturesTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.EditHistory;
import UserFeatures.Ministry;
import UserFeatures.TableUtils;

/**
 * Unit tests for the {@link EditHistory} class.
 *
 * <p>
 * These tests verify the correct behavior of the {@link EditHistory#historyOfEdit(String, double, double, int)} method,
 * ensuring that:
 * </p>
 *
 * <ul>
 * <li>The edit history file is created when it does not exist</li>
 * <li>The table header is written only once</li>
 * <li>Subsequent edits are correctly appended</li>
 * <li>The "New Change" title is conditionally added when {@code type == 0}</li>
 * </ul>
 *
 * <p>
 * The tests operate on the real file path used by the application and reset the file state before each test to ensure
 * isolation and repeatability.
 * </p>
 */
public class TestEditHistory {

    /**
     * Path to the edit history file.
     * <p>
     * This must match the path defined in {@link EditHistory} exactly, as the production code writes directly to this
     * location.
     * </p>
     */
    private static final String HISTORY_FILE = "src/main/resources/NecessaryFilesAndData/edithistory.txt";

    /** Resolved {@link Path} object for the history file. */
    private Path historyPath;

    /**
     * Prepares a clean test environment before each test.
     *
     * <p>
     * This method:
     * </p>
     * <ul>
     * <li>Ensures all parent directories exist</li>
     * <li>Deletes the history file if it already exists</li>
     * </ul>
     *
     * @throws IOException
     *             if file system operations fail
     */
    @BeforeEach
    void setup() throws IOException {
        historyPath = Paths.get(HISTORY_FILE);

        Path parent = historyPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.deleteIfExists(historyPath);
    }

    /**
     * Verifies that the first edit written to an empty or non-existent file:
     *
     * <ul>
     * <li>Creates the history file</li>
     * <li>Writes the table header</li>
     * <li>Appends exactly one edit row</li>
     * </ul>
     *
     * @throws IOException
     *             if the file cannot be read
     */
    @Test
    void testFirstWriteCreatesHeaderAndRow() throws IOException {
        String ministryName = "Ministry of Health";
        double previous = 1000;
        double next = 1100;

        EditHistory.historyOfEdit(ministryName, previous, next, 1);

        assertTrue(Files.exists(historyPath), "Edit history file should be created on first write");

        String actual = Files.readString(historyPath, StandardCharsets.UTF_8);
        String expected = buildExpectedFirstEntry(ministryName, previous, next);

        assertEquals(expected, actual);
    }

    /**
     * Verifies that when the history file already exists and {@code type != 0}, a new edit is appended without
     * inserting a "New Change" title.
     *
     * @throws IOException
     *             if the file cannot be read
     */
    @Test
    void testAppendWithoutTypeZeroDoesNotAddNewChangeTitle() throws IOException {
        String ministryName = "Ministry of Health";

        EditHistory.historyOfEdit(ministryName, 1000, 1100, 1);
        EditHistory.historyOfEdit(ministryName, 1100, 900, 1);

        String actual = Files.readString(historyPath, StandardCharsets.UTF_8);

        String expected = buildExpectedFirstEntry(ministryName, 1000, 1100)
                + buildExpectedAppendRowOnly(ministryName, 1100, 900);

        assertEquals(expected, actual);
    }

    /**
     * Verifies that when {@code type == 0} and the file already exists, a centered "New Change" title is inserted
     * before the appended edit row.
     *
     * @throws IOException
     *             if the file cannot be read
     */
    @Test
    void testAppendWithTypeZeroAddsNewChangeTitleThenRow() throws IOException {
        String ministryName = "Ministry of Health";

        EditHistory.historyOfEdit(ministryName, 1000, 1100, 1);
        EditHistory.historyOfEdit(ministryName, 1100, 1200, 0);

        String actual = Files.readString(historyPath, StandardCharsets.UTF_8);

        String expected = buildExpectedFirstEntry(ministryName, 1000, 1100)
                + buildExpectedAppendWithNewChangeTitle(ministryName, 1100, 1200);

        assertEquals(expected, actual);
    }

    // -------------------------------------------------------------------------
    // Helper methods for constructing expected output using production utilities
    // -------------------------------------------------------------------------

    /**
     * Builds the expected file content for the very first edit entry, including table headers and separators.
     */
    private static String buildExpectedFirstEntry(String ministryName, double prev, double next) {

        StringBuilder sb = new StringBuilder();

        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTitle(sb, "RECENT CHANGES", 120);
        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTableRow(sb, "MINISTRY", "PREVIOUS BUDGET", "NEW BUDGET");
        TableUtils.appendSeparator(sb, 120, '-');

        TableUtils.appendTableRow(sb, ministryName, Ministry.getFormattedBudget(prev),
                Ministry.getFormattedBudget(next));

        return sb.toString();
    }

    /**
     * Builds the expected output for a simple appended edit row without any additional headers.
     */
    private static String buildExpectedAppendRowOnly(String ministryName, double prev, double next) {

        StringBuilder sb = new StringBuilder();

        TableUtils.appendTableRow(sb, ministryName, Ministry.getFormattedBudget(prev),
                Ministry.getFormattedBudget(next));

        return sb.toString();
    }

    /**
     * Builds the expected output when a "New Change" title precedes an appended edit row.
     */
    private static String buildExpectedAppendWithNewChangeTitle(String ministryName, double prev, double next) {

        StringBuilder sb = new StringBuilder();

        TableUtils.appendTitle(sb, "========== New Change ==========", 120);
        TableUtils.appendTableRow(sb, ministryName, Ministry.getFormattedBudget(prev),
                Ministry.getFormattedBudget(next));

        return sb.toString();
    }
}
