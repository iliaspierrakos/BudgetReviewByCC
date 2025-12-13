package UserFeatures;

import java.util.Scanner;

/**
 * The TaxReceiptVisualizer class implements the "Personal Tax Receipt" feature.
 * This feature is designed exclusively for Citizens.
 * * It allows the user to input their annual income, calculates the estimated taxes,
 * and visualizes how their specific tax money is distributed among the different ministries
 * based on the 2026 government budget.
 */
public class TaxReceiptVisualizer {

    /**
     * The main method of this feature.
     * It asks for the user's income, calculates the tax (assuming a flat 20% rate for simplicity),
     * and prints a receipt showing where that money goes.
     */
    public void generateReceipt() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n*** PERSONAL TAX RECEIPT VISUALIZER ***");
        System.out.println("See exactly where your taxes go!");
        System.out.println("------------------------------------------------");
        
        //  Get User Input
        System.out.println("Please enter your approximate Annual Income (€):");
        double income = -1;
        
        // Simple validation to ensure positive income
        while (income < 0) {
            if (scanner.hasNextDouble()) {
                income = scanner.nextDouble();
                if (income < 0) {
                    System.out.println("Income cannot be negative. Try again:");
                }
            } else {
                System.out.println("Invalid input. Please enter a number:");
                scanner.next(); // clear invalid input
            }
        }

        //  Calculate Tax
        double estimatedTax = income * 0.20; 

        System.out.println("\nBased on your income of " + Ministry.getFormattedBudget(income) + "€,");
        System.out.println("your estimated contribution (Taxes) is: " + Ministry.getFormattedBudget(estimatedTax) + "€");
        System.out.println("\nHere is your personal \"Retail Receipt\" from the State:");
        
        //   Calculate Total Government Budget (Reference Year 2026)
        double totalGovBudget = 0;
        // We use the static array from CreatingMinistries as our data source
        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                totalGovBudget += m.getBudget();
            }
        }

        // Avoid division by zero if data is missing
        if (totalGovBudget == 0) {
            System.out.println("Error: Total Government Budget is zero. Cannot calculate distribution.");
            return;
        }

        // 4. Generate and Print the Receipt
        System.out.println("\n==================================================");
        System.out.println("             OFFICIAL STATE RECEIPT               ");
        System.out.println("==================================================");
        System.out.printf("%-40s %10s%n", "MINISTRY", "YOUR SHARE");
        System.out.println("--------------------------------------------------");

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                // Logic: (Ministry Budget / Total Budget) * User's Tax
                double percentage = m.getBudget() / totalGovBudget;
                double myContribution = estimatedTax * percentage;

                // Only show ministries where the contribution is significant (e.g., > 0.01€)
                if (myContribution > 0.01) {
                    System.out.printf("%-40s %9.2f€%n", m.getMinistryName(), myContribution);
                }
            }
        }
        
        System.out.println("--------------------------------------------------");
        System.out.printf("%-40s %9.2f€%n", "TOTAL TAX PAID", estimatedTax);
        System.out.println("==================================================");
        System.out.println("   Thank you for your contribution to society!    ");
        System.out.println("==================================================\n");
        
        // Wait for user to read before returning
        System.out.println("Press Enter to return to menu...");
        scanner.nextLine(); // clear buffer
        scanner.nextLine(); // wait for enter
    }
}