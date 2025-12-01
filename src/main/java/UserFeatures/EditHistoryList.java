package UserFeatures;

import java.util.LinkedList;

public class EditHistoryList { 
    public static LinkedList<Edit> editList = new LinkedList<>();
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
            index--;
        } else {
            System.out.println("Nothing to undo");
         }
    }
    public void reverseEdit(Edit lastEdit) {
        if (lastEdit.getChange().equalsIgnoreCase("Increase")) {
            var e = new Edit(lastEdit.getName(), "Decrease", lastEdit.getAmount());
            e.editingbudget(e);
        } else {
            var e = new Edit(lastEdit.getName(), "Increase", lastEdit.getAmount());
            e.editingbudget(e);
        }
    }
}