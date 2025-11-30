package UserFeatures;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Scanner;

/**
 * The {@code Edit} class provides the methods for transferring and editing ministry budgets.
 * It manages a central {@code balance} for investments/transfers and includes
 * methods for collecting input, validating data, and updating ministry budgets.
 *
 * Note: This class relies on the {@code CreatingMinistries} and {@code Ministry} classes
 * for accessing ministry data and formatting budgets.
 */
public class Edit {
    //instance variables: ministryname , the type of change, the amount of the change
    private String name;
    private String change;
    private double amount;
    private Scanner scanner = new Scanner(System.in);
    public static double balance = 0;

    /**
     * Collects user input for a budget transfer.
     * The flow depends on the current value of the static {@code balance}.
     */
    public void collectData() {
        System.out.println("*** Ministry Budget Transfer ***");

        if (balance==0) {
            zerobalance(); // No money available, must decrease first
        } else {
            nonzerobalance(); // Money available, can increase or decrease
        }
    }

    /**
     * Constructs an {@code Edit} object with the target ministry name,
     * the type of change, and the amount of the change.
     *
     * @param name The name of the ministry to edit.
     * @param change The type of change ("Increase" or "Decrease").
     * @param amount The amount of money to transfer.
     */
    public Edit(String name, String change, double amount) {
        this.name = name;
        this.amount = amount;
        this.change = change;
    }




    /**
     * Edits the actual budget based on the data in the
     * provided {@code Edit} object.
     * It searches for the ministry and updates its budget by adding or subtracting
     * the specified amount.
     *
     * @param object The {@code Edit} object containing the ministry name, change type, and amount.
     */
    public void editingbudget( Edit object) {
        for (int i = 0; i < CreatingMinistries.ministries.length; i++) {// Loop used for searching the ministry's name
            if (CreatingMinistries.ministries[i] != null && CreatingMinistries.ministries[i].getMinistryName().equalsIgnoreCase(object.name)) {
                // found the correct ministry
                double newBudget;
                if (object.change.equalsIgnoreCase("Increase")) {// checking the type of change and making the proper move to the ministry's budget
                    newBudget = CreatingMinistries.ministries[i].getBudget() + object.amount;
                    printNewBudget(newBudget,"Increase",i);
                } else {
                    newBudget = CreatingMinistries.ministries[i].getBudget() - object.amount ;
                    printNewBudget(newBudget, "Decrease", i);

                }
                break; // Exit loop once ministry is found
            }
        }
    }

    /**
     * Sets the new budget for the ministry and prints the result of the edit.
     *
     * @param finalBudget The newly calculated budget value.
     * @param type The type of change ("Increase" or "Decrease").
     * @param i The index of the ministry in the {@code CreatingMinistries.ministries} array.
     */
    public void printNewBudget(double finalBudget, String type, int i) {
        double previousBudget = CreatingMinistries.ministries[i].getBudget();
        String ministryName = CreatingMinistries.ministries[i].getMinistryName();
        CreatingMinistries.ministries[i].setBudget(finalBudget);
        EditHistory.historyOfEdit(ministryName, previousBudget, finalBudget);
        System.out.println("Budget updated successfully!");
        System.out.println("New budget for " + CreatingMinistries.ministries[i].getMinistryName() + " " + Ministry.getFormattedBudget(CreatingMinistries.ministries[i].getBudget()));// printing the new result
    }


    /**
     * Prompts the user for a ministry name and validates that the name exists
     * in the list of created ministries. Repeats until a valid name is provided.
     *
     * @param name The initial ministry name provided by the user.
     * @return The validated and existing ministry name.
     */
    public String validityCheck(String name){
       boolean minfound=false;
        do {
            for (int i = 0; i < CreatingMinistries.ministries.length; i++) {
                if (CreatingMinistries.ministries[i].getMinistryName().equalsIgnoreCase(name)) {
                    minfound=true;
                    break;
                }
            }
            if (minfound==false) {
                System.out.println("Invalid name of Ministry. Please type again!");
                name = "Ministry of " + scanner.nextLine();
            }
        } while (minfound == false);
        return name;
    }


