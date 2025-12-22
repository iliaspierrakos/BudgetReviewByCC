package UserFeatures;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the "View" feature.
 * Provides both CLI-style viewing (optional) and GUI-friendly data access.
 */
public class View {

    /**
     * Row model for JavaFX TableView (GUI).
     * Stores already formatted strings.
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

        public String getMinistry() { return ministry; }
        public String getBudgetText() { return budgetText; }
        public String getPercentText() { return percentText; }
    }

    /**
     * Returns the Ministry array for a specific year (2020–2026).
     */
    public static Ministry[] ministryYear(int year) {
        return switch (year) {
            case 2020 -> CreatingMinistries.ministries2020;
            case 2021 -> CreatingMinistries.ministries2021;
            case 2022 -> CreatingMinistries.ministries2022;
            case 2023 -> CreatingMinistries.ministries2023;
            case 2024 -> CreatingMinistries.ministries2024;
            case 2025 -> CreatingMinistries.ministries2025;
            case 2026 -> CreatingMinistries.ministries2026;
            default -> null;
        };
    }

    /**
     * GUI-friendly method:
     * - DOES NOT print
     * - DOES NOT write files
     * - ONLY returns rows for TableView
     */
    public List<GovBudgetRow> getGovBudgetRowsForGui(int year, boolean sort) {
        Ministry[] selected = ministryYear(year);
        if (selected == null) return List.of();

        // Clone so we never mutate the stored arrays (safe)
        Ministry[] working = selected.clone();

        if (sort) sortingBudgets(working);

        double total = 0;
        for (Ministry m : working) {
            if (m != null) total += m.getBudget();
        }
        if (total == 0) return List.of();

        List<GovBudgetRow> rows = new ArrayList<>();
        for (Ministry m : working) {
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
     * Sorting hook (optional).
     * If you haven’t implemented it yet, leaving it empty is OK.
     */
    public void sortingBudgets(Ministry[] ministries) {
        // TODO: implement sorting if needed
    }
}
