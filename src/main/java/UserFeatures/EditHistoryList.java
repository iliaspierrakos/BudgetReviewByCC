package UserFeatures;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class EditHistoryList {
    private final LinkedList<Edit> editList = new LinkedList<>();
    private int index = -1;

    public void addEdit(Edit edit) {
        editList.add(edit);
        index++;
    }

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

    public void reverseEdit(Edit lastEdit) {
        if (lastEdit.getChangeType().equalsIgnoreCase("fixed")) {
            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                var e = new Edit(lastEdit.getName(), "Decrease", lastEdit.getAmount());
                Edit.balance += lastEdit.getAmount();
                e.editingbudget(e, true, false);
            } else {
                var e = new Edit(lastEdit.getName(), "Increase", lastEdit.getAmount());
                Edit.balance -= lastEdit.getAmount();
                e.editingbudget(e, true, false);
            }
        } else { // percentage
            double currentBudget = Ministry.budgetSearchByName(lastEdit.getName(), CreatingMinistries.ministries2026);

            if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
                double oldBudget = currentBudget * 100 / (100 + lastEdit.getAmount());
                double updatedAmount = currentBudget - oldBudget;

                var e = new Edit(lastEdit.getName(), "Decrease", updatedAmount);
                Edit.balance += updatedAmount;
                e.editingbudget(e, true, false);

            } else { // Decrease %
                double oldBudget = currentBudget * 100 / (100 - lastEdit.getAmount());
                double updatedAmount = oldBudget - currentBudget;

                var e = new Edit(lastEdit.getName(), "Increase", updatedAmount);
                Edit.balance -= updatedAmount;
                e.editingbudget(e, true, false);
            }
        }
    }

    public void applyingEdits() {
        if (index >= 0) {
            for (Edit e : editList) {
                e.editingbudget(e, false, false);
            }
           
            editList.clear();
            index = -1;
        } else {
            System.out.println("No edits to apply.");
        }
    }

    public int getIndex() {
        return index;
    }

    // καθαρίζει το in-memory history
    public void clear() {
        editList.clear();
        index = -1;
    }

    
    public List<Edit> getEditList() {
        return List.copyOf(editList); // read-only snapshot
    }

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
