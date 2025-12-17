package UserFeatures;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
/**
 * This is a class that creates the edithistory.txt file where all the edits made by the GOVERNOR and the CITIZENS
 * are stored. This file does NOT store the edits made by the ministers.
 */
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class EditHistory {
    Scanner scanner = new Scanner(System.in);
    private static final String HISTORY_FILE = "NecessaryFilesAndData/edithistory.txt";
    public static void historyOfEdit(String ministryName, double previousBudget, double newBudget, int type) {
        StringBuilder sb = new StringBuilder();
        FileWriter fw = null;
        PrintWriter pw = null;
        try {
            File file = new File(HISTORY_FILE);
            /*fw = new FileWriter("NecessaryFilesAndData/edithistory.txt", true);
            pw = new PrintWriter(fw);*/
            if (!file.exists() || file.length() == 0) {
                TableUtils.appendSeparator(sb, 120, '=');
                TableUtils.appendTitle(sb, "RECENT CHANGES", 120);
                TableUtils.appendSeparator(sb, 120, '=');
                TableUtils.appendTableRow(sb, "MINISTRY ", "PREVIOUS BUDGET", "NEW BUDGET");
                TableUtils.appendSeparator(sb, 120, '-');
            } else {
                if (type == 0) {
                TableUtils.appendTitle(sb, "========== New Change ==========", 120);
                }
            }
            String budget1 = Ministry.getFormattedBudget(previousBudget);
            String budget2 = Ministry.getFormattedBudget(newBudget);
            TableUtils.appendTableRow(sb, ministryName, budget1, budget2);
            String output = sb.toString();
            Files.writeString(
                Paths.get(HISTORY_FILE),
                output,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,        // Δημιούργησε αν δεν υπάρχει
                StandardOpenOption.APPEND         // ΠΡΟΣΘΕΣΕ, μην διαγράψεις!
            );
        } catch (IOException e) {
            System.err.println("Error writing to edit history: " + e.getMessage());
            e.printStackTrace();  // Για debugging
            /* pw.println(ministryName + " previous budget: " + Ministry.getFormattedBudget(previousBudget));
            pw.println(ministryName + " new budget: " + Ministry.getFormattedBudget(newBudget));
            pw.close();
            fw.close(); */
                //System.out.println("Do you want to undo?");
                //var e = new EditHistoryList();
                //e.undo();
        } 
    }
}

