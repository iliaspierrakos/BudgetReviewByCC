package UserFeatures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Maintains a history of {@link Edit} objects and provides undo/apply operations.
 *
 * <p>
 * All balance and budget mutations are delegated to {@link Edit#applyEdit(Edit, boolean, boolean)}
 * to ensure consistent state transitions.
 * </p>
 */
public class EditHistoryList {

  private final LinkedList<Edit> editList = new LinkedList<>();
  private int index = -1;

  /**
   * Adds an edit entry to the history.
   *
   * @param edit the edit to add
   */
  public void addEdit(Edit edit) {
    if (edit == null) {
      return;
    }
    editList.add(edit);
    index++;
  }

  /**
   * Returns an unmodifiable view of the edit list.
   *
   * @return list of edits
   */
  public List<Edit> getEditList() {
    return Collections.unmodifiableList(editList);
  }

  /**
   * Clears the history and resets the index.
   */
  public void clear() {
    editList.clear();
    index = -1;
  }

  /**
   * Returns the current history index.
   *
   * @return index
   */
  public int getIndex() {
    return index;
  }

  /**
   * Undoes the most recent edit (if any) and removes it from the history.
   */
  public void undo() {
    if (index >= 0) {
      Edit lastEdit = editList.get(index);

      // Single source of truth
      Edit.applyEdit(lastEdit, true, false);

      editList.remove(index);
      index--;
    } else {
      System.out.println("Nothing to undo");
    }
  }

  /**
   * Reverses a specific edit instance without removing it from the list.
   *
   * @param lastEdit the edit to reverse
   */
  public void reverseEdit(Edit lastEdit) {
    if (lastEdit == null) {
      return;
    }
    Edit.applyEdit(lastEdit, true, false);
  }

  /**
   * Applies all edits in the current list to the real budgets, then clears the list.
   *
   * <p>
   * This is not used for file-based proposals directly; proposals should be parsed and applied via
   * {@link Edit#parse(String)} and {@link Edit#applyEdit(Edit, boolean, boolean)}.
   * </p>
   */
  public void applyingEdits() {
    if (index >= 0) {
      for (Edit e : editList) {
        Edit.applyEdit(e, false, false);
      }
      clear();
    } else {
      System.out.println("No edits to apply.");
    }
  }

  // CLI helper (kept if legacy CLI usage remains)
  private final Scanner scanner = new Scanner(System.in);

  /**
   * CLI helper method for undoing multiple last changes.
   */
  public void reverseChanges() {
    System.out.println("How many of your last changes do you want to undo?");
    int changes = scanner.nextInt();
    scanner.nextLine();

    while (changes < 0 || changes > (Edit.history.getIndex() + 1)) {
      System.out.println("Invalid. Your response must be <= " + (Edit.history.getIndex() + 1));
      changes = scanner.nextInt();
      scanner.nextLine();
    }

    for (int i = 0; i < changes; i++) {
      Edit.history.undo();
    }
  }
}
