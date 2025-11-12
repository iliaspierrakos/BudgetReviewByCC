import java.util.Scanner;

public class Edit {
private String name; //instance variables: ministryname , the type of change, the amount of the change
private String change;
private double amount;
private Scanner scanner = new Scanner(System.in);
public static double balance = 0;

public void collectData() { // method for collecting user input
    System.out.println("*** Ministry Budget Transfer ***");

    if (balance==0) {
        // Ask for source ministry
        System.out.println("Which ministries' budgets do you want to decrease?");
        String fromName = "Ministry of " + scanner.nextLine();
        fromName = validityCheck(fromName);
        //Input validation check 
        double currentBudget = Ministry.budgetSearchByName(fromName);
        double amount = 0;
        boolean validInput = false ;
        while (!validInput) {
            System.out.println("Enter amount to decrease:");// Ask for transfer amount
            if (scanner.hasNextDouble()) {
                amount = scanner.nextDouble();
                scanner.nextLine();

                if (amount > currentBudget){
                System.out.println("Error:The amount ("+ amount + ") exceeds the Ministry's currnet budget ("+ currentBudget + 
                "). Please enter a smaller amount");
                }else if (amount<=0) {
               System.out.println("Error:The amount must be a positive number. Please enter a new amount"); 
                }else {
                validInput = true;
            }
        }else {
            System.out.println("Error:Invalid input. Please enter a valid number");
            scanner.nextLine();
        }
    }    
        
    balance = balance + amount;
    System.out.println(fromName + " previous budget: " + Ministry.budgetSearchByName(fromName));
    Edit obj1 = new Edit(fromName, "Decrease", amount);
    obj1.editingbudget(obj1);
    System.out.println("Available money for Investment : " + balance);


    //Ask for edit either increase or decrease
    System.out.println("Would you like to edit the budget of another ministry? ");
    String answer=scanner.nextLine(); //need validation

    if (answer.equalsIgnoreCase("no")) { //need validation
            return; //exit
        }
        newedit();
    } else {
        newedit();
    }
}

public Edit(String name, String change, double amount) {// edit constructor
        this.name = name;
        this.amount = amount;
        this.change = change;
}
public Edit() {}; //default constructor useless

public void editingbudget( Edit object) {// editing budget method
        for (int i = 0; i < View.ministries.length; i++) {// loop used for searching the ministry's name
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
                break;
            }
        }
    }
    public void printNewBudget(double finalBudget, String type, int i) { // method for printing the Edit results
        View.ministries[i].setBudget(finalBudget);
                System.out.println("Budget updated successfully!");
                System.out.println("New budget for " + View.ministries[i].getMinistryName() + " " + finalBudget);// printing the new result
    }

    public String validityCheck(String name){ // method used for validating the name of the ministry exists
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
    public void newedit () {
        String newanswer;
        do {
            // Ask for destination ministry
            System.out.println("Which ministries' budgets do you want to edit?");
            String toName = "Ministry of " + scanner.nextLine();
            toName = validityCheck(toName);

            System.out.println("Do you want to Increase or Decrease the budget of " + toName + "?" );
            String change=scanner.nextLine(); //need validation
            System.out.println("By how much?");
            double changeamount = scanner.nextDouble(); //need validation
            scanner.nextLine();

            if (change.equalsIgnoreCase("Decrease")) {
                balance=balance + changeamount;
            } else {
                balance = balance - changeamount; //need validation changeamount more that balance
            }

            // Show their current budgets
            System.out.println(toName + " current budget: " + Ministry.budgetSearchByName(toName));

            // Perform the transfer (Decrease from source, Increase to destination)
            Edit obj2 = new Edit(toName, change, amount);
            obj2.editingbudget(obj2);
            System.out.println("Available money for Investment : " +balance);
            //Ask for edit either increase or decrease
            System.out.println("Would you like to edit the budget of another ministry? ");
            newanswer=scanner.nextLine(); //need validation
        } while (newanswer.equalsIgnoreCase("yes"));
    }
}
