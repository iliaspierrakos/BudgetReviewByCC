import java.util.Scanner;

public class Edit {
    //instance variables: ministryname , the type of change, the amount of the change
    private String name;
    private String change;
    private double amount;
    private Scanner scanner = new Scanner(System.in);
    public static double balance = 0;

    public void collectData() { // method for collecting user input
        System.out.println("*** Ministry Budget Transfer ***");

        if (balance==0) {
            zerobalance(); // No money available, must decrease first
        } else {
            nonzerobalance(); // Money available, can increase or decrease
        }
    }


    public Edit(String name, String change, double amount) {// edit constructor
        this.name = name;
        this.amount = amount;
        this.change = change;
    }


    public Edit() {}; //default constructor useless



    public void editingbudget( Edit object) {// Editing budget method
        for (int i = 0; i < View.ministries.length; i++) {// Loop used for searching the ministry's name
            if (View.ministries[i] != null && View.ministries[i].getMinistryName().equalsIgnoreCase(object.name)) {
                // found the correct ministry
                double newBudget;
                if (object.change.equalsIgnoreCase("Increase")) {// checking the type of change and making the proper move to the ministry's budget
                    newBudget = View.ministries[i].getBudget() + object.amount;
                    printNewBudget(newBudget,"Increase",i);
                } else {
                    newBudget = View.ministries[i].getBudget() - object.amount ;
                    printNewBudget(newBudget, "Decrease", i);

                }
                break; // Exit loop once ministry is found
            }
        }
    }


    public void printNewBudget(double finalBudget, String type, int i) { // method for printing the Edit results
        View.ministries[i].setBudget(finalBudget);
        System.out.println("Budget updated successfully!");
        System.out.println("New budget for " + View.ministries[i].getMinistryName() + " " + finalBudget);// printing the new result
    }


    // Validate the ministry name
    public String validityCheck(String name){
       boolean minfound=false;
        do {
            for (int i = 0; i < View.ministries.length; i++) {
                if (View.ministries[i].getMinistryName().equalsIgnoreCase(name)) {
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


    // Edit when balance is zero (must decrease first)
    public void zerobalance(){
        // Ask for source ministry
        System.out.println("You have to decrease first a ministry's budget because you do not have available money ");
        System.out.println("Which ministry's budgets do you want to decrease?");
        String fromName = "Ministry of " + scanner.nextLine();
        fromName = validityCheck(fromName); //Validate the ministry's name

        // Ask for amount
        System.out.println("Enter amount to decrease:");
        double amount = validityAmount(Ministry.budgetSearchByName(fromName)); //Validate the amount with ministry's budget
        balance = balance + amount;
        // Show previous budget and perform the decrease
        System.out.println(fromName + " previous budget: " + Ministry.budgetSearchByName(fromName));
        Edit obj1 = new Edit(fromName, "Decrease", amount);
        obj1.editingbudget(obj1);
        System.out.println("Available money for Investment : " + balance);


        //Ask for a new edit either increase or decrease
        System.out.println("Would you like to edit the budget of another ministry? ");
        String answer=scanner.nextLine();
        answer=validityAnswer(answer);  //Validation for answer

        if (answer.equalsIgnoreCase("no")) {
            return; //exit
        }
        nonzerobalance();
    }


    //Edit when balance is available
    public void nonzerobalance () {
        String newanswer;
        do {
            // Ask for destination ministry
            System.out.println("Which ministry's budgets do you want to edit?");
            String toName = "Ministry of " + scanner.nextLine();
            toName = validityCheck(toName); //Validate

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
            System.out.println(toName + " current budget: " + Ministry.budgetSearchByName(toName));

            // Perform the transfer (Decrease from source, Increase to destination)
            Edit obj2 = new Edit(toName, change, amount);
            obj2.editingbudget(obj2);
            System.out.println("Available money for Investment : " +balance);
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


    // Validate that the change type is either Increase or Decrease
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


    // Validate that the change type is either yes or no
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

    //Validate the amount
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
                        System.out.println("The amount "+ validAmount + " exceeds the maximum limit "+ budgetOrBalance +
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
}