    /**
     * Handles the budget transfer process when the available {@code balance} is zero.
     * The user must first choose a ministry to **Decrease** its budget to create
     * available balance before any other edits can be made.
     */
    public void zerobalance(){
        // Ask for source ministry
        System.out.println("You have to decrease first a ministry's budget because you do not have available money ");
        System.out.println("Which ministry's budgets do you want to decrease?");
        String fromName = "Ministry of " + scanner.nextLine();
        fromName = validityCheck(fromName); //Validate the ministry's name
        System.out.println("The budget of the " + fromName + "is " + Ministry.getFormattedBudget(Ministry.budgetSearchByName(fromName)));
        // Ask for amount
        System.out.println("Enter amount to decrease:");
        double amount = validityAmount(Ministry.budgetSearchByName(fromName)); //Validate the amount with ministry's budget
        balance = balance + amount;
        // Show previous budget and perform the decrease
        System.out.println(fromName + " previous budget: " + Ministry.getFormattedBudget(Ministry.budgetSearchByName(fromName)));
        Edit obj1 = new Edit(fromName, "Decrease", amount);
        obj1.editingbudget(obj1);
        System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));

        //Ask for a new edit either increase or decrease
        System.out.println("Would you like to edit the budget of another ministry? ");
        String answer=scanner.nextLine();
        answer=validityAnswer(answer);  //Validation for answer

        if (answer.equalsIgnoreCase("no")) {
            return; //exit
        }
        nonzerobalance();
    }


    /**
     * Handles the budget transfer process when there is available {@code balance}.
     * The user can choose to Increase or Decrease a ministry's budget.
     * Increase operations consume the available balance; Decrease operations add to it.
     */
    public void nonzerobalance () {
        String newanswer;
        do {
            // Ask for destination ministry
            System.out.println("Which ministry's budgets do you want to edit?");
            String toName = "Ministry of " + scanner.nextLine();
            toName = validityCheck(toName); //Validate
            System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));
            System.out.println("Do you want to Increase or Decrease the budget of " + toName + "?" );
            String change=scanner.nextLine();
            change = validityChange(change); //Validation for change
            System.out.println("By how much?");
            double changeamount = 0;

            if (change.equalsIgnoreCase("Decrease")) {
                changeamount = validityAmount(Ministry.budgetSearchByName(toName)); //Validate the amount with ministry's budget
                balance=balance + changeamount;
            } else {
                changeamount = validityAmount(balance); //Validate the amount with balance
                balance = balance - changeamount;
            }
            // Show their current budgets
            System.out.println(toName + " previous budget: " + Ministry.getFormattedBudget(Ministry.budgetSearchByName(toName)));

            // Perform the transfer (Decrease from source, Increase to destination)
            Edit obj2 = new Edit(toName, change, changeamount);
            obj2.editingbudget(obj2);
            System.out.println("Available money for Investment : " + Ministry.getFormattedBudget(balance));
            //Ask for edit either increase or decrease
            System.out.println("Would you like to edit the budget of another ministry? ");
            newanswer=scanner.nextLine();
            newanswer=validityAnswer(newanswer);
            // If balance is 0 and user wants a new edit , go to zerobalance
            if (balance==0 && newanswer.equalsIgnoreCase("yes")){
                zerobalance();
            }
        } while (newanswer.equalsIgnoreCase("yes"));
    }


    /**
     * Validates that the user input for the type of change is either "Increase" or "Decrease" (case-insensitive).
     * Prompts the user to re-enter the input until a valid choice is made.
     *
     * @param validChange The initial user input for the change type.
     * @return The validated change type string ("Increase" or "Decrease").
     */
    public String validityChange(String validChange) {
        while (true) {
            if (!validChange.equalsIgnoreCase("Increase") && !validChange.equalsIgnoreCase("Decrease")) {
                System.out.println("Invalid input, please type Increase or Decrease");
            } else {
                break;
            }
            validChange=scanner.nextLine();
        }
        return validChange;
    }


    /**
     * Validates that the user input for an answer is either "Yes" or "No" (case-insensitive).
     * Prompts the user to re-enter the input until a valid choice is made.
     *
     * @param validAnswer The initial user input for the answer.
     * @return The validated answer string ("Yes" or "No").
     */
    public String validityAnswer(String validAnswer) {
        boolean valid = false ;
        do {
            if (validAnswer.equalsIgnoreCase("yes") || validAnswer.equalsIgnoreCase("no")) {
                valid = true;
            } else {
                System.out.println("Invalid input. Your answer must be either Yes or No.");
                validAnswer=scanner.nextLine();
            }
        } while(!valid);
        return validAnswer;
    }

    /**
     * Validates that the user-entered amount is a positive number and does not exceed
     * the maximum available limit (either the ministry's current budget for a Decrease,
     * or the available {@code balance} for an Increase).
     *
     * @param budgetOrBalance The maximum available amount (budget of ministry or available balance).
     * @return The validated amount as a double.
     */
    public double validityAmount(double budgetOrBalance) {
        boolean validInput = false;
        double validAmount = 0;
        while (!validInput) {
            if (scanner.hasNextDouble()) {
                // Check if input is a valid double
                validAmount = scanner.nextDouble();
                scanner.nextLine(); // Clear
                if (validAmount>0) {
                    if (budgetOrBalance < validAmount) {//if decrease takes the current budget if decrease takes the balance
                        System.out.println("The amount "+ Ministry.getFormattedBudget(validAmount) + " exceeds the maximum limit of "+ Ministry.getFormattedBudget(budgetOrBalance) +
                        ". Please enter a smaller amount");
                    } else {
                        break;
                    }
                } else {
                    System.out.println("The amount must be a positive number. Please enter a new amount");
                }
            } else {
                System.out.println("Invalid input! Please enter a numeric value.");
                scanner.nextLine();
            }

        }
        return validAmount;
    }

    /**
     * NumberFormat object for formatting numbers, configured for US locale
     * and used by {@code formatNumber}.
     */
    NumberFormat df = NumberFormat.getNumberInstance(Locale.US);//make number readable


    /**
     * Default constructor. Configures the {@code NumberFormat} object for readable output.
     */
    public Edit() {
        df.setMaximumFractionDigits(2);
    }

    /**
     * Formats a double number into a readable string using the configured {@code NumberFormat}.
     *
     * @param number The double value to format.
     * @return The formatted string representation of the number.
     */
    public String formatNumber(double number) {
        return df.format(number);
    }
}
