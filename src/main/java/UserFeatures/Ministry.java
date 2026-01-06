package UserFeatures;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Represents a government ministry with a name and a budget.
 */
public class Ministry {

    private String ministryName;
    private double budget;
    private static int counter;

    public Ministry(String name, double number) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ministry name cannot be null/blank");
        }
        this.ministryName = name;
        this.budget = number;
        counter++;
    }

    public static int getCounter() {
        return counter;
    }

    @Override
    public String toString() {
        return ministryName + " Regular Budget: " + getFormattedBudget(budget);
    }

    /**
     * Returns the budget for a ministry by name.
     * @throws IllegalArgumentException if not found.
     */
    public static double budgetSearchByName(String searchingMinistry, Ministry[] ministriesArray) {
        Ministry m = findByName(searchingMinistry, ministriesArray);
        if (m == null) {
            throw new IllegalArgumentException("Ministry not found: " + searchingMinistry);
        }
        return m.budget;
    }

    /**
     * Finds a ministry object by name. Returns null if not found.
     */
    public static Ministry findByName(String searchingMinistry, Ministry[] ministriesArray) {
        if (searchingMinistry == null || ministriesArray == null) return null;

        for (Ministry m : ministriesArray) {
            if (m == null) continue;
            if (m.ministryName != null && m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m;
            }
        }
        return null;
    }

    public String getMinistryName() {
        return ministryName;
    }

    public void setMinistryName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ministry name cannot be null/blank");
        }
        this.ministryName = name;
    }

    public void setBudget(double budget) {
        // Επιτρέπεις 0 και πάνω. Αν θες να επιτρέπεις και αρνητικά, αφαίρεσε το check.
        if (budget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative: " + budget);
        }
        this.budget = budget;
    }

    public double getBudget() {
        return budget;
    }

    /**
     * Greek/German-style grouping: 1.234.567,89
     */
    public static String getFormattedBudget(double budget) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        // δείχνει έως 2 δεκαδικά, χωρίς να γεμίζει μηδενικά
        DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
        return df.format(budget);
    }

    public static void displayListOfMinistries() {
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            Ministry m = CreatingMinistries.ministries2026[i];
            if (m != null) {
                System.out.printf(
                        "%d. %s (Budget: %s)%n",
                        i + 1,
                        m.getMinistryName(),
                        Ministry.getFormattedBudget(m.getBudget())
                );
            }
        }
    }

    public static String yesOrNo(String response) {
        while (response == null || (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("no"))) {
            // δεν ανοίγουμε Scanner εδώ (static leak). Απλά κάνουμε validate.
            return "no";
        }
        return response;
    }
}
