package UserFeatures;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * The {@code EditHistoryList} class manages a history of edits made by the user.
 * <p>
 * It stores edits in a linked list and provides functionality to:
 * <ul>
 *     <li>Add new edits to the history</li>
 *     <li>Undo the most recent edits</li>
 *     <li>Reverse a specific edit</li>
 *     <li>Apply all stored edits</li>
 *     <li>Interactively undo multiple changes</li>
 * </ul>
 * </p>
 * <p>
 * This class is useful for applications that need undo/redo functionality and
 * keep track of changes in budgets or other editable items.
 * </p>
 */
public class EditHistoryList { 

    /** List that stores all the edits made by the user. */
    public LinkedList<Edit> editList = new LinkedList<>();

    /** Current index of the most recent edit in the list. */
    private int index = -1;

    /** Scanner for reading user input when reversing changes interactively. */
    Scanner scanner = new Scanner(System.in);

    /**
     * Adds an edit to the history list and updates the current index.
     * 
     * @param edit The {@link Edit} object representing the change to be added.
     */
    public void addEdit(Edit edit) {
        editList.add(edit);
        index++;
    }

    /**
     * Undoes the most recent edit in the history.
     * <p>
     * If there are no edits to undo, a message is printed to the console.
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
     * For "Fixed" type edits, it simply inverts the increase/decrease change.
     * For percentage-based edits, it calculates the original value and reverses
     * the change proportionally.
     * </p>
     * 
     * @param lastEdit The {@link Edit} object representing the edit to reverse.
     */
    public void reverseEdit(Edit lastEdit) {
        if (lastEdit.getChangeType().equalsIgnoreCase("Fixed")) {
            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                var e = new Edit(lastEdit.getName(), "Decrease", lastEdit.getAmount());
                Edit.balance += lastEdit.getAmount();
                e.editingbudget(e, true, false);
            } else {
                var e = new Edit(lastEdit.getName(), "Increase", lastEdit.getAmount());
                Edit.balance -= lastEdit.getAmount();
                e.editingbudget(e, true, false);
            }
        } else {
            double currentBudget = Ministry.budgetSearchByName(
                    lastEdit.getName(), CreatingMinistries.ministries2026);

            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                double oldBudget = currentBudget * 100 / (100 + lastEdit.getAmount());
                double updatedAmount = currentBudget - oldBudget;
                var e = new Edit(lastEdit.getName(), "Decrease", updatedAmount);
                Edit.balance += updatedAmount;
                e.editingbudget(e, true, false);
            } else {
                double oldBudget = currentBudget * 100 / (100 - lastEdit.getAmount());
                double updatedAmount = oldBudget - currentBudget;
                var e = new Edit(lastEdit.getName(), "Increase", updatedAmount);
                Edit.balance -= updatedAmount;
                e.editingbudget(e, true, false);
            }
        }
    }

    /**
     * Applies all edits stored in the history list.
     * <p>
     * Once applied, the index is reset. If there are no edits, a message is printed.
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
     * @return The index of the most recent edit, or -1 if the list is empty.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Allows the user to interactively undo multiple recent edits.
     * <p>
     * Prompts the user for the number of last changes to undo, validates the input,
     * and then undoes the specified number of edits.
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
