package UserFeatures;

import java.util.Scanner;

/**
 * The MinistryProgress class provides functionality to view the budget progress
 * of a specific ministry across all available years (2020-2026).
 */
public class MinistryProgress {
    
    /**
     * Displays the budget progress for a specific ministry across all available years.
     * Shows the ministry name, year, and budget in a formatted table.
     * 
     * @param ministryName The name of the ministry to search for
     */
    public static void viewMinistryProgress(String ministryName) {
        // Build the table using StringBuilder and TableUtils
        StringBuilder sb = new StringBuilder();
        
        // ========== HEADER ==========
        TableUtils.appendSeparator(sb, 85, '=');
        TableUtils.appendTitle(sb, "BUDGET PROGRESS: " + ministryName, 85);
        TableUtils.appendSeparator(sb, 85, '=');
        
        // ========== COLUMN HEADERS ==========
        TableUtils.appendTableRow(sb, "YEAR", "BUDGET");
        TableUtils.appendSeparator(sb, 85, '-');
        
        // ========== DATA ROWS ==========
        for (int year = 2020; year <= 2026; year++) {
            Ministry[] yearArray = ViewGovernmentBudget.ministryYear(year);
            
            if (yearArray != null) {
                // Search for the ministry in this year's array
                for (Ministry m : yearArray) {
                    if (m != null && m.getMinistryName().equalsIgnoreCase(ministryName)) {
                        String formattedBudget = Ministry.getFormattedBudget(m.getBudget());
                        TableUtils.appendTableRow(sb, String.valueOf(year), formattedBudget);
                        break;
                    }
                }
            }
        }
        
        // ========== FOOTER ==========
        TableUtils.appendSeparator(sb, 85, '=');
        
        // Display the table
        System.out.println(sb.toString());
    }
    
    /**
     * Interactive method that prompts the user to enter a ministry name
     * and then displays its budget progress.
     */
    public static void interactiveMinistryProgress() {
        Scanner scanner = new Scanner(System.in);
        Edit editValidator = new Edit(); // Create Edit object to use validityCheck
        
        System.out.println("Which ministry's budget progress do you want to view?");
        String ministryInput = "Ministry of " + scanner.nextLine();
        
        // Use the existing validation method from Edit class
        String validatedMinistry = editValidator.validityCheck(ministryInput);
        
        viewMinistryProgress(validatedMinistry);
    }
}