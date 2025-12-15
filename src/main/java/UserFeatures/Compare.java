package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * The Compare class provides functionality to compare ministry budgets between two different years.
 * It displays a side-by-side comparison table showing budget data from both years.
 *
 * REFACTORED: Now uses StringBuilder + TableUtils for cleaner, more efficient code.
 */
public class Compare {

    /**
     * Main method for comparing ministry budgets between two years.
     * Prompts the user to select two years, validates them, and displays a comparison table.
     * The comparison is both displayed on screen and saved to a file.
     */
    public static void comparingMinistries() {
        System.out.println("Please type the first of the two years that you want to compare:");
        int firstYear = validityYear(0);

        System.out.println("Please type the second year that you want to compare:");
        int secondYear = validityYear(firstYear);

        // Retrieve ministry data for both years
        Ministry[] firstYearMinistry = View.ministryYear(firstYear);
        Ministry[] secondYearMinistry = View.ministryYear(secondYear);

        // Validation: Check if data exists for both years
        if (firstYearMinistry == null || secondYearMinistry == null) {
            System.out.println("Cannot compare - missing data for one or both years.");
            return;
        }

        // Build comparison table using StringBuilder and TableUtils
        StringBuilder sb = new StringBuilder();

        // ========== HEADER ==========
        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTitle(sb, "BUDGET COMPARISON: " + firstYear + " vs " + secondYear, 120);
        TableUtils.appendSeparator(sb, 120, '=');

        // ========== COLUMN HEADERS ==========
        TableUtils.appendTableRow(sb, "MINISTRY", firstYear + " BUDGET", secondYear + " BUDGET");
        TableUtils.appendSeparator(sb, 120, '-');

        // ========== DATA ROWS ==========
        // Compare only up to the length of the shorter array
        int maxRows = Math.min(firstYearMinistry.length, secondYearMinistry.length);

        for (int i = 0; i < maxRows; i++) {
            if (firstYearMinistry[i] != null && secondYearMinistry[i] != null) {
                String name = firstYearMinistry[i].getMinistryName();
                String budget1 = Ministry.getFormattedBudget(firstYearMinistry[i].getBudget());
                String budget2 = Ministry.getFormattedBudget(secondYearMinistry[i].getBudget());

                TableUtils.appendTableRow(sb, name, budget1, budget2);
            }
        }

        // ========== FOOTER ==========
        TableUtils.appendSeparator(sb, 120, '=');

        // Convert StringBuilder to String
        String output = sb.toString();

        // Display to screen
        System.out.println(output);

        // Save to file and handle any errors
        try {
            Files.writeString(
                Paths.get("NecessaryFilesAndData/compare" + firstYear + "with" + secondYear + ".txt"),
                output,
                StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            System.err.println("Error writing comparison file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Validates and prompts the user to enter a valid year between 2020 and 2026.
     * Continues prompting until a valid year is entered.
     *
     * @return A valid year between 2020 and 2026 (inclusive)
     */
    public static int validityYear(int year) {
        Scanner scanner = new Scanner(System.in);
        int selectedYear = 0;
        boolean validYear = false;

        while (!validYear) {
            System.out.println("Please select a year (2020-2026):");
            try {
                selectedYear = scanner.nextInt();
                scanner.nextLine();
                if (selectedYear >= 2020 && selectedYear <= 2026) {
                    if (selectedYear!=year) {
                        validYear = true;
                    } else {
                        System.out.println("Cannot be the same with the first year");
                    }
                } else {
                    System.out.println("Invalid year. Please enter a year between 2020 and 2026.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a valid year.");
                scanner.nextLine(); // Clear invalid input
            }
        }
        return selectedYear;
    }
}