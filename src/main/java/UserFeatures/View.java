package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The {@code View} class handles the "View" option of the application's menu.
 * It provides methods for displaying data related to the budgets of the selected Ministries.
 *
 * REFACTORED: Now uses StringBuilder + TableUtils for cleaner, more efficient code.
 */
public class View {

    /**
     * Displays the name, budget, and percentage of every Ministry object for the selected year.
     * The output is formatted as a professional table and both displayed on screen and saved to a file.
     *
     * Uses StringBuilder for efficient string building and TableUtils for consistent formatting.
     *
     * @param year The year for which to display the budget (2020-2026)
     */
    public static void viewGovBudget(int year) {
        Ministry[] selectedMinistries = ministryYear(year);

        // Validation: Check if data exists for the year
        if (selectedMinistries == null) {
            System.out.println("No data available for year " + year);
            return;
        }

        // Calculate total budget across all ministries
        double inUseBudget = 0;
        for (Ministry m : selectedMinistries) {
            if (m != null) {
                inUseBudget += m.getBudget();
            }
        }

        // Validation: Cannot calculate percentages if total is zero
        if (inUseBudget == 0) {
            System.out.println("Total budget is 0. cannot calculate percentages.");
            return;
        }

        // Build the table using StringBuilder and TableUtils
        StringBuilder sb = new StringBuilder();

        // ========== HEADER ==========
        TableUtils.appendSeparator(sb, 105, '=');
        TableUtils.appendTitle(sb, "GOVERNMENT BUDGET " + year, 105);
        TableUtils.appendSeparator(sb, 105, '=');

        // ========== COLUMN HEADERS ==========
        TableUtils.appendTableRow(sb, "MINISTRY", "BUDGET", "PERCENTAGE");
        TableUtils.appendSeparator(sb, 105, '-');

        // ========== DATA ROWS ==========
        for (Ministry m : selectedMinistries) {
            if (m != null) {
                double budget = m.getBudget();
                String formattedBudget = Ministry.getFormattedBudget(budget);

                // Calculate percentage of total budget
                double percent = (budget / inUseBudget) * 100;

                // Format percentage with European style (comma for decimals)
                String formattedPercent = String.format("%.2f%%", percent).replace(".", ",");

                // Add row to table
                TableUtils.appendTableRow(sb, m.getMinistryName(), formattedBudget, formattedPercent);
            }
        }

        // ========== FOOTER ==========
        TableUtils.appendSeparator(sb, 105, '-');
        TableUtils.appendTableRow(sb, "TOTAL", Ministry.getFormattedBudget(inUseBudget), "100,00%");
        TableUtils.appendSeparator(sb, 105, '=');

        // Convert StringBuilder to String
        String output = sb.toString();

        // Display to screen
        System.out.println(output);

        // Save to file and handle any errors
        try {
            Files.writeString(
                Paths.get("NecessaryFilesAndData/view" + year + ".txt"),
                output,
                StandardCharsets.UTF_8
            );

            // For year 2026, also show available balance for investments
            if (year == 2026) {
                System.out.println("Available = " + Ministry.getFormattedBudget(Edit.balance));
            }
        } catch (IOException e) {
            System.err.println("Error writing view file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Returns the Ministry array for a specific year.
     * Used to select which year's data to display or compare.
     *
     * @param year The year to retrieve ministries for (2020-2026)
     * @return The Ministry array for the specified year, or null if year is invalid
     */
    public static Ministry[] ministryYear(int year) {
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
        }
        return selectedMinistries;
    }
}
