package UserFeatures;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

/**
 * The {@code EditHistory} class is responsible for maintaining a persistent history of all budget
 * edits performed by the Governor and the Citizens.
 *
 * <p>
 * The edit history is stored in a text file ({@code edithistory.txt}) and records changes in a
 * formatted table structure, including the ministry name, the previous budget, and the updated
 * budget.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> Edits performed by ministers are intentionally excluded from this history
 * and are handled separately.
 * </p>
 */
public class EditHistory {

  /** Scanner used for console input (reserved for future extensions). */
  private final Scanner scanner = new Scanner(System.in);

  /** Path to the edit history file. */
  private static final String HISTORY_FILE =
      "src/main/resources/NecessaryFilesAndData/edithistory.txt";

  /**
   * Appends a new budget modification entry to the edit history file.
   *
   * <p>
   * If the history file does not exist or is empty, a table header is created before adding the
   * first entry. Subsequent edits are appended to the existing file.
   * </p>
   *
   * <p>
   * The method formats budget values using the {@link Ministry} formatting utilities and relies on
   * {@link TableUtils} to maintain consistent table structure.
   * </p>
   *
   * @param ministryName the name of the ministry whose budget was modified
   * @param previousBudget the budget value before the change
   * @param newBudget the budget value after the change
   * @param type the type of edit (e.g. 0 indicates a new change header)
   */
  public static void historyOfEdit(String ministryName, double previousBudget, double newBudget,
      int type) {

    StringBuilder sb = new StringBuilder();

    try {
      File file = new File(HISTORY_FILE);

      if (!file.exists() || file.length() == 0) {
        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTitle(sb, "RECENT CHANGES", 120);
        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTableRow(sb, "MINISTRY", "PREVIOUS BUDGET", "NEW BUDGET");
        TableUtils.appendSeparator(sb, 120, '-');
      } else {
        if (type == 0) {
          TableUtils.appendTitle(sb, "========== New Change ==========", 120);
        }
      }

      String budget1 = Ministry.getFormattedBudget(previousBudget);
      String budget2 = Ministry.getFormattedBudget(newBudget);

      TableUtils.appendTableRow(sb, ministryName, budget1, budget2);

      Files.writeString(Paths.get(HISTORY_FILE), sb.toString(), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);

    } catch (IOException e) {
      System.err.println("Error writing to edit history: " + e.getMessage());
      e.printStackTrace(); // For debugging purposes
    }
  }
}
