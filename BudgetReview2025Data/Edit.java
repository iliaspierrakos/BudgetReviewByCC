import java.util.Scanner;

public class Edit {
private String name; //instance variables: ministryname , the type of change, the amount of the change
private String change;
private double amount;
private Scanner scanner = new Scanner(System.in);

public void collectData() { // method for collecting user input 
    System.out.println("=== Ministry Budget Transfer ===");

    // Ask for source ministry 
    System.out.print("Transfer from (Ministry of): ");
    String fromName = "Ministry of " + scanner.nextLine();

    // Ask for destination ministry 
    System.out.print("Transfer to (Ministry of): ");
    String toName = "Ministry of " + scanner.nextLine();

    // Show their current budgets
    System.out.println(fromName + " current budget: " + Ministry.budgetSearchByName(fromName));
    System.out.println(toName + " current budget: " + Ministry.budgetSearchByName(toName));

    // Ask for transfer amount
    System.out.print("Enter amount to transfer: ");
    double amount = scanner.nextDouble();
    scanner.nextLine(); 

    // Perform the transfer (Decrease from source, Increase to destination)
    Edit obj1 = new Edit(fromName, "Decrease", amount);
    Edit obj2 = new Edit(toName, "Increase", amount);

    obj1.editingbudget(obj1);
    obj2.editingbudget(obj2);

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
    public void printNewBudget(double finalBudget, String type, int i) {
        View.ministries[i].setBudget(finalBudget);
                System.out.println("Budget updated successfully!");
                System.out.println("New budget for " + View.ministries[i].getMinistryName() + " " + finalBudget);// printing the new result
    }
}