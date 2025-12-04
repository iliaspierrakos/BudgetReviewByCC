package UserFeatures;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Scanner;

/**
 * The {@code Ministry} class represents a government ministry with a name and a general budget.
 * It also includes a static counter to track the number of {@code Ministry} objects created.
 * Provides methods for budget searching and formatting.
 */
public class Ministry {    //Ministry class
    private String ministryName; //Ministry name
    private double budget; //Ministry's general budget
    private static int counter; //

      /**
     * Constructs a new {@code Ministry} object.
     * Increments the static counter upon creation.
     *
     * @param name The name of the ministry.
     * @param number The general budget allocated to the ministry.
     */
    public Ministry (String name, double number) { //Ministry object constructor
        this.ministryName = name;
        this.budget = number;
        counter++;
    }
    /**
     * Returns the total number of {@code Ministry} objects that have been instantiated.
     * This is used, for example, to ensure an array of the correct size is created.
     *
     * @return The number of created ministry objects.
     */
    public static int getCounter() { //getCounter method used for making sure the array is made
        return counter;
    }
     /** Returns a string representation of the {@code Ministry} object,
     * including its name and regular budget.
     *
     * @return A string containing the ministry's name and its regular budget.
     */
    @Override
    public String toString() {
        return ministryName + "Regular Budget:" + budget;
    }

    public static double budgetSearchByName(String searchingMinistry, Ministry[] ministriesArray) { //method for searching the regular budget of a ministry with its name
        for (Ministry m : ministriesArray) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m.budget;
            }
        }
        return -1 ;
    }
    public String getMinistryName(){
        return ministryName;
    }

    /**
     * Sets a new name for the ministry.
     *
     * @param name The new name to set.
     */
    public void setMinistryName(String name){
        this.ministryName = name;
    }

    /**
     * Sets a new general budget for the ministry.
     * Prints "yes" to the console upon successful update.
     *
     * @param budget The new budget to set.
     */
    public void setBudget(double budget) {
        this.budget = budget;
    }

    /**
     * Returns the general budget of the ministry.
     *
     * @return The ministry's general budget.
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Formats a given budget value as a string with a German-style grouping separator ('.').
     * The format uses a thousands separator (e.g., 1000000 becomes "1.000.000").
     *
     * @param budget The double value representing the budget to be formatted.
     * @return A string with the formatted budget.
     */
    public static String getFormattedBudget(double budget) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat df = new DecimalFormat("#,###.##", symbols);
        return df.format(budget);
    }
    // Display all ministries with numbers
    public static void displayListOfMinistries(){
        for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
            if (CreatingMinistries.ministries2026[i] != null) {
                System.out.printf("%d. %s (Budget: %s)%n", 
                    i + 1, // Display 1-based numbering for user
                    CreatingMinistries.ministries2026[i].getMinistryName(),
                    Ministry.getFormattedBudget(CreatingMinistries.ministries2026[i].getBudget()));
            }
        }
    }
    public static String yesOrNo(String response) {
        Scanner scanner = new Scanner(System.in);
        while (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("no"))  {
            System.out.println("Invalid input!Please respond with yes or no.");
            response = scanner.nextLine();
        }
        return response;
    }
}