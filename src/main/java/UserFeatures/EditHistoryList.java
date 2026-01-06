package UserFeatures;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Stores edits and supports undo / reverse operations.
 *
 * FIXES:
 * - undo/applyAll now go through Edit.applyEdit(...) so balance stays consistent.
 * - reverseEdit no longer manually tweaks balance.
 */
public class EditHistoryList {

    private final LinkedList<Edit> editList = new LinkedList<>();
    private int index = -1;

    public void addEdit(Edit edit) {
        if (edit == null) return;
        editList.add(edit);
        index++;
    }

    /** needed for Propose.java */
    public List<Edit> getEditList() {
        return Collections.unmodifiableList(editList);
    }

    /** needed for Propose.java */
    public void clear() {
        editList.clear();
        index = -1;
    }

    public int getIndex() {
        return index;
    }

    public void undo() {
        if (index >= 0) {
            Edit lastEdit = editList.get(index);

            // ✅ single source of truth
            Edit.applyEdit(lastEdit, true, false);

            editList.remove(index);
            index--;
        } else {
            System.out.println("Nothing to undo");
        }
    }

    public void reverseEdit(Edit lastEdit) {
        if (lastEdit == null) return;
        // ✅ single source of truth
        Edit.applyEdit(lastEdit, true, false);
    }

    /** apply all edits (if used for proposals/accept) */
    public void applyingEdits() {
        if (index >= 0) {
            for (Edit e : editList) {
                // ✅ single source of truth
                Edit.applyEdit(e, false, false);
            }
            clear();
        } else {
            System.out.println("No edits to apply.");
        }
    }

    // CLI helper (if you keep it)
    private final Scanner scanner = new Scanner(System.in);

    public void reverseChanges() {
        System.out.println("How many of your last changes do you want to undo?");
        int changes = scanner.nextInt();

        while (changes < 0 || changes > (Edit.history.getIndex() + 1)) {
            System.out.println("Invalid. Your response must be <= " + (Edit.history.getIndex() + 1));
            changes = scanner.nextInt();
        }

        for (int i = 0; i < changes; i++) {
            Edit.history.undo();
        }
    }
}
