package UserFeatures;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TaxReceipt {

    public static class TaxRow {
        private final String ministry;
        private final String shareText;   // formatted for table
        private final double shareValue;  // for sorting if needed

        public TaxRow(String ministry, double shareValue) {
            this.ministry = ministry;
            this.shareValue = shareValue;
            this.shareText = String.format("%.2f", shareValue);
        }

        public String getMinistry() { return ministry; }
        public String getShareText() { return shareText; }
        public double getShareValue() { return shareValue; }
    }

    public static class TaxResult {
        private final double income;
        private final int kids;
        private final int age;
        private final double tax;
        private final String incomeText;
        private final String taxText;
        private final List<TaxRow> rows;

        public TaxResult(double income, int kids, int age, double tax, List<TaxRow> rows) {
            this.income = income;
            this.kids = kids;
            this.age = age;
            this.tax = tax;
            this.incomeText = Ministry.getFormattedBudget(income);
            this.taxText = Ministry.getFormattedBudget(tax);
            this.rows = rows;
        }

        public double getIncome() { return income; }
        public int getKids() { return kids; }
        public int getAge() { return age; }
        public double getTax() { return tax; }
        public String getIncomeText() { return incomeText; }
        public String getTaxText() { return taxText; }
        public List<TaxRow> getRows() { return rows; }
    }

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
            throw new IllegalStateException("Total Government Budget is zero. Cannot calculate distribution.");
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
