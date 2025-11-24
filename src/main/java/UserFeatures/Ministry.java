package UserFeatures;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

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
    /**
     * Searches for a ministry by name in the {@code CreatingMinistries.ministries} array
     * and returns its regular budget.
     *
     * @param searchingMinistry The name of the ministry to search for.
     * @return The budget of the found ministry, or -1 if the ministry is not found.
     */
    public static double budgetSearchByName(String searchingMinistry) { //method for searching the regular budget of a ministry with its name
        for (Ministry m : CreatingMinistries.ministries) {
            if (m.ministryName.equalsIgnoreCase(searchingMinistry)) {
                return m.budget;
            }
        }
        return -1 ;
    }
    
    /**
     * Returns the name of the ministry.
     *
     * @return The ministry's name.
     */
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
        System.out.println("yes");
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
    DecimalFormat df = new DecimalFormat("#,###", symbols);
    return df.format(budget);
    }
}
