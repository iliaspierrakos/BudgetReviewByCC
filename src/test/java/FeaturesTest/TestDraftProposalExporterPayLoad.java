
package FeaturesTest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import UserFeatures.DraftEditSession;
import UserFeatures.DraftProposalExporter;
import UserManagement.User;

/**
 * Unit tests for proposal payload generation in {@link DraftProposalExporter}.
 *
 * <p>
 * Scope: Validates deterministic, UI-independent output produced by the
 * private method {@code buildPayload(User, List)}. The public method
 * {@code exportAndNotify(...)} is intentionally not tested here, as it
 * depends on JavaFX dialogs and blocking UI behavior ({@code showAndWait()}).
 * </p>
 *
 * <p>
 * Reflection is used to avoid widening production visibility solely for tests.
 * </p>
 */
public class TestDraftProposalExporterPayLoad {

    /** Reflective reference to private: buildPayload(User, List). */
    private Method buildPayload;

    /**
     * Resolves the private buildPayload method before each test.
     * If this fails, production signature likely changed.
     */
    @BeforeEach
    void setup() {
        try {
            buildPayload = DraftProposalExporter.class
                    .getDeclaredMethod("buildPayload", User.class, List.class);
            buildPayload.setAccessible(true);
        } catch (Exception e) {
            fail("Unable to access DraftProposalExporter.buildPayload via reflection", e);
        }
    }

    /**
     * Creates a concrete {@link User} using the production constructor.
     *
     * <p>
     * To avoid coupling tests to specific Role constant names, the first
     * available enum value is used.
     * </p>
     */
    private User user(String username) {
        User.Role role = User.Role.values()[0];
        return new User(username, "dummyPassword", role);
    }

    /**
     * Creates an immutable DraftEdit instance using its production constructor.
     * DraftEdit fields are final; assignments are not allowed.
     */
    private DraftEditSession.DraftEdit edit(String ministry, String changeType, double amount, String mode) {
        return new DraftEditSession.DraftEdit(ministry, changeType, amount, mode);
    }

    /**
     * Validates that the payload includes all mandatory sections:
     * header, author, timestamp, edits section, and reason placeholder.
     */
    @Test
    void buildPayload_containsHeaderAndSections() throws Exception {
        User u = user("kostas");
        List<DraftEditSession.DraftEdit> edits =
                List.of(edit("Health", "Increase", 100, "fixed"));

        String out = (String) buildPayload.invoke(null, u, edits);

        assertNotNull(out, "Payload must not be null");
        assertTrue(out.startsWith("MINISTER PROPOSAL"),
                "Payload must start with the expected header");
        assertTrue(out.contains("From: kostas"),
                "Payload must include submitting user's username");
        assertTrue(out.contains("Submitted:"),
                "Payload must include submission timestamp");
        assertTrue(out.contains("Draft edits:"),
                "Payload must include 'Draft edits' section");
        assertTrue(out.contains("Reason: —"),
                "Payload must include reason placeholder");
    }

    /**
     * Covers both Increase and Decrease branches (human-readable section)
     * and ensures machine-readable EDIT lines are present.
     */
    @Test
    void buildPayload_increaseAndDecreaseBranches_andEditLines() throws Exception {
        User u = user("u1");

        List<DraftEditSession.DraftEdit> edits = new ArrayList<>();
        edits.add(edit("Finance", "Increase", 500, "fixed"));
        edits.add(edit("Defense", "Decrease", 250, "percent"));

        String out = (String) buildPayload.invoke(null, u, edits);

        // Human-readable lines (branch coverage)
        assertTrue(out.contains("Finance Increased by"),
                "Increase branch should generate 'Increased by' line");
        assertTrue(out.contains("Defense Decreased by"),
                "Decrease branch should generate 'Decreased by' line");

        // Machine-readable lines (do not assert exact numeric formatting beyond presence)
        assertTrue(out.contains("EDIT|Finance|Increase|"),
                "EDIT line for increase must exist");
        assertTrue(out.contains("EDIT|Defense|Decrease|"),
                "EDIT line for decrease must exist");
    }

    /**
     * Ensures that when mode is null or blank, it defaults to "fixed"
     * as per production logic.
     */
    @Test
    void buildPayload_modeDefaultsToFixed_whenNullOrBlank() throws Exception {
        User u = user("u2");

        List<DraftEditSession.DraftEdit> edits = List.of(
                edit("Education", "Increase", 10, null),
                edit("Transport", "Increase", 20, "   ")
        );

        String out = (String) buildPayload.invoke(null, u, edits);

        assertTrue(out.contains("EDIT|Education|Increase|10.0|fixed"),
                "Null mode must default to 'fixed'");
        assertTrue(out.contains("EDIT|Transport|Increase|20.0|fixed"),
                "Blank mode must default to 'fixed'");
    }

    /**
     * Validates that null edits are skipped and that ministry values are trimmed.
     */
    @Test
    void buildPayload_trimsMinistry_andSkipsNullEdits() throws Exception {
        User u = user("u3");

        List<DraftEditSession.DraftEdit> edits = new ArrayList<>();
        edits.add(null); // must be skipped
        edits.add(edit("  Health  ", "Increase", 1, "fixed"));

        String out = (String) buildPayload.invoke(null, u, edits);

        assertFalse(out.contains("EDIT|null|"),
                "Null edits must not produce EDIT lines");
        assertTrue(out.contains("EDIT|Health|Increase|1.0|fixed"),
                "Ministry must be trimmed before being written");
    }

    /**
     * Confirms that unknown/unsupported change types are normalized to Increase.
     */
    @Test
    void buildPayload_unknownChangeDefaultsToIncrease() throws Exception {
        User u = user("u4");

        List<DraftEditSession.DraftEdit> edits =
                List.of(edit("Env", "???", 3, "fixed"));

        String out = (String) buildPayload.invoke(null, u, edits);

        assertTrue(out.contains("Env Increased by"),
                "Unknown change types should default to Increase");
        assertTrue(out.contains("EDIT|Env|Increase|3.0|fixed"),
                "Normalized Increase must be reflected in EDIT line");
    }
}
