package UserFeatures;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * The {@code Compare} class provides functionality for comparing
 * government ministry budgets between two different years.
 *
 * <p>The comparison can be:
 * <ul>
 *   <li>Displayed in the console</li>
 *   <li>Exported to a text file</li>
 *   <li>Consumed by a GUI via structured data objects</li>
 * </ul>
 */
public class Compare {

    /** Scanner used for console input. */
    private static final Scanner SCANNER = new Scanner(System.in);

    /**
     * Initiates an interactive comparison between two selected years.
     *
     * <p>The method validates user input, retrieves ministry data
     * for both years, and outputs a formatted comparison table
     * both to the console and to a file.</p>
     */
    public static void comparingMinistries() {
        System.out.println("Please type the first of the two years that you want to compare:");
        int firstYear = validityYear(-1);

        System.out.println("Please type the second year that you want to compare:");
        int secondYear = validityYear(firstYear);

        Ministry[] firstYearMinistry = ViewGovernmentBudget.ministryYear(firstYear);
        Ministry[] secondYearMinistry = ViewGovernmentBudget.ministryYear(secondYear);

        if (firstYearMinistry == null || secondYearMinistry == null) {
            System.out.println("Cannot compare - missing data for one or both years.");
            return;
        }

        Map<String, Ministry> map1 = toMapByName(firstYearMinistry);
        Map<String, Ministry> map2 = toMapByName(secondYearMinistry);

        Set<String> ministryNames = new TreeSet<>();
        ministryNames.addAll(map1.keySet());
        ministryNames.addAll(map2.keySet());

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

        for (String name : ministryNames) {
            Ministry m1 = map1.get(name);
            Ministry m2 = map2.get(name);

            String budget1 = (m1 == null)
                    ? "-"
                    : Ministry.getFormattedBudget(m1.getBudget());
            String budget2 = (m2 == null)
                    ? "-"
                    : Ministry.getFormattedBudget(m2.getBudget());

            TableUtils.appendTableRow(sb, name, budget1, budget2);
        }

        TableUtils.appendSeparator(sb, 120, '=');

        String output = sb.toString();
        System.out.println(output);

        try {
            Path outDir = Paths.get("outputs");
            Files.createDirectories(outDir);

            Path outFile = outDir.resolve(
                    "compare" + firstYear + "with" + secondYear + ".txt"
            );

            Files.writeString(outFile, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error writing comparison file: " + e.getMessage());
        }
    }

    /**
     * Validates and returns a year entered by the user.
     *
     * <p>The year must be within the range 2020–2026 and must not
     * match the forbidden year.</p>
     *
     * @param forbiddenYear a year that cannot be selected
     * @return a valid year
     */
    private static int validityYear(int forbiddenYear) {
        while (true) {
            System.out.println("Please select a year (2020-2026):");
            String input = SCANNER.nextLine().trim();

            int year;
            try {
                year = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid year.");
                continue;
            }

            if (year < 2020 || year > 2026) {
                System.out.println("Invalid year. Please enter a year between 2020 and 2026.");
                continue;
            }

            if (year == forbiddenYear) {
                System.out.println("Cannot be the same as the first year.");
                continue;
            }

            return year;
        }
    }

    /**
     * Converts an array of ministries into a map indexed by ministry name.
     *
     * @param ministries the array of ministries
     * @return a map of ministry name to ministry object
     */
    private static Map<String, Ministry> toMapByName(Ministry[] ministries) {
        Map<String, Ministry> map = new HashMap<>();
        if (ministries == null) return map;

        for (Ministry m : ministries) {
            if (m != null && m.getMinistryName() != null) {
                map.put(m.getMinistryName(), m);
            }
        }
        return map;
    }

    /* ===================== GUI SUPPORT ===================== */

    /**
     * Represents a single comparison row for GUI display.
     */
    public static class CompareRow {

        private final String ministry;
        private final String firstYearBudget;
        private final String secondYearBudget;

        /**
         * Constructs a comparison row.
         *
         * @param ministry the ministry name
         * @param firstYearBudget formatted budget for the first year
         * @param secondYearBudget formatted budget for the second year
         */
        public CompareRow(String ministry,
                          String firstYearBudget,
                          String secondYearBudget) {
            this.ministry = ministry;
            this.firstYearBudget = firstYearBudget;
            this.secondYearBudget = secondYearBudget;
        }

        /** @return ministry name */
        public String getMinistry() {
            return ministry;
        }

        /** @return formatted budget for the first year */
        public String getFirstYearBudget() {
            return firstYearBudget;
        }

        /** @return formatted budget for the second year */
        public String getSecondYearBudget() {
            return secondYearBudget;
        }
    }

    /**
     * Generates comparison rows suitable for GUI tables.
     *
     * @param firstYear the first comparison year
     * @param secondYear the second comparison year
     * @return a list of comparison rows
     */
    public static List<CompareRow> getComparisonRowsForGui(int firstYear, int secondYear) {
        List<CompareRow> rows = new ArrayList<>();

        Ministry[] a = ViewGovernmentBudget.ministryYear(firstYear);
        Ministry[] b = ViewGovernmentBudget.ministryYear(secondYear);

        if (a == null || b == null) return rows;

        Map<String, Ministry> m1 = toMapByName(a);
        Map<String, Ministry> m2 = toMapByName(b);

        Set<String> names = new TreeSet<>();
        names.addAll(m1.keySet());
        names.addAll(m2.keySet());

        for (String name : names) {
            Ministry x = m1.get(name);
            Ministry y = m2.get(name);

            String budget1 = (x == null)
                    ? "-"
                    : Ministry.getFormattedBudget(x.getBudget());
            String budget2 = (y == null)
                    ? "-"
                    : Ministry.getFormattedBudget(y.getBudget());

            rows.add(new CompareRow(name, budget1, budget2));
        }

        return rows;
    }
}
