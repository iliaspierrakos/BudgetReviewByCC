package UserFeatures;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The {@code View} class handles the "View" option of the application's menu.
 * It provides methods for displaying data related to the budgets of the selected Ministries.
 */
public class View {

    /**
     * Displays the name and budget of every Ministry object currently stored
     * in the {@code CreatingMinistries.ministries} array.
     *
     * The budget is retrieved as a double and formatted into a readable string
     * using the static {@code getFormattedBudget} method from the {@code Ministry} class.
     *
     * Note: This method depends on the existence and accessibility of a static array
     * named {@code ministries} in a class named {@code CreatingMinistries}.
     * The name, the budget and the percentage of each ministry are stored and shown to
     * the user as a txt file.
     */
    public static void viewGovBudget(int year) {
        FileWriter fw = null;
        PrintWriter pw = null;
        double mbudg;
        String readable;
        String readablePercent;
        double inUseBudget = 0;

        try {
            fw = new FileWriter("NecessaryFilesAndData/view" + year +".txt", false);
            pw = new PrintWriter(fw);
            Ministry[] selectedMinistries = null;
            switch (year) {
            case 2020:
                selectedMinistries = CreatingMinistries.ministries2020;
                break;
            case 2021:
                selectedMinistries = CreatingMinistries.ministries2021;
                break;
            case 2022:
                selectedMinistries = CreatingMinistries.ministries2022;
                break;
            case 2023:
                selectedMinistries = CreatingMinistries.ministries2023;
                break;
            case 2024:
                selectedMinistries = CreatingMinistries.ministries2024;
                break;
            case 2025:
                selectedMinistries = CreatingMinistries.ministries2025;
                break;
            case 2026:
                selectedMinistries = CreatingMinistries.ministries2026;
                break;
            default:
                System.out.println("Invalid year. Please select a year between 2020 and 2026.");
                return;
            }
            if (selectedMinistries == null) {
                System.out.println("No data available for year " + year);
                return;
            }
            
            // Calculate total budget
            for (Ministry m : selectedMinistries) {
                mbudg = m.getBudget();
                inUseBudget += mbudg;
            }
            
            if (inUseBudget == 0) {
                System.out.println("Total budget is 0 – cannot calculate percentages.");
                return;
            }
            
            // Print header with decorative line
            pw.println("=".repeat(105));
            // Centered title with the year
            pw.println(String.format("%63s", "GOVERNMENT BUDGET " + year));
            pw.println("=".repeat(105));
            // Column headers with proper alignment
            pw.printf("%-60s %20s %20s%n", "MINISTRY", "BUDGET", "PERCENTAGE");
            pw.println("-".repeat(105));
            
            // Print data for each ministry
            for (Ministry m : selectedMinistries) {
                mbudg = m.getBudget();
                readable = Ministry.getFormattedBudget(mbudg);
                
                // Calculate percentage of total budget
                double percent = (mbudg / inUseBudget) * 100;
                
                // Format percentage with European style (comma for decimals)
                // Use String.format with replace to change . to ,
                readablePercent = String.format("%.2f%%", percent).replace(".", ",");
                
                // Print row with proper alignment
                // %-60s: left-aligned string of 60 characters (for ministry name)
                // %20s: right-aligned string of 20 characters (for budget)
                // %20s: right-aligned string of 20 characters (for percentage)
                pw.printf("%-60s %20s %20s%n",
                          m.getMinistryName(),
                          readable,
                          readablePercent);
            }
            
            // Print footer with total
            pw.println("-".repeat(105));
            // Total row with right-aligned 100,00%
            pw.printf("%-60s %20s %20s%n",
                      "TOTAL",
                      Ministry.getFormattedBudget(inUseBudget),
                      "100,00%");
            pw.println("=".repeat(105));
            
            pw.close();
            fw.close();
            
            // Display file contents to screen
            System.out.println(Files.readString(Paths.get("NecessaryFilesAndData/view" + year + ".txt")));
            
        } catch(IOException e) {
            // Print error message if file write/read fails
            System.err.println("Error writing or reading view file: " + e.getMessage());
            e.printStackTrace();
        }
    }

}