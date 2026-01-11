package guiFolder;

import UserFeatures.Ministry;
import UserManagement.MinistryMember;
import UserManagement.User;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * DraftProposalExporter
 *
 * <p>Exports the current minister draft session into a proposal text file that the Governor/Prime Minister
 * can read and accept via {@code GovernorCheckScreen}.</p>
 *
 * <h2>Compatibility guarantees</h2>
 * <ul>
 *   <li>Writes under {@code ProposalsFromMinisters} (resolved robustly across run modes).</li>
 *   <li>Filename contains {@code "proposal"} so UI inbox filters can discover it.</li>
 *   <li>Writes both:
 *     <ul>
 *       <li><b>Human-readable</b> lines under {@code Draft edits:} (supported by GovernorCheckScreen).</li>
 *       <li><b>Machine-readable</b> lines in exact format:
 *           {@code EDIT|<ministry>|<Increase/Decrease>|<amount>|<fixed/percent>}</li>
 *     </ul>
 *   </li>
 *   <li>Normalizes change type to <b>exactly</b> {@code Increase} or {@code Decrease} so {@code Edit.parse(...)}
 *       does not reject edits.</li>
 * </ul>
 */
public final class DraftProposalExporter {

    private static final Path[] CANDIDATE_DIRS = new Path[] {
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("target/classes/NecessaryFilesAndData/ProposalsFromMinisters")
    };

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DraftProposalExporter() {}

    /**
     * Exports the current draft edits to disk and shows a JavaFX dialog with the result.
     *
     * @param owner owner JavaFX stage
     * @param user  current user (must be {@link MinistryMember})
     */
    public static void exportAndNotify(Stage owner, User user) {
        if (!(user instanceof MinistryMember)) {
            show(owner, Alert.AlertType.ERROR, "Access denied",
                    "Only Ministry Members can submit proposals.");
            return;
        }

        if (!DraftEditSession.isInitialized()) {
            show(owner, Alert.AlertType.WARNING, "Draft not ready",
                    "Draft session is not initialized. Open the draft editor first.");
            return;
        }

        List<DraftEditSession.DraftEdit> edits = DraftEditSession.getHistory();
        if (edits == null || edits.isEmpty()) {
            show(owner, Alert.AlertType.WARNING, "Nothing to send",
                    "No draft edits found to export.");
            return;
        }

        Path dir = resolveProposalsDir();
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            show(owner, Alert.AlertType.ERROR, "Folder error",
                    "Cannot access proposals folder:\n" + dir.toAbsolutePath() + "\n\n" + e.getMessage());
            return;
        }

        String from = safeToken(user.getUsername(), "unknown");
        String ministry = safeToken(((MinistryMember) user).getMinistryName(), "UnknownMinistry");
        String stamp = LocalDateTime.now().format(FILE_TS);

        // IMPORTANT: GovernorCheckScreen may filter by (startsWith proposal_ OR contains proposal)
        Path out = dir.resolve("proposal_" + from + "_" + ministry + "_" + stamp + ".txt");

        try {
            String payload = buildPayload(user, edits);

            Files.writeString(out, payload, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);

            show(owner, Alert.AlertType.INFORMATION, "Proposal submitted",
                    "Saved:\n" + out.toAbsolutePath());

        } catch (Exception e) {
            show(owner, Alert.AlertType.ERROR, "Export failed", e.getMessage());
        }
    }

    /**
     * Builds proposal content that is compatible with GovernorCheckScreen parsing.
     */
    private static String buildPayload(User user, List<DraftEditSession.DraftEdit> edits) {
        StringBuilder sb = new StringBuilder();

        sb.append("MINISTER PROPOSAL\n");
        sb.append("From: ").append(user.getUsername() == null ? "unknown" : user.getUsername()).append("\n");
        sb.append("Submitted: ").append(LocalDateTime.now()).append("\n\n");

        sb.append("Draft edits:\n");

        for (DraftEditSession.DraftEdit e : edits) {
            if (e == null) continue;

            String ministry = (e.ministry == null ? "" : e.ministry.trim());
            String mode = (e.mode == null || e.mode.isBlank()) ? "fixed" : e.mode.trim();
            double amount = e.amount;

            // Normalize to EXACT tokens expected by Edit.parse / Governor UI.
            String change = normalizeChange(e.changeType);

            // Human-readable (GovernorCheckScreen parses under "Draft edits:")
            if ("Increase".equalsIgnoreCase(change)) {
                sb.append(ministry).append(" Increased by ").append(formatHuman(amount)).append(" ").append(mode).append("\n");
            } else {
                sb.append(ministry).append(" Decreased by ").append(formatHuman(amount)).append(" ").append(mode).append("\n");
            }

            // Machine-readable (GovernorCheckScreen parses via Edit.parse("EDIT|..."))
            sb.append("EDIT|")
              .append(ministry).append("|")
              .append(change).append("|")
              .append(amount).append("|")
              .append(mode)
              .append("\n");
        }

        sb.append("\nReason: —\n");
        return sb.toString();
    }

    /**
     * Resolves the proposals directory robustly (same idea as console GovernorCheck).
     */
    private static Path resolveProposalsDir() {
        for (Path p : CANDIDATE_DIRS) {
            if (Files.exists(p) && Files.isDirectory(p)) return p;
        }
        return CANDIDATE_DIRS[0];
    }

    /**
     * Normalizes any change text to exactly "Increase" or "Decrease".
     * Accepts also "Increased"/"Decreased" etc.
     */
    private static String normalizeChange(String raw) {
        if (raw == null) return "Increase";
        String s = raw.trim().toLowerCase();
        if (s.startsWith("dec")) return "Decrease";
        return "Increase";
    }

    /**
     * Formats amount in a human-friendly way that your loose parser accepts.
     * Uses your Ministry formatter and strips currency.
     */
    private static String formatHuman(double amount) {
        return Ministry.getFormattedBudget(amount).replace("€", "").trim();
    }

    /**
     * Sanitizes tokens for file names.
     */
    private static String safeToken(String raw, String fallback) {
        String v = raw == null ? "" : raw.trim();
        if (v.isBlank()) v = fallback;
        v = v.replaceAll("[^A-Za-z0-9]+", "_");
        if (v.length() > 40) v = v.substring(0, 40);
        return v;
    }

    /**
     * Shows themed JavaFX alert.
     */
    private static void show(Stage owner, Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.initOwner(owner);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(msg);

        var css = DraftProposalExporter.class.getResource("/css/DarkTheme.css");
        if (css != null) a.getDialogPane().getStylesheets().add(css.toExternalForm());

        a.showAndWait();
    }
}
