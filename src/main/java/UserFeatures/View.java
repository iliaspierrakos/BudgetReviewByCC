package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


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
    public void viewGovBudget(int year, boolean sort) {
        Ministry[] selectedMinistries = ministryYear(year);
        if (sort) {
            sortingBudgets(selectedMinistries);
        }

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
     /**
     * A simple data holder (row model) for displaying the government budget in the GUI (JavaFX TableView).
     *
     * <p>This class stores already formatted strings so the GUI does not deal with formatting rules.</p>
     */
    public static class GovBudgetRow {
        private final String ministry;
        private final String budgetText;
        private final String percentText;

        public GovBudgetRow(String ministry, String budgetText, String percentText) {
            this.ministry = ministry;
            this.budgetText = budgetText;
            this.percentText = percentText;
        }

        public String getMinistry() {
            return ministry;
        }

        public String getBudgetText() {
            return budgetText;
        }

        public String getPercentText() {
            return percentText;
        }
    }

    /**
     * Builds the rows needed by the GUI to display the government budget for a given year.
     *
     * <p>GUI-friendly alternative to {@code viewGovBudget(...)}:
     * it does not print anything and does not write files. It only returns data.</p>
     *
     * @param year the budget year (2020–2026)
     * @param sort whether ministries should be sorted by budget
     * @return list of rows ready for a JavaFX TableView. If no data, returns empty list.
     */
    public List<GovBudgetRow> getGovBudgetRowsForGui(int year, boolean sort) {

        Ministry[] selectedMinistries = ministryYear(year);
        if (selectedMinistries == null) return List.of();

        if (sort) sortingBudgets(selectedMinistries);

        double total = 0;
        for (Ministry m : selectedMinistries) {
            if (m != null) total += m.getBudget();
        }
        if (total == 0) return List.of();

        List<GovBudgetRow> rows = new ArrayList<>();
        for (Ministry m : selectedMinistries) {
            if (m == null) continue;

            double budget = m.getBudget();
            double percent = (budget / total) * 100.0;

            String formattedBudget = Ministry.getFormattedBudget(budget);
            String formattedPercent = String.format("%.2f%%", percent).replace(".", ",");

            rows.add(new GovBudgetRow(m.getMinistryName(), formattedBudget, formattedPercent));
        }

        rows.add(new GovBudgetRow("TOTAL", Ministry.getFormattedBudget(total), "100,00%"));
        return rows;
    }

    /**
     * Optional sorting hook. If you want, you can implement sorting by budget here.
     * For now it does nothing (safe).
     *
     * @param ministries the array to be sorted
     */
    public void sortingBudgets(Ministry[] ministries) {
        // TODO: implement sorting if needed
    }

    public View() {
    }
}