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
 * This test class focuses exclusively on deterministic, UI-independent
 * helper logic contained in {@code DraftProposalExporter}.
 * </p>
 *
 * <p>
 * Private static helper methods are accessed via reflection in order
 * to avoid modifying production visibility solely for testing purposes.
 * </p>
 */
public class TestDraftProposalExporter {

    /** Reference to normalizeChange(String) helper method. */
    private Method normalizeChange;

    /** Reference to safeToken(String, String) helper method. */
    private Method safeToken;

    /** Reference to formatHuman(double) helper method. */
    private Method formatHuman;

    /** Reference to resolveProposalsDir() helper method. */
    private Method resolveProposalsDir;

    /**
     * Loads private static helper methods via reflection before each test.
     * Failing here indicates an unexpected change in method signatures.
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
            fail("Failed to access private helper methods via reflection", e);
        }
    }

    /**
     * Verifies normalization rules for change type values.
     * Only {@code Increase} or {@code Decrease} should be produced.
     */
    @Test
    void testNormalizeChange() throws Exception {
        assertEquals("Increase", normalizeChange.invoke(null, (Object) null));
        assertEquals("Increase", normalizeChange.invoke(null, " increase "));
        assertEquals("Decrease", normalizeChange.invoke(null, "Decrease"));
        assertEquals("Decrease", normalizeChange.invoke(null, "dec"));
        assertEquals("Decrease", normalizeChange.invoke(null, "Decreasing"));
        assertEquals("Increase", normalizeChange.invoke(null, "unknown value"));
    }

    /**
     * Ensures that filename tokens are sanitized, deterministic and bounded.
     */
    @Test
    void testSafeToken() throws Exception {
        assertEquals("unknown", safeToken.invoke(null, null, "unknown"));
        assertEquals("UnknownMinistry", safeToken.invoke(null, "   ", "UnknownMinistry"));
        assertEquals("John_Doe_2026",
                safeToken.invoke(null, "John Doe! 2026", "x"));

        String longInput =
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 60 chars
        String out = (String) safeToken.invoke(null, longInput, "x");

        assertTrue(out.length() <= 40,
                "Output length must be capped to prevent filesystem issues");
    }

    /**
     * Confirms that currency symbols are removed and
     * output is trimmed for human-readable formatting.
     */
    @Test
    void testFormatHumanRemovesEuroSymbol() throws Exception {
        String out = (String) formatHuman.invoke(null, 1234.56);

        assertNotNull(out);
        assertFalse(out.contains("€"),
                "Currency symbol must not be present in formatted output");
        assertEquals(out.trim(), out,
                "Formatted output should not contain leading/trailing whitespace");
    }

    /**
     * Verifies that the first existing directory from the candidate list
     * is selected by the resolver.
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
            assertEquals(existing, resolved,
                    "Resolver must return the first directory that exists");
        } finally {
            dirs[0] = d0;
            dirs[1] = d1;
            dirs[2] = d2;
        }
    }

    /**
     * Ensures fallback behavior when none of the candidate directories exist.
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
            assertEquals(missing1, resolved,
                    "When no directory exists, the first candidate must be returned");
        } finally {
            dirs[0] = d0;
            dirs[1] = d1;
            dirs[2] = d2;
        }
    }
}
