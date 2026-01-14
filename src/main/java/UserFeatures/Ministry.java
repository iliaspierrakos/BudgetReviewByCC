package UserFeatures;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * The {@code Ministry} class represents a government ministry with a name and an assigned budget.
 *
 * <p>
 * This class also provides utility methods for searching, formatting, and displaying ministry
 * budget information.
 * </p>
 */
public class Ministry {

  /** Name of the ministry. */
  private String ministryName;

  /** Current budget of the ministry. */
  private double budget;

  /** Counts the number of ministry instances created. */
  private static int counter;

  /**
   * Constructs a new {@code Ministry}.
   *
   * @param name the ministry name
   * @param number the initial budget
   *
   * @throws IllegalArgumentException if the name is null or blank
   */
  public Ministry(String name, double number) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Ministry name cannot be null/blank");
    }
    this.ministryName = name;
    this.budget = number;
    counter++;
  }

  /**
   * @return the number of ministry instances created
   */
  public static int getCounter() {
    return counter;
  }

  @Override
  public String toString() {
    return ministryName + " Regular Budget: " + getFormattedBudget(budget);
  }

  /**
   * Returns the budget of a ministry identified by name.
   *
   * @param searchingMinistry the ministry name
   * @param ministriesArray the array to search
   *
   * @return the ministry's budget
   *
   * @throws IllegalArgumentException if the ministry is not found
   */
  public static double budgetSearchByName(String searchingMinistry, Ministry[] ministriesArray) {

    Ministry m = findByName(searchingMinistry, ministriesArray);
    if (m == null) {
      throw new IllegalArgumentException("Ministry not found: " + searchingMinistry);
    }
    return m.budget;
  }

  /**
   * Finds a ministry by name.
   *
   * @param searchingMinistry the ministry name
   * @param ministriesArray the array to search
   *
   * @return the matching ministry, or {@code null} if not found
   */
  public static Ministry findByName(String searchingMinistry, Ministry[] ministriesArray) {

    if (searchingMinistry == null || ministriesArray == null)
      return null;

    for (Ministry m : ministriesArray) {
      if (m == null)
        continue;
      if (m.ministryName != null && m.ministryName.equalsIgnoreCase(searchingMinistry)) {
        return m;
      }
    }
    return null;
  }

  /** @return the ministry name */
  public String getMinistryName() {
    return ministryName;
  }

  /**
   * Updates the ministry name.
   *
   * @param name the new name
   *
   * @throws IllegalArgumentException if the name is null or blank
   */
  public void setMinistryName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Ministry name cannot be null/blank");
    }
    this.ministryName = name;
  }

  /**
   * Updates the ministry budget.
   *
   * @param budget the new budget value
   *
   * @throws IllegalArgumentException if the budget is negative
   */
  public void setBudget(double budget) {
    if (budget < 0) {
      throw new IllegalArgumentException("Budget cannot be negative: " + budget);
    }
    this.budget = budget;
  }

  /** @return the current budget */
  public double getBudget() {
    return budget;
  }

  /**
   * Formats a budget value using Greek/German-style formatting (e.g. {@code 1.234.567,89}).
   *
   * @param budget the numeric budget value
   *
   * @return formatted budget string
   */
  public static String getFormattedBudget(double budget) {
    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMAN);
    symbols.setGroupingSeparator('.');
    symbols.setDecimalSeparator(',');

    DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
    return df.format(budget);
  }

  /**
   * Displays the list of ministries and their budgets to the console.
   */
  public static void displayListOfMinistries() {
    for (int i = 0; i < CreatingMinistries.ministries2026.length; i++) {
      Ministry m = CreatingMinistries.ministries2026[i];
      if (m != null) {
        System.out.printf("%d. %s (Budget: %s)%n", i + 1, m.getMinistryName(),
            getFormattedBudget(m.getBudget()));
      }
    }
  }

  /**
   * Validates a yes/no response.
   *
   * @param response the input string
   *
   * @return {@code "yes"} or {@code "no"} (defaults to {@code "no"} if invalid)
   */
  public static String yesOrNo(String response) {
    if (response == null
        || (!response.equalsIgnoreCase("yes") && !response.equalsIgnoreCase("no"))) {
      return "no";
    }
    return response;
  }
}
