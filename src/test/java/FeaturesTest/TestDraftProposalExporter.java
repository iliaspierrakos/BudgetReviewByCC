package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.DraftProposalExporter;

/**
 * Unit tests for {@link DraftProposalExporter}.
 *
 * <p>
 * This test suite verifies deterministic, UI-independent helper logic
 * implemented inside {@code DraftProposalExporter}.
 * </p>
 *
 * <p>
 * The public method {@code exportAndNotify(...)} is intentionally excluded
 * from testing, as it depends on JavaFX dialogs and user interaction.
 * </p>
 */
public class TestDraftProposalExporter {

    private Method normalizeChange;
    private Method safeToken;
    private Method formatHuman;
    private Method resolveProposalsDir;

    /**
     * Loads private static helper methods via reflection
     * so they can be tested in isolation.
     */
    @BeforeEach
    void setupReflection() {
        try {
            normalizeChange = DraftProposalExporter.class
                .getDeclaredMethod("normalizeChange", String.class);
            normalizeChange.setAccessible(true);

            safeToken = DraftProposalExporter.class
                .getDeclaredMethod("safeToken", String.class, String.class);
            safeToken.setAccessible(true);

            formatHuman = DraftProposalExporter.class
                .getDeclaredMethod("formatHuman", double.class);
            formatHuman.setAccessible(true);

            resolveProposalsDir = DraftProposalExporter.class
                .getDeclaredMethod("resolveProposalsDir");
            resolveProposalsDir.setAccessible(true);
        } catch (Exception e) {
            fail("Failed to access private helper methods via reflection");
        }
    }

    /**
     * Verifies that change types are normalized consistently
     * to either {@code "Increase"} or {@code "Decrease"}.
     */
    @Test
    void testNormalizeChange() throws Exception {
        assertEquals(
        "Increase",
        (String) normalizeChange.invoke(null, (Object) null),
        "Null change type should default to Increase"
        );

        assertEquals(
        "Increase",
        (String) normalizeChange.invoke(null, " increase "),
        "Increase should normalize to Increase"
        );

        assertEquals(
        "Decrease",
        (String) normalizeChange.invoke(null, "Decrease"),
        "Decrease should normalize to Decrease"
        );

        assertEquals(
        "Decrease",
        (String) normalizeChange.invoke(null, "dec"),
        "Prefix 'dec' should normalize to Decrease"
        );

        assertEquals(
        "Decrease",
        (String) normalizeChange.invoke(null, "Decreasing"),
        "Words starting with 'dec' should normalize to Decrease"
        );

        assertEquals(
        "Increase",
        (String) normalizeChange.invoke(null, "something else"),
        "Unknown values should normalize to Increase"
        );
    }

    /**
     * Ensures that file-name tokens are safe and deterministic:
     * <ul>
     *   <li>Null or blank values use the fallback</li>
     *   <li>Non-alphanumeric characters are replaced with underscores</li>
     *   <li>Output length is capped</li>
     * </ul>
     */
    @Test
    void testSafeToken() throws Exception {
        assertEquals(
        "unknown",
        (String) safeToken.invoke(null, null, "unknown"),
        "Null input should return fallback"
        );

        assertEquals(
        "UnknownMinistry",
        (String) safeToken.invoke(null, "   ", "UnknownMinistry"),
        "Blank input should return fallback"
        );

        assertEquals(
        "John_Doe_2026",
        (String) safeToken.invoke(null, "John Doe! 2026", "x"),
        "Non-alphanumeric characters should be replaced with underscores"
        );

        String longInput =
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 60 chars
        String out = (String) safeToken.invoke(null, longInput, "x");

        assertTrue(
        out.length() <= 40,
        "Token length must be capped at 40 characters"
        );
    }

    /**
     * Verifies that numeric values are formatted for human readability
     * without currency symbols and without leading/trailing whitespace.
     */
    @Test
    void testFormatHumanRemovesEuroSymbol() throws Exception {
        String out = (String) formatHuman.invoke(null, 1234.56);

        assertNotNull(out, "Formatted output should not be null");
        assertFalse(out.contains("€"),
        "Formatted output should not contain the euro symbol");
        assertEquals(out.trim(), out,
        "Formatted output should be trimmed");
    }

    /**
     * Ensures that the proposals directory resolver returns
     * the first existing directory from the candidate list.
     */
    @Test
    void testResolveProposalsDirReturnsExistingDirectory() throws Exception {
        Path base = Files.createTempDirectory("proposalDirs");
        Path missing = base.resolve("missingDir");
        Path existing = Files.createDirectory(base.resolve("existingDir"));
        Path alsoExisting = Files.createDirectory(base.resolve("alsoExistingDir"));

        Field dirsField =
        DraftProposalExporter.class.getDeclaredField("CANDIDATE_DIRS");
        dirsField.setAccessible(true);
        Path[] dirs = (Path[]) dirsField.get(null);

        Path d0 = dirs[0], d1 = dirs[1], d2 = dirs[2];
        try {
            dirs[0] = missing;
            dirs[1] = existing;
            dirs[2] = alsoExisting;

            Path resolved = (Path) resolveProposalsDir.invoke(null);

            assertEquals(
                existing,
                resolved,
                "Should return the first directory that exists"
            );
        } finally {
            dirs[0] = d0;
            dirs[1] = d1;
            dirs[2] = d2;
        }
    }

    /**
     * Ensures that when no candidate directory exists,
     * the resolver falls back to the first candidate.
     */
    @Test
        void testResolveProposalsDirFallsBackToDefault() throws Exception {
        Path base = Files.createTempDirectory("proposalDirs2");
        Path missing1 = base.resolve("missing1");
        Path missing2 = base.resolve("missing2");
        Path missing3 = base.resolve("missing3");

        Field dirsField =
        DraftProposalExporter.class.getDeclaredField("CANDIDATE_DIRS");
        dirsField.setAccessible(true);
        Path[] dirs = (Path[]) dirsField.get(null);

        Path d0 = dirs[0], d1 = dirs[1], d2 = dirs[2];
        try {
            dirs[0] = missing1;
            dirs[1] = missing2;
            dirs[2] = missing3;

            Path resolved = (Path) resolveProposalsDir.invoke(null);

            assertEquals(
                missing1,
                resolved,
                "If none exist, the first candidate should be returned"
            );
        } finally {
            dirs[0] = d0;
            dirs[1] = d1;
            dirs[2] = d2;
        }
    }
}
