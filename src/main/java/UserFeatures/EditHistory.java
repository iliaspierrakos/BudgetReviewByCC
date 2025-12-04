package UserFeatures;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EditHistory {

    public static void historyOfEdit(String ministryName, double previousBudget, double newBudget) {

        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            fw = new FileWriter("NecessaryFilesAndData/edithistory.txt", true);
            pw = new PrintWriter(fw);
            pw.println(ministryName + " previous budget: " + Ministry.getFormattedBudget(previousBudget));
            pw.println(ministryName + " new budget: " + Ministry.getFormattedBudget(newBudget));
            pw.close();
            fw.close();
            //System.out.println("Do you want to undo?");
            //var e = new EditHistoryList();
            //e.undo();
        } catch(IOException e) {}

    }
}

