package UserFeatures;

import java.util.List;

/**
 * Creates Ministry objects for each year based on:
 * - budget lines already loaded in memory (MinistriesBudgets)
 * - ministry names provided by Ministries
 *
 * No file writing.
 * No file reading.
 * GUI & JAR safe.
 */
public class CreatingMinistries {

    public static Ministry[] ministries2020 = new Ministry[20];
    public static Ministry[] ministries2021 = new Ministry[20];
    public static Ministry[] ministries2022 = new Ministry[20];
    public static Ministry[] ministries2023 = new Ministry[20];
    public static Ministry[] ministries2024 = new Ministry[20];
    public static Ministry[] ministries2025 = new Ministry[20];
    public static Ministry[] ministries2026 = new Ministry[20];

    /**
     * Creates Ministry objects for a given year using already loaded budgets.
     * Must be called AFTER MinistriesBudgets.loadFromResources(year)
     */
    public static void ministryCreationFromLoadedBudgets(int year) {

        List<String> budgetLines = MinistriesBudgets.getBudgets(year);
        List<String> ministryNames = Ministries.getMinistryNames();

        int total = Math.min(budgetLines.size(), ministryNames.size());

        for (int i = 0; i < total; i++) {

            String budgetLine = budgetLines.get(i).trim();
            String ministryName = ministryNames.get(i).trim();

            // Extract last numeric token (budget)
            String[] tokens = budgetLine.split("\\s+");
            String rawNumber = "0";

            for (int j = tokens.length - 1; j >= 0; j--) {
                if (tokens[j].matches("[\\d\\.,]+")) {
                    rawNumber = tokens[j];
                    break;
                }
            }

            double budget = parseBudget(rawNumber);

            switch (year) {
                case 2020 -> ministries2020[i] = new Ministry(ministryName, budget);
                case 2021 -> ministries2021[i] = new Ministry(ministryName, budget);
                case 2022 -> ministries2022[i] = new Ministry(ministryName, budget);
                case 2023 -> ministries2023[i] = new Ministry(ministryName, budget);
                case 2024 -> ministries2024[i] = new Ministry(ministryName, budget);
                case 2025 -> ministries2025[i] = new Ministry(ministryName, budget);
                case 2026 -> ministries2026[i] = new Ministry(ministryName, budget);
                default -> throw new IllegalArgumentException("Unsupported year: " + year);
            }
        }
    }

    /**
     * Converts budget strings like:
     * "1.234.567,89" -> 1234567.89
     */
    private static double parseBudget(String raw) {
        if (raw == null || raw.isBlank()) return 0.0;

        String clean = raw.replace(".", "").replace(",", ".");
        try {
            return Double.parseDouble(clean);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
