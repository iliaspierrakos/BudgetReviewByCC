package UserFeatures;

import UserManagement.MinistryMember;
import UserManagement.User;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Exports the current draft budget session into a text proposal file.
 *
 * <p>
 * The produced file is intended for review by higher authorities (e.g. Governor or Prime Minister)
 * and contains a human-readable summary of all draft edits.
 * </p>
 */
public final class DraftProposalExporter {

  /**
   * Candidate directories where proposal files may be stored, depending on runtime environment.
   */
  private static final Path[] CANDIDATE_DIRS =
      new Path[] {Path.of("src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters"),
          Path.of("NecessaryFilesAndData/ProposalsFromMinisters"),
          Path.of("target/classes/NecessaryFilesAndData/ProposalsFromMinisters")};

  /** Timestamp format used in proposal file names. */
  private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  private DraftProposalExporter() {}

  /**
   * Exports the current draft session to a proposal file and displays a notification dialog to the
   * user.
   *
   * <p>
   * Only users that are instances of {@link MinistryMember} are allowed to submit proposals.
   * </p>
   *
   * @param owner the owning JavaFX stage for dialog display
   * @param user the currently logged-in user
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
      show(owner, Alert.AlertType.WARNING, "Nothing to send", "No draft edits found to export.");
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

  /**
   * Builds the textual content of the proposal file.
   *
   * @param user the submitting user
   * @param edits the list of draft edits to include
   *
   * @return formatted proposal text
   */
  private static String buildPayload(User user, List<DraftEditSession.DraftEdit> edits) {
    StringBuilder sb = new StringBuilder();

    sb.append("MINISTER PROPOSAL\n");
    sb.append("From: ").append(user.getUsername() == null ? "unknown" : user.getUsername())
        .append("\n");
    sb.append("Submitted: ").append(LocalDateTime.now()).append("\n\n");

    sb.append("Draft edits:\n");

    for (DraftEditSession.DraftEdit e : edits) {
      if (e == null)
        continue;

      String ministry = (e.ministry == null ? "" : e.ministry.trim());
      String mode = (e.mode == null || e.mode.isBlank()) ? "fixed" : e.mode.trim();
      double amount = e.amount;

      String change = normalizeChange(e.changeType);

      if ("Increase".equalsIgnoreCase(change)) {
        sb.append(ministry).append(" Increased by ").append(formatHuman(amount)).append(" ")
            .append(mode).append("\n");
      } else {
        sb.append(ministry).append(" Decreased by ").append(formatHuman(amount)).append(" ")
            .append(mode).append("\n");
      }

      sb.append("EDIT|").append(ministry).append("|").append(change).append("|").append(amount)
          .append("|").append(mode).append("\n");
    }

    sb.append("\nReason: —\n");
    return sb.toString();
  }

  /**
   * Resolves the directory used for storing proposal files.
   *
   * @return an existing directory path or a default fallback
   */
  private static Path resolveProposalsDir() {
    for (Path p : CANDIDATE_DIRS) {
      if (Files.exists(p) && Files.isDirectory(p)) {
        return p;
      }
    }
    return CANDIDATE_DIRS[0];
  }

  /**
   * Normalizes raw change type values to either "Increase" or "Decrease".
   *
   * @param raw the raw change type string
   *
   * @return normalized change type
   */
  private static String normalizeChange(String raw) {
    if (raw == null) {
      return "Increase";
    }
    String s = raw.trim().toLowerCase();
    if (s.startsWith("dec")) {
      return "Decrease";
    }
    return "Increase";
  }

  /**
   * Formats a numeric amount into a human-readable form without currency symbols.
   *
   * @param amount the amount to format
   *
   * @return formatted amount string
   */
  private static String formatHuman(double amount) {
    return Ministry.getFormattedBudget(amount).replace("€", "").trim();
  }

  /**
   * Sanitizes a string so it can be safely used in file names.
   *
   * @param raw original value
   * @param fallback fallback value if input is empty
   *
   * @return safe token string
   */
  private static String safeToken(String raw, String fallback) {
    String v = raw == null ? "" : raw.trim();
    if (v.isBlank()) {
      v = fallback;
    }
    v = v.replaceAll("[^A-Za-z0-9]+", "_");
    if (v.length() > 40) {
      v = v.substring(0, 40);
    }

    return v;
  }

  /**
   * Displays a modal JavaFX alert dialog.
   *
   * @param owner the owning stage
   * @param type alert type
   * @param title dialog title
   * @param msg dialog message
   */
  private static void show(Stage owner, Alert.AlertType type, String title, String msg) {
    Alert a = new Alert(type);
    a.initOwner(owner);
    a.setTitle(title);
    a.setHeaderText(title);
    a.setContentText(msg);

    var css = DraftProposalExporter.class.getResource("/css/DarkTheme.css");
    if (css != null) {
      a.getDialogPane().getStylesheets().add(css.toExternalForm());
    }

    a.showAndWait();
  }
}
