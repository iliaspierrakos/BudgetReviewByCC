package UserFeatures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The {@code TaxReceipt} class is responsible for calculating an individual's tax
 * based on income, number of children, and age, and for distributing that tax
 * proportionally across government ministries.
 *
 * <p>The generated result is primarily intended for GUI presentation, providing
 * both numeric values and formatted text suitable for display.</p>
 *
 * <p>This class contains two helper data classes ({@link TaxRow} and {@link TaxResult})
 * as well as utility methods for tax calculation and distribution.</p>
 *
 */
public class TaxReceipt {

    /**
     * Represents a single row in the tax receipt table.
     * Each row corresponds to a government ministry and shows
     * the user's contribution to that ministry.
     */
    public static class TaxRow {

        /** Name of the ministry. */
        private final String ministry;

        /** Formatted text of the user's contribution (for GUI display). */
        private final String shareText;

        /** Numeric value of the user's contribution (used for sorting). */
        private final double shareValue;

        /**
         * Constructs a {@code TaxRow} for a specific ministry.
         *
         * @param ministry   the name of the ministry
         * @param shareValue the monetary contribution to that ministry
         */
        public TaxRow(String ministry, double shareValue) {
            this.ministry = ministry;
            this.shareValue = shareValue;
            this.shareText = String.format("%.2f", shareValue);
        }

        /**
         * @return the ministry name
         */
        public String getMinistry() {
            return ministry;
        }

        /**
         * @return the formatted contribution value as text
         */
        public String getShareText() {
            return shareText;
        }

        /**
         * @return the numeric contribution value
         */
        public double getShareValue() {
            return shareValue;
        }
    }

    /**
     * Encapsulates the complete tax calculation result for a user.
     * This includes personal data, total tax, formatted values, and
     * the detailed distribution across ministries.
     */
    public static class TaxResult {

        /** User's annual income. */
        private final double income;

        /** Number of dependent children. */
        private final int kids;

        /** User's age. */
        private final int age;

        /** Total calculated tax. */
        private final double tax;

        /** Formatted income text for display. */
        private final String incomeText;

        /** Formatted tax text for display. */
        private final String taxText;

        /** List of tax distribution rows per ministry. */
        private final List<TaxRow> rows;

        /**
         * Constructs a {@code TaxResult} object.
         *
         * @param income user's income
         * @param kids number of children
         * @param age user's age
         * @param tax total calculated tax
         * @param rows list of tax distribution rows
         */
        public TaxResult(double income, int kids, int age, double tax, List<TaxRow> rows) {
            this.income = income;
            this.kids = kids;
            this.age = age;
            this.tax = tax;
            this.incomeText = Ministry.getFormattedBudget(income);
            this.taxText = Ministry.getFormattedBudget(tax);
            this.rows = rows;
        }

        /** @return user's income */
        public double getIncome() { return income; }

        /** @return number of children */
        public int getKids() { return kids; }

        /** @return user's age */
        public int getAge() { return age; }

        /** @return total calculated tax */
        public double getTax() { return tax; }

        /** @return formatted income string */
        public String getIncomeText() { return incomeText; }

        /** @return formatted tax string */
        public String getTaxText() { return taxText; }

        /** @return list of tax distribution rows */
        public List<TaxRow> getRows() { return rows; }
    }

    /**
     * Generates a {@link TaxResult} object for GUI usage.
     *
     * <p>The method validates input values, calculates the total tax,
     * and distributes it proportionally according to each ministry's
     * budget.</p>
     *
     * @param income user's annual income
     * @param kids number of children
     * @param age user's age
     * @return a {@code TaxResult} containing tax and distribution data
     * @throws IllegalArgumentException if income or kids are negative,
     *                                  or if age is below 18
     * @throws IllegalStateException if the total government budget is zero
     */
    public static TaxResult generateForGui(double income, int kids, int age) {
        if (income < 0) throw new IllegalArgumentException("Income cannot be negative.");
        if (kids < 0) throw new IllegalArgumentException("Kids cannot be negative.");
        if (age < 18) throw new IllegalArgumentException("Age must be at least 18.");

        double tax = calculateTax(income, kids, age);

        double totalGovBudget = 0;
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) totalGovBudget += m.getBudget();
        }

        if (totalGovBudget <= 0) {
            throw new IllegalStateException(
                "Total Government Budget is zero. Cannot calculate distribution."
            );
        }

        List<TaxRow> rows = new ArrayList<>();
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m == null) continue;

            double percentage = m.getBudget() / totalGovBudget;
            double myContribution = tax * percentage;

            if (myContribution > 0.01) {
                rows.add(new TaxRow(m.getMinistryName(), myContribution));
            }
        }

        rows.sort(Comparator.comparingDouble(TaxRow::getShareValue).reversed());

        return new TaxResult(income, kids, age, tax, rows);
    }

    /**
     * Calculates the tax amount based on progressive tax brackets,
     * age-related exemptions, and child-related reductions.
     *
     * <p>This method implements the tax rules defined for the project
     * and is not exposed publicly.</p>
     *
     * @param income user's income
     * @param kids number of children
     * @param age user's age
     * @return calculated tax amount
     */
    private static double calculateTax(double income, int kids, int age) {
        double tax = 0;

        double rate1 = (kids >= 4) ? 0 : 0.09;
        if (age <= 25 || (age <= 30 && kids >= 4)) rate1 = 0;

        tax += Math.min(income, 10000) * rate1;

        if (income > 10000) {
            double rate2;
            if (kids == 0) rate2 = 0.20;
            else if (kids == 1) rate2 = 0.18;
            else if (kids == 2) rate2 = 0.16;
            else if (kids == 3) rate2 = 0.09;
            else rate2 = 0;

            if (age <= 25 || (age <= 30 && kids >= 4)) rate2 = 0;
            else if (age <= 30) rate2 = 0.09;

            tax += Math.min(income - 10000, 10000) * rate2;
        }

        if (income > 20000) {
            double rate3;
            if (kids == 0) rate3 = 0.26;
            else if (kids == 1) rate3 = 0.24;
            else if (kids == 2) rate3 = 0.22;
            else if (kids == 3) rate3 = 0.20;
            else rate3 = 0.18;

            tax += Math.min(income - 20000, 10000) * rate3;
        }

        if (income > 30000) tax += Math.min(income - 30000, 10000) * 0.34;
        if (income > 40000) tax += Math.min(income - 40000, 20000) * 0.39;
        if (income > 60000) tax += (income - 60000) * 0.44;

        return tax;
    }
}
