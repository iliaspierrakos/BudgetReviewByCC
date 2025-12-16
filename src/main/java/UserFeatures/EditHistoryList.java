package UserFeatures;
/**
 
This is a class for creating a list for storing the edits made from the user
it has the addEdit, undo and reverseEdit methods which are used for
adding an edit to the list, adding the undo feature to the app and being
able to reverse edit the changes made by the user. */
import java.util.LinkedList;

public class EditHistoryList { 
    public LinkedList<Edit> editList = new LinkedList<>();
    private int index=-1;
    public void addEdit(Edit edit) {
        editList.add(edit);
        index++;
        //System.out.println(edit);
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
        if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
            var e = new Edit(lastEdit.getName(), "Decrease", lastEdit.getAmount());
            //System.out.println(lastEdit);
            Edit.balance += lastEdit.getAmount();
            e.editingbudget(e,  true, false);
        } else {
            var e = new Edit(lastEdit.getName(), "Increase", lastEdit.getAmount());
            //System.out.println(lastEdit);
            Edit.balance -= lastEdit.getAmount();
            e.editingbudget(e, true, false);
        }
    }
    public void applyingEdits() {
        if (index >= 0) {
            for (Edit e : editList) {
            e.editingbudget(e, false, false);
            index = -1;
            }
        } else {
            System.out.println("No edits to apply.");
        }
    }
    public int getIndex() {
        return index;
    }
}