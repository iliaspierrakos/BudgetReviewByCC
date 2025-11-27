package UserFeatures;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;

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
        } catch(IOException e) {}

    }
}

