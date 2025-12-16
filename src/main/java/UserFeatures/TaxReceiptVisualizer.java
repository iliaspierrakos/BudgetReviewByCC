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
        System.out.println("Please enter your approximate Annual Income :");
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
        double tax = 0;

        int kids = validityKids();
        double age = validityAge();
        double rate1;
        if (kids >= 4) {
            rate1 = 0;
        } else {
            rate1 = 0.09;
        }
        if (age<=25 || (age<=30 && kids>=4)) {
            rate1 = 0;
        }
        if (income <= 10000) {
            tax += income * rate1;
        } else {
            tax += 10000 * rate1;
        }

        if (income > 10000) {
            double rate2;

            if (kids == 0) {
                rate2 = 0.20;
            } else if (kids == 1) {
                rate2 = 0.18;
            } else if (kids == 2) {
                rate2 = 0.16;
            } else if (kids == 3) {
                rate2 = 0.09;
            } else {
                rate2 = 0;
            }
            if (age<=25 || (age<=30 && kids>=4)) {
                rate2 = 0;
            } else if (age<=30) {
                rate2 = 0.09;
            }

            if (income <= 20000) {
                tax += (income - 10000) * rate2;
            } else {
                tax += 10000 * rate2;
            }
        }

        if (income > 20000) {
            double rate3;

            if (kids == 0) {
                rate3 = 0.26;
            } else if (kids == 1) {
                rate3 = 0.24;
            } else if (kids == 2) {
                rate3 = 0.22;
            } else if (kids == 3) {
                rate3 = 0.20;
            } else {
                rate3 = 0.18;
            }

            if (income <= 30000) {
                tax += (income - 20000) * rate3;
            } else {
                tax += 10000 * rate3;
            }
        }

        if (income > 30000) {
            if (income <= 40000) {
                tax += (income - 30000) * 0.34;
            } else {
                tax += 10000 * 0.34;
            }
        }

        if (income > 40000) {
            if (income <= 60000) {
                tax += (income - 40000) * 0.39;
            } else {
                tax += 20000 * 0.39;
            }
        }

        if (income > 60000) {
            tax += (income - 60000) * 0.44;
        }


        System.out.println("\nBased on your income of " + Ministry.getFormattedBudget(income) );
        System.out.println("your estimated contribution (Taxes) is: " + Ministry.getFormattedBudget(tax) );
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
        System.out.printf("%-60s %10s%n", "MINISTRY", "YOUR SHARE");
        System.out.println("--------------------------------------------------");

        for (Ministry m : CreatingMinistries.ministries2026) {
            if (m != null) {
                // Logic: (Ministry Budget / Total Budget) * User's Tax
                double percentage = m.getBudget() / totalGovBudget;
                double myContribution = tax * percentage;

                // Only show ministries where the contribution is significant (e.g., > 0.01€)
                if (myContribution > 0.01) {
                    System.out.printf("%-60s %9.2f%n", m.getMinistryName(), myContribution);
                }
            }
        }

        System.out.println("--------------------------------------------------");
        System.out.printf("%-60s %9.2f%n", "TOTAL TAX PAID", tax);
        System.out.println("==================================================");
        System.out.println("   Thank you for your contribution to society!    ");
        System.out.println("==================================================\n");


    }
    public static int validityKids() {

        Scanner sc = new Scanner(System.in);
        int kids;

        do {
            System.out.print("Enter how many children you have: ");
            kids = sc.nextInt();
            sc.nextLine();

            if (kids < 0) {
                System.out.println("Invalid input! The number of children cannot be less than 0.");
            }

        } while (kids < 0);

        return kids;
    }
    public static int validityAge() {
        Scanner sc = new Scanner(System.in);
        int age;

        do {
            System.out.print("Enter your age: ");
            age = sc.nextInt();
            sc.nextLine();


            if (age <= 0) {
                System.out.println("Invalid input! Age cannot be negative or zero.");
            } else if (age < 18) {
                System.out.println("Unfortunately you are a minor! You must be at least 18 years old.");
            }

        } while (age <= 0 || age < 18);

        return age;
    }
}