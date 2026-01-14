package UserFeatures;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.Scanner;

/**
 * Provides proposal creation and persistence for ministry members.
 *
 * <p>
 * Each ministry is associated with a single proposal file:
 *
 * <pre>
 * MinistryOf&lt;SafeName&gt;.txt
 * </pre>
 *
 * <p>
 * Multiple proposals are stored as blocks:
 *
 * <pre>
 * PROPOSAL|&lt;timestamp&gt;
 * EDIT|&lt;ministryName&gt;|&lt;Increase/Decrease&gt;|&lt;amount&gt;|&lt;changeType&gt;
 * ...
 * REASON|...
 * ENDPROPOSAL
 * </pre>
 *
 * <p>
 * The Governor reviews the latest block and can accept/reject it. Accepting applies edits to the
 * real budgets (global state).
 * </p>
 */
public class Propose {

  /** Storage directory for proposal files. */
  private static final String BASE_DIR =
      "src/main/resources/NecessaryFilesAndData/ProposalsFromMinisters/";

  /** Ministry name associated with this proposal handler. */
  private String ministryName;

  /**
   * Temporary balance used exclusively during proposal mode. This value is used to validate
   * increases in proposal mode without mutating real budgets.
   */
  public static double sharedBalance = 0;

  /** Scanner used by legacy CLI code paths that still prompt for reasoning. */
  private final Scanner scanner = new Scanner(System.in);

  /** Creates an instance and ensures the directory exists. */
  public Propose() {
    createDirectories();
  }

  /**
   * Creates an instance for a specific ministry and ensures the directory exists.
   *
   * @param ministryName the ministry name
   */
  public Propose(String ministryName) {
    this.ministryName = ministryName;
    createDirectories();
  }

  /**
   * Sets the ministry name associated with this instance.
   *
   * @param ministryName the ministry name
   */
  public void setMinistryName(String ministryName) {
    this.ministryName = ministryName;
  }

  /**
   * Returns the associated ministry name.
   *
   * @return ministry name
   */
  public String getMinistryName() {
    return ministryName;
  }

  /**
   * Returns the proposal file for the configured ministry.
   *
   * @return proposal file
   *
   * @throws IllegalStateException if ministry name is not set
   */
  public File getProposalFile() {
    if (ministryName == null || ministryName.isBlank()) {
      throw new IllegalStateException("Ministry name not set.");
    }
    String safe = safeName(ministryName);
    return new File(BASE_DIR + "MinistryOf" + safe + ".txt");
  }

  /*
   * ========================================================= Backward-compatible API (required by
   * existing code) =========================================================
   */

  /**
   * Legacy entry point retained for compatibility with existing code paths.
   *
   * <p>
   * This method collects edits in proposal (sandbox) mode and persists them as a new proposal block
   * under the ministry proposal file.
   * </p>
   *
   * @param ministryName the ministry name
   */
  public void editProposal(String ministryName) {
    createDirectories();
    this.ministryName = ministryName;

    // Backup real balance and switch to proposal sandbox balance
    double realBalanceBackup = Edit.balance;
    Edit.balance = sharedBalance;

    try {
      // Collect edits in proposal mode (no real budget mutations)
      Edit editor = new Edit();
      editor.collectData(true);

      // Persist remaining proposal balance for the next proposal session
      sharedBalance = Edit.balance;

      // Optional reasoning (legacy flow)
      System.out.println("Would you like to add a reasoning for the changes?");
      String reason = scanner.nextLine();

      // Persist as a proposal block
      submitEditsProposalBlock(reason);

    } finally {
      // Restore real balance and clear proposal edits from memory
      Edit.balance = realBalanceBackup;
      Edit.history.clear();
    }
  }

  /*
   * ========================================================= Primary persistence API
   * =========================================================
   */

  /**
   * Persists the current in-memory edit history ({@link Edit#history}) as a new proposal block.
   *
   * @param reasoning optional reasoning text
   *
   * @throws IllegalStateException if no edits are available in {@link Edit#history}
   */
  public void submitEditsProposalBlock(String reasoning) {
    createDirectories();

    if (Edit.history.getEditList().isEmpty()) {
      throw new IllegalStateException("No edits to submit.");
    }

    File file = getProposalFile();

    try (FileWriter fw = new FileWriter(file, true); PrintWriter pw = new PrintWriter(fw)) {

      String timestamp = Instant.now().toString();

      pw.println("PROPOSAL|" + timestamp);

      for (Edit e : Edit.history.getEditList()) {
        pw.println(e.serialize());
      }

      pw.println("REASON|" + (reasoning == null ? "" : reasoning.trim()));
      pw.println("ENDPROPOSAL");
      pw.println();

    } catch (IOException e) {
      throw new RuntimeException("Failed to save proposal.", e);
    }
  }

  /*
   * ========================================================= Internal helpers
   * =========================================================
   */

  /** Ensures the proposals directory exists. */
  private void createDirectories() {
    File dir = new File(BASE_DIR);
    if (!dir.exists()) {
      dir.mkdirs();
    }
  }

  /**
   * Produces a filesystem-safe name for proposal filenames.
   *
   * @param name original ministry name
   *
   * @return alphanumeric-only string
   */
  private static String safeName(String name) {
    if (name == null) {
      return "";
    }
    return name.replaceAll("[^a-zA-Z0-9]", "");
  }
}
