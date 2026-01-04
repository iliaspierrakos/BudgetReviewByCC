package UserFeatures;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * Manages a history of edits made by the user.
 * <p>
 * Provides functionality to add edits, undo the most recent edits, reverse
 * specific edits, apply all edits, and interactively undo multiple changes.
 * </p>
 * <p>
 * This class is useful for applications that need undo/redo functionality and
 * for tracking changes in budgets or other editable items.
 * </p>
 */
public class EditHistoryList { 

    /** List that stores all the edits made by the user. */
    public LinkedList<Edit> editList = new LinkedList<>();

    /** Current index of the most recent edit in the list. */
    private int index = -1;

    /** Scanner for reading user input when reversing changes interactively. */
    private Scanner scanner = new Scanner(System.in);

    /**
     * Adds an edit to the history list and updates the current index.
     *
     * @param edit the {@link Edit} object representing the change to be added
     */
    public void addEdit(Edit edit) {
        editList.add(edit);
        index++;
    }

    /**
     * Undoes the most recent edit in the history.
     * <p>
     * If there are no edits to undo, prints a message to the console.
     * </p>
     */
    public void undo() {
        if (index >= 0) {
            Edit lastEdit = editList.get(index);
            reverseEdit(lastEdit);
            editList.remove(index);
            index--;
        } else {
            System.out.println("Nothing to undo");
        }
    }

    /**
     * Reverses the effect of a given edit.
     * <p>
     * For "Fixed" type edits, it inverts the increase/decrease change.
     * For percentage-based edits, it calculates the original value and
     * reverses the change proportionally.
     * </p>
     *
     * @param lastEdit the {@link Edit} object representing the edit to reverse
     */
    public void reverseEdit(Edit lastEdit) {
        if (lastEdit.getChangeType().equalsIgnoreCase("Fixed")) {
            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                Edit e = new Edit(lastEdit.getName(), "Decrease", lastEdit.getAmount());
                Edit.balance += lastEdit.getAmount();
                e.editingbudget(e, true, false);
            } else {
                Edit e = new Edit(lastEdit.getName(), "Increase", lastEdit.getAmount());
                Edit.balance -= lastEdit.getAmount();
                e.editingbudget(e, true, false);
            }
        } else {
            double currentBudget = Ministry.budgetSearchByName(
                    lastEdit.getName(), CreatingMinistries.ministries2026);

            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                double oldBudget = currentBudget * 100 / (100 + lastEdit.getAmount());
                double updatedAmount = currentBudget - oldBudget;
                Edit e = new Edit(lastEdit.getName(), "Decrease", updatedAmount);
                Edit.balance += updatedAmount;
                e.editingbudget(e, true, false);
            } else {
                double oldBudget = currentBudget * 100 / (100 - lastEdit.getAmount());
                double updatedAmount = oldBudget - currentBudget;
                Edit e = new Edit(lastEdit.getName(), "Increase", updatedAmount);
                Edit.balance -= updatedAmount;
                e.editingbudget(e, true, false);
            }
        }
    }

    /**
     * Applies all edits stored in the history list.
     * <p>
     * Once applied, the index is reset. If there are no edits, prints a message.
     * </p>
     */
    public void applyingEdits() {
        if (index >= 0) {
            for (Edit e : editList) {
                e.editingbudget(e, false, false);
            }
            index = -1;
        } else {
            System.out.println("No edits to apply.");
        }
    }

    /**
     * Returns the current index of the last edit in the list.
     *
     * @return the index of the most recent edit, or -1 if the list is empty
     */
    public int getIndex() {
        return index;
    }

    /**
     * Allows the user to interactively undo multiple recent edits.
     * <p>
     * Prompts the user for the number of last changes to undo, validates the input,
     * and undoes the specified number of edits.
     * </p>
     */
    public void reverseChanges() {
        System.out.println("How many of your last changes do you want to undo?");
        int changes = scanner.nextInt();

        while (changes < 0 || changes > (Edit.history.getIndex() + 1)) {
            System.out.println("Invalid. Your response must be <= " + Edit.history.getIndex());
            changes = scanner.nextInt();
        }

        for (int i = 0; i < changes; i++) {
            Edit.history.undo();
        }
    }
}
