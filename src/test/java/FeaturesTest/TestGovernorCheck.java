package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.GovernorCheck;

/**
 * Unit tests for {@link GovernorCheck}.
 *
 * <p>
 * These tests validate console-driven logic in a deterministic way by:
 * </p>
 * <ul>
 *   <li>Injecting scripted input into the internal {@link Scanner}</li>
 *   <li>Capturing {@code System.out} to verify user-facing messages</li>
 *   <li>Creating and cleaning proposal files under the same path used by production code</li>
 * </ul>
 *
 * <p>
 * Note: The "accept" path triggers {@code new EditHistoryList().applyingEdits()},
 * which can have broad side effects. Therefore, tests focus on the "reject"
 * path and input validation logic.
 * </p>
 */
public class TestGovernorCheck {

    private static final Path PROPOSALS_DIR = Path.of(
            "src", "main", "resources", "NecessaryFilesAndData", "ProposalsFromMinisters"
    );

    private PrintStream originalOut;
    private ByteArrayOutputStream outBuffer;

    @BeforeEach
    void setup() throws Exception {
        // Capture stdout
        originalOut = System.out;
        outBuffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outBuffer, true, StandardCharsets.UTF_8));

        // Ensure proposals directory exists and is empty
        if (Files.exists(PROPOSALS_DIR)) {
            try (var stream = Files.list(PROPOSALS_DIR)) {
                stream.forEach(p -> {
                    try { Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            }
        } else {
            Files.createDirectories(PROPOSALS_DIR);
        }
    }

    @AfterEach
    void teardown() {
        System.setOut(originalOut);
    }

    /**
     * Verifies that {@link GovernorCheck#budgetChecking()} returns {@code true}
     * when the user answers "yes" (case-insensitive).
     */
    @Test
    void testBudgetCheckingYesReturnsTrue() throws Exception {
        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, "YES\n");

        boolean result = gc.budgetChecking();

        assertTrue(result, "Expected budgetChecking() to return true for 'yes'");
    }

    /**
     * Verifies that {@link GovernorCheck#budgetChecking()} returns {@code false}
     * when the user answers "no" (case-insensitive).
     */
    @Test
    void testBudgetCheckingNoReturnsFalse() throws Exception {
        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, "no\n");

        boolean result = gc.budgetChecking();

        assertFalse(result, "Expected budgetChecking() to return false for 'no'");
    }

    /**
     * Verifies that {@link GovernorCheck#budgetChecking()} keeps prompting until
     * a valid answer ("yes" or "no") is given.
     */
    @Test
    void testBudgetCheckingLoopsUntilValidInput() throws Exception {
        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, "maybe\nok\nYeS\n");

        boolean result = gc.budgetChecking();

        assertTrue(result, "Expected budgetChecking() to eventually accept 'yes' and return true");

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Please type yes or no"),
                "Expected a prompt for invalid inputs");
    }

    /**
     * Verifies that rejecting a proposal deletes the corresponding file.
     */
    @Test
    void testFileManagementRejectDeletesFile() throws Exception {
        GovernorCheck gc = new GovernorCheck();

        String fileName = "proposal1";
        Path proposalFile = PROPOSALS_DIR.resolve(fileName + ".txt");
        Files.writeString(proposalFile, "dummy\n", StandardCharsets.UTF_8);

        assertTrue(Files.exists(proposalFile), "Precondition: proposal file should exist");

        gc.fileManagement(false, fileName);

        assertFalse(Files.exists(proposalFile), "Rejected proposal should be deleted");
        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("File deleted successfully"),
                "Expected success deletion message");
    }

    /**
     * Verifies that when the proposals folder does not exist, the UI reports it as
     * "Folder not found or empty."
     */
    @Test
    void testViewProposalsNamesFolderNotFoundOrEmptyMessage() throws Exception {
        // Remove the folder to force folder.listFiles() == null or empty folder case depending on OS/JDK
        // In your code, "files == null" prints: "Folder not found or empty."
        // So we delete directory if possible.
        try (var stream = Files.list(PROPOSALS_DIR)) {
            stream.forEach(p -> {
                try { Files.deleteIfExists(p); } catch (Exception ignored) {}
            });
        }
        Files.deleteIfExists(PROPOSALS_DIR);

        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, "\n"); // not used in this branch

        gc.viewProposalsNames();

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Folder not found or empty."),
                "Expected 'Folder not found or empty.' when directory is missing");
    }

    /**
     * Verifies that when the proposals folder exists but contains no files,
     * the UI reports "Folder empty."
     */
    @Test
    void testViewProposalsNamesEmptyFolderPrintsFolderEmpty() throws Exception {
        // Ensure directory exists and is empty (setup already does this)
        assertTrue(Files.exists(PROPOSALS_DIR), "Precondition: proposals directory should exist");
        try (var stream = Files.list(PROPOSALS_DIR)) {
            assertEquals(0, stream.count(), "Precondition: proposals directory should be empty");
        }

        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, "\n"); // not used in this branch

        gc.viewProposalsNames();

        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Folder empty."),
                "Expected 'Folder empty.' when directory exists but has no files");
    }

    /**
     * Verifies the end-to-end "reject flow" via {@link GovernorCheck#viewProposalsNames()}:
     * <ul>
     *   <li>Lists a proposal file</li>
     *   <li>Accepts user selection</li>
     *   <li>Displays file contents</li>
     *   <li>Rejects changes ("no")</li>
     *   <li>Deletes the proposal file</li>
     * </ul>
     */
    @Test
    void testViewProposalsNamesRejectFlowDeletesSelectedFile() throws Exception {
        // Arrange: create a proposal file
        String fileName = "proposalX";
        Path proposalFile = PROPOSALS_DIR.resolve(fileName + ".txt");
        Files.writeString(proposalFile, "LINE1\nLINE2\n", StandardCharsets.UTF_8);

        assertTrue(Files.exists(proposalFile), "Precondition: proposal file should exist");

        // User selects fileName (without .txt), then answers "no" to reject
        GovernorCheck gc = new GovernorCheck();
        injectScanner(gc, fileName + "\nno\n");

        // Act
        gc.viewProposalsNames();

        ///or Assert
        String printed = outBuffer.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains(fileName + ".txt"), "Expected file to be listed by name");
        assertTrue(printed.contains("LINE1"), "Expected proposal content to be printed");
        assertTrue(printed.contains("Would you like to accept the changes?"),
                "Expected prompt to accept/reject changes");

        assertFalse(Files.exists(proposalFile), "Rejected proposal should be deleted");
    }

    // ----------------- helper -----------------

    /**
     * Replaces the internal {@code scanner} field with a scripted scanner,
     * enabling deterministic tests without real console input.
     *
     * @param gc the {@link GovernorCheck} instance
     * @param inputScript newline-separated answers
     */
    private static void injectScanner(GovernorCheck gc, String inputScript) throws Exception {
        Field f = GovernorCheck.class.getDeclaredField("scanner");
        f.setAccessible(true);
        f.set(gc, new Scanner(inputScript));
    }
}
