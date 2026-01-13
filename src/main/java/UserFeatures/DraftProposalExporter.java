package UserFeatures;

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
 * Exports the current draft session into a proposal text file for Governor/Prime Minister review.
 */
public final class DraftProposalExporter {

    private static final Path[] CANDIDATE_DIRS = new Path[] {
            Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("NecessaryFilesAndData/ProposalsFromMinisters"),
            Path.of("target/classes/NecessaryFilesAndData/ProposalsFromMinisters")
    };

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DraftProposalExporter() {}

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

            String change = normalizeChange(e.changeType);

            if ("Increase".equalsIgnoreCase(change)) {
                sb.append(ministry).append(" Increased by ").append(formatHuman(amount)).append(" ").append(mode).append("\n");
            } else {
                sb.append(ministry).append(" Decreased by ").append(formatHuman(amount)).append(" ").append(mode).append("\n");
            }

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

    private static Path resolveProposalsDir() {
        for (Path p : CANDIDATE_DIRS) {
            if (Files.exists(p) && Files.isDirectory(p)) return p;
        }
        return CANDIDATE_DIRS[0];
    }

    private static String normalizeChange(String raw) {
        if (raw == null) return "Increase";
        String s = raw.trim().toLowerCase();
        if (s.startsWith("dec")) return "Decrease";
        return "Increase";
    }

    private static String formatHuman(double amount) {
        return Ministry.getFormattedBudget(amount).replace("€", "").trim();
    }

    private static String safeToken(String raw, String fallback) {
        String v = raw == null ? "" : raw.trim();
        if (v.isBlank()) v = fallback;
        v = v.replaceAll("[^A-Za-z0-9]+", "_");
        if (v.length() > 40) v = v.substring(0, 40);
        return v;
    }

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
