package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Provides functionality for comparing ministry budgets between two years.
 *
 * <p>This class belongs to the logic layer and is independent of any UI.
 * It exposes GUI-friendly methods that return comparison data models
 * suitable for JavaFX TableView usage.</p>
 */
public class Compare {

    /* =====================================================
       CLI VERSION (optional – not used by GUI)
       ===================================================== */

    public static void comparingMinistries() {
        System.out.println("Please type the first of the two years that you want to compare:");
        int firstYear = validityYear(0);

        System.out.println("Please type the second year that you want to compare:");
        int secondYear = validityYear(firstYear);

        Ministry[] firstYearMinistry = View.ministryYear(firstYear);
        Ministry[] secondYearMinistry = View.ministryYear(secondYear);

        if (firstYearMinistry == null || secondYearMinistry == null) {
            System.out.println("Cannot compare - missing data.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        TableUtils.appendSeparator(sb, 120, '=');
        TableUtils.appendTitle(
                sb,
                "BUDGET COMPARISON: " + firstYear + " vs " + secondYear,
                120
        );
        TableUtils.appendSeparator(sb, 120, '=');

        TableUtils.appendTableRow(
                sb,
                "MINISTRY",
                firstYear + " BUDGET",
                secondYear + " BUDGET"
        );
        TableUtils.appendSeparator(sb, 120, '-');

        int max = Math.min(firstYearMinistry.length, secondYearMinistry.length);
        for (int i = 0; i < max; i++) {
            if (firstYearMinistry[i] != null && secondYearMinistry[i] != null) {
                TableUtils.appendTableRow(
                        sb,
                        firstYearMinistry[i].getMinistryName(),
                        Ministry.getFormattedBudget(firstYearMinistry[i].getBudget()),
                        Ministry.getFormattedBudget(secondYearMinistry[i].getBudget())
                );
            }
        }

        TableUtils.appendSeparator(sb, 120, '=');

        String output = sb.toString();
        System.out.println(output);

        try {
            Files.writeString(
                    Paths.get("NecessaryFilesAndData/compare" + firstYear + "with" + secondYear + ".txt"),
                    output,
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int validityYear(int year) {
        Scanner scanner = new Scanner(System.in);
        int selectedYear = 0;

        while (true) {
            System.out.println("Please select a year (2020-2026):");
            try {
                selectedYear = scanner.nextInt();
                scanner.nextLine();
                if (selectedYear >= 2020 && selectedYear <= 2026 && selectedYear != year) {
                    return selectedYear;
                }
                System.out.println("Invalid year.");
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("Invalid input.");
            }
        }
    }

    /* =====================================================
       GUI VERSION (USED BY JavaFX)
       ===================================================== */

    /**
     * Immutable row model used by JavaFX TableView.
     */
    public static class CompareRow {
        private final String ministry;
        private final String firstYearBudget;
        private final String secondYearBudget;

        public CompareRow(String ministry, String firstYearBudget, String secondYearBudget) {
            this.ministry = ministry;
            this.firstYearBudget = firstYearBudget;
            this.secondYearBudget = secondYearBudget;
        }

        public String getMinistry() {
            return ministry;
        }

        public String getFirstYearBudget() {
            return firstYearBudget;
        }

        public String getSecondYearBudget() {
            return secondYearBudget;
        }
    }

    /**
     * Builds comparison rows for GUI display.
     *
     * @param firstYear first year (2020–2026)
     * @param secondYear second year (2020–2026)
     * @return list of rows for JavaFX TableView
     */
    public static List<CompareRow> getComparisonRowsForGui(
            int firstYear,
            int secondYear
    ) {

        if (firstYear == secondYear) return List.of();

        Ministry[] first = View.ministryYear(firstYear);
        Ministry[] second = View.ministryYear(secondYear);

        if (first == null || second == null) return List.of();

        int max = Math.min(first.length, second.length);
        List<CompareRow> rows = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            if (first[i] == null || second[i] == null) continue;

            rows.add(new CompareRow(
                    first[i].getMinistryName(),
                    Ministry.getFormattedBudget(first[i].getBudget()),
                    Ministry.getFormattedBudget(second[i].getBudget())
            ));
        }

        return rows;
    }
}
